#!/usr/bin/env bash
# Enables the analytics (Iceberg) bucket API in a local Supabase stack.
#
# Usage: script/enable-analytics.sh [workdir]
#   workdir  Supabase CLI working directory, relative to the repository root (default: integration-test)
#
# The Supabase CLI never sets ICEBERG_ENABLED on the local storage container, so the storage server
# doesn't register the /storage/v1/iceberg routes. [storage.analytics] in config.toml is only applied
# to linked hosted projects. This script recreates the storage container with the same configuration
# plus ICEBERG_ENABLED=true.
#
# In single-tenant mode, analytics bucket create/list/delete, the catalog config and namespaces only
# use the local database. Iceberg tables would still need an external catalog and keep failing locally.
#
# The script depends on the CLI's container naming (supabase_storage_<project_id>). If a future CLI
# release changes it, the script fails with an error instead of silently doing nothing.
#
# Run it after `supabase start`. `supabase stop` removes the recreated container like any other, and
# the next `supabase start` restores the CLI's default setup. Running it twice is a no-op.
#
# Requires: docker, jq, supabase CLI, curl.

set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
workdir="${1:-integration-test}"

fail() {
    echo "error: $*" >&2
    exit 1
}

check_requirements() {
    local tool
    for tool in docker jq supabase curl; do
        command -v "$tool" >/dev/null 2>&1 || fail "'$tool' is required but not installed."
    done
    [[ -f "$repo_root/$workdir/supabase/config.toml" ]] || fail "No supabase/config.toml found in '$workdir'."
}

# Recreates the container described by the inspect JSON in $config with ICEBERG_ENABLED=true.
recreate_with_iceberg() {
    local container="$1" config="$2" env_file args=() cmd=() line network
    env_file=$(mktemp)
    chmod 600 "$env_file"
    # Expanded now, because the local variable is gone when the trap runs at exit.
    # shellcheck disable=SC2064
    trap "rm -f '$env_file'" EXIT

    # Carry over every setting of the CLI-managed container.
    jq -r '.Config.Env[]' <<<"$config" | grep -v '^ICEBERG_ENABLED=' >"$env_file" || true
    echo "ICEBERG_ENABLED=true" >>"$env_file"

    args=(--detach --name "$container" --env-file "$env_file")
    while IFS= read -r line; do args+=(--label "$line"); done < <(jq -r '.Config.Labels // {} | to_entries[] | "\(.key)=\(.value)"' <<<"$config")
    while IFS= read -r line; do args+=(--volume "$line"); done < <(jq -r '.Mounts[] | "\(if .Type == "volume" then .Name else .Source end):\(.Destination)"' <<<"$config")
    network=$(jq -r '.NetworkSettings.Networks | keys[0]' <<<"$config")
    args+=(--network "$network")
    while IFS= read -r line; do args+=(--network-alias "$line"); done < <(jq -r --arg n "$network" '.NetworkSettings.Networks[$n].Aliases // [] | .[]' <<<"$config")
    line=$(jq -r '.HostConfig.RestartPolicy.Name // empty' <<<"$config")
    if [[ -n "$line" && "$line" != "no" ]]; then args+=(--restart "$line"); fi
    line=$(jq -r 'if .Config.Healthcheck.Test[0] == "CMD-SHELL" then .Config.Healthcheck.Test[1] else empty end' <<<"$config")
    if [[ -n "$line" ]]; then args+=(--health-cmd "$line" --health-interval 10s --health-timeout 2s --health-retries 3); fi
    while IFS= read -r line; do cmd+=("$line"); done < <(jq -r '.Config.Cmd // [] | .[]' <<<"$config")

    echo "Recreating $container with ICEBERG_ENABLED=true..."
    docker rm --force "$container" >/dev/null
    docker run "${args[@]}" "$(jq -r '.Config.Image' <<<"$config")" ${cmd[@]+"${cmd[@]}"} >/dev/null
}

# Waits until the iceberg routes answer through the API gateway.
wait_for_analytics_api() {
    local container="$1" status_json api_url service_role_key code=""
    status_json=$(supabase status --workdir "$workdir" --output json 2>/dev/null) || fail "'supabase status' failed. Is the stack running?"
    api_url=$(jq -r '.API_URL' <<<"$status_json")
    service_role_key=$(jq -r '.SERVICE_ROLE_KEY' <<<"$status_json")
    for _ in $(seq 1 30); do
        code=$(curl --silent --output /dev/null --write-out '%{http_code}' \
            -H "Authorization: Bearer $service_role_key" "$api_url/storage/v1/iceberg/bucket" || true)
        if [[ "$code" == "200" ]]; then
            echo "Analytics bucket API is available at $api_url/storage/v1/iceberg."
            return
        fi
        sleep 2
    done
    docker logs --tail 20 "$container" >&2
    fail "Analytics bucket API did not become available (last status: $code)."
}

main() {
    local project_id container config
    cd "$repo_root"
    check_requirements

    project_id=$(sed -n 's/^project_id *= *"\(.*\)"/\1/p' "$workdir/supabase/config.toml")
    container="supabase_storage_${project_id}"
    config=$(docker inspect "$container" 2>/dev/null | jq '.[0]') \
        || fail "Container $container not found. Run 'supabase start --workdir $workdir' first."

    if jq -e '.Config.Env | index("ICEBERG_ENABLED=true")' <<<"$config" >/dev/null; then
        echo "ICEBERG_ENABLED is already set on $container."
    else
        recreate_with_iceberg "$container" "$config"
    fi
    wait_for_analytics_api "$container"
}

main

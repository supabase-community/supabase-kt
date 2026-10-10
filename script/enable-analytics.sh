#!/usr/bin/env bash
# Enables the analytics (Iceberg) bucket API in a local Supabase stack. Run it after `supabase start`.
#
# Usage: script/enable-analytics.sh [workdir]   (default workdir: integration-test)
#
# The Supabase CLI never sets ICEBERG_ENABLED on the local storage container ([storage.analytics] in
# config.toml only applies to hosted projects), and Docker can't change the env of a running container.
# So this recreates the storage container with the same env, labels, volumes and network plus
# ICEBERG_ENABLED=true. The labels keep `supabase stop` cleaning it up. Running it twice is a no-op.
#
# Requires: docker, jq, curl, supabase CLI.

set -euo pipefail

cd "$(dirname "$0")/.."
workdir="${1:-integration-test}"
project_id=$(sed -n 's/^project_id *= *"\(.*\)"/\1/p' "$workdir/supabase/config.toml")
container="supabase_storage_${project_id}"
config=$(docker inspect "$container" 2>/dev/null | jq '.[0]') || {
    echo "error: $container not found. Run 'supabase start --workdir $workdir' first." >&2
    exit 1
}

if jq -e '.Config.Env | index("ICEBERG_ENABLED=true")' <<<"$config" >/dev/null; then
    echo "ICEBERG_ENABLED is already set on $container."
else
    args=()
    while IFS= read -r line; do args+=("$line"); done < <(jq -r '
        (.Config.Env[] | select(startswith("ICEBERG_ENABLED=") | not) | "--env", .),
        (.Config.Labels // {} | to_entries[] | "--label", "\(.key)=\(.value)"),
        (.Mounts[] | "--volume", "\(if .Type == "volume" then .Name else .Source end):\(.Destination)"),
        (.NetworkSettings.Networks | to_entries[0] | ("--network", .key), (.value.Aliases // [] | .[] | "--network-alias", .))
    ' <<<"$config")

    echo "Recreating $container with ICEBERG_ENABLED=true..."
    docker rm --force "$container" >/dev/null
    docker run --detach --name "$container" --env ICEBERG_ENABLED=true "${args[@]}" "$(jq -r '.Config.Image' <<<"$config")" >/dev/null
fi

# Wait until the iceberg routes answer through the API gateway
status=$(supabase status --workdir "$workdir" --output json 2>/dev/null)
api_url=$(jq -r '.API_URL' <<<"$status")
key=$(jq -r '.SERVICE_ROLE_KEY' <<<"$status")
for _ in $(seq 30); do
    code=$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $key" "$api_url/storage/v1/iceberg/bucket" || true)
    if [[ "$code" == "200" ]]; then
        echo "Analytics bucket API is available at $api_url/storage/v1/iceberg."
        exit 0
    fi
    sleep 2
done
docker logs --tail 20 "$container" >&2
echo "error: analytics bucket API did not become available (last status: $code)." >&2
exit 1

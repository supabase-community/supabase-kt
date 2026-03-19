# Manual Testing: OAuth Client Admin API

## Prerequisites

```bash
brew install supabase/tap/supabase
```

## Steps

### 1. Start local Supabase

```bash
mkdir /tmp/supabase-test && cd /tmp/supabase-test
supabase init
supabase start
```

Note the `API URL` and `service_role key` from the output.

### 2. Verify endpoint exists

```bash
SERVICE_KEY="<service_role_key>"

curl -s -o /dev/null -w "%{http_code}" \
  http://localhost:54321/auth/v1/admin/oauth/clients \
  -H "Authorization: Bearer $SERVICE_KEY" \
  -H "apikey: $SERVICE_KEY"
```

- 401 or 200 = endpoint exists, proceed
- 404 = GoTrue version too old, run `supabase update` and restart

### 3. Test CRUD with curl

```bash
# Create
curl -X POST http://localhost:54321/auth/v1/admin/oauth/clients \
  -H "Authorization: Bearer $SERVICE_KEY" \
  -H "apikey: $SERVICE_KEY" \
  -H "Content-Type: application/json" \
  -d '{"client_name":"Test App","redirect_uris":["https://example.com/cb"]}'

# List
curl http://localhost:54321/auth/v1/admin/oauth/clients \
  -H "Authorization: Bearer $SERVICE_KEY" \
  -H "apikey: $SERVICE_KEY"

# Get (replace CLIENT_ID)
curl http://localhost:54321/auth/v1/admin/oauth/clients/CLIENT_ID \
  -H "Authorization: Bearer $SERVICE_KEY" \
  -H "apikey: $SERVICE_KEY"

# Update
curl -X PUT http://localhost:54321/auth/v1/admin/oauth/clients/CLIENT_ID \
  -H "Authorization: Bearer $SERVICE_KEY" \
  -H "apikey: $SERVICE_KEY" \
  -H "Content-Type: application/json" \
  -d '{"client_name":"Renamed App"}'

# Regenerate secret
curl -X POST http://localhost:54321/auth/v1/admin/oauth/clients/CLIENT_ID/regenerate_secret \
  -H "Authorization: Bearer $SERVICE_KEY" \
  -H "apikey: $SERVICE_KEY"

# Delete
curl -X DELETE http://localhost:54321/auth/v1/admin/oauth/clients/CLIENT_ID \
  -H "Authorization: Bearer $SERVICE_KEY" \
  -H "apikey: $SERVICE_KEY"
```

### 4. Verify response shape

Compare the JSON response fields against `OAuthClient.kt` data class.
Check for any missing/extra fields or naming mismatches.

### 5. Cleanup

```bash
cd /tmp/supabase-test
supabase stop
```

## What to verify

- [ ] All 6 endpoints return expected status codes
- [ ] Response JSON matches `OAuthClient` serialization (field names, types)
- [ ] `token_endpoint_auth_method` is present in responses (issue #1205)
- [ ] Null/optional fields are handled correctly
- [ ] Pagination params (`page`, `per_page`) work on list endpoint

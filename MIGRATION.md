# Migrating from version 2.X to 3.0.0

### Ktor 3

Starting with `2.7.0`, supabase-kt now uses Ktor 3. This brings WASM support, but projects using Ktor 2 will be incompatible.
Ktor `3.0.0-rc-1` or later has to be used.

### Rename gotrue-kt to auth-kt and rename the package name

- **The `gotrue-kt` module is no longer being published starting with version `3.0.0`. Use the new `auth-kt` module.**
- **Rename `auth-kt` package name from `io.github.jan.supabase.gotrue` to `io.github.jan.supabase.auth`**.

### New plugin & Kotlin targets

- New [coil3-integration](/plugins/Coil3Integration)
- New wasm-js support for almost all plugins

### File uploading & downloading 

- Each uploading method (upload, update, uploadAsFlow ...) now has a `options` DSL. Currently you can configure three things:
1. Whether to upsert or not
2. The content type (will still be inferred like in 2.X if null)
3. Additional HTTP request configurations
   Example:
```kotlin
supabase.storage.from("test").upload("test.txt", "Hello World!".encodeToByteArray()) {
    contentType = ContentType.Text.Plain
    upsert = true
}
```
- Each downloading method (downloadPublic, downloadAuthenticated, downloadPublicAsFlow, ...) now has a `options` DSL. Currently you can only configure the image transformation
  Example:
```kotlin
supabase.storage.from("test").downloadAuthenticated("test.jpg") {
    transform {
        size(100, 100)
    }
}
```

### Postgrest Changes

- Move all optional function parameters for `PostgrestQueryBuilder#select()`, `insert()`, `upsert()` and `Postgrest#rpc()` to the request DSL
  Example:
```kotlin
supabase.from("table").upsert(myValue) {
    defaultToNull = false
    ignoreDuplicates = false
}
```
- Move the non-parameter variant of `Postgrest#rpc()` to the `Postgrest` interface. It was an extension function before
- Add a non-generic parameter variant of `Postgrest#rpc()` to the `Postgrest` interface. This function will be called from the existing generic variant

### Realtime changes

- `RealtimeChannel#presenceChangeFlow` is now a member function of `RealtimeChannel`. (It was an extension function before)

### Apollo GraphQL plugin

The Apollo GraphQL plugin now uses Apollo GraphQL 4.0.0.

<details>
<summary>Migrating from version 1.4.X to 2.0.0</summary>

# Migrating from version 1.4.X to 2.0.0

## GoTrue

The GoTrue module had a lot of changes including many renames:
- Rename `GoTrue` plugin to `Auth`
- Rename `GoTrueConfig` to `AuthConfig`
- Rename `SupabaseClient#gotrue` to `SupabaseClient#auth`
- Rename `Auth#loginWith` to `Auth#signInWith`
- Rename `Auth#logout` to `Auth#signOut`
- Rename `LogoutScope` to `SignOutScope`
- Rename `AdminUserUpdateBuilder#phoneNumber` to `AdminUserUpdateBuilder#phone`
- Rename `UserUpdateBuilder#phoneNumber` to `UserUpdateBuilder#phone`
- Rename `Phone.Config#phoneNumber` to `Phone.Config#phone`
- Rename `Auth#sendRecoveryEmail` to `Auth#resetPasswordForEmail`

#### The process of signing in with an OTP & SSO has also been refactored. 
#### There is now a new `OTP` Auth Provider which does the same as the old `sendOtpTo`:

Old:
```kotlin
supabase.gotrue.sendOtpTo(Email) {
    email = "example@email.com"
}
//or
supabase.gotrue.sendOtpTo(Phone) {
    phoneNumber = "+123456789"
}
```

New:
```kotlin
supabase.auth.signInWith(OTP) {
    email = "example@email.com"
    //or
    phone = "+123456789"
}
```

#### Similarly, the SSO Auth Provider was also refactored to match the other Auth Providers:

Old:
```kotlin
supabase.gotrue.loginWith(SSO.withProvider("provider"))
//or
supabase.gotrue.loginWith(SSO.withDomain("domain"))
```

New:
```kotlin
supabase.auth.signInWith(SSO) {
    providerId = "providerId"
    //or
    domain = "domain"
}
```

## Realtime

The Realtime module also had a few renames:
- Rename `Realtime#createChannel` to `Realtime#channel`
- Remove `RealtimeChannel#join` and add new `RealtimeChannel#subscribe` method, which does the same but also connects to the realtime websocket automatically
- Add `Realtime.Config#connectOnSubscribe` to disable this behaviour
- Rename `RealtimeChannel#leave` to `RealtimeChannel#unsubscribe`
- Add `SupabaseClient#channel` extension function delegating to `Realtime#channel`
- Rename `Realtime.Status` to reflect the new methods:
    - `UNSUBSCRIBED`
    - `SUBSCRIBING`
    - `SUBSCRIBED`
    - `UNSUBSCRIBING`

## Postgrest

The syntax for interacting with the PostgREST API has been refactored significantly. Each database method (`SELECT`, `UPDATE`, etc.)
now have a new builder and most of the properties which were a method parameter are now in this builder.

The filters now get applied within a `filter {}` block.

### **Examples:**

**Select**

Old:
```kotlin
supabase.postgrest.from("countries").select(count = Count.EXACT) {
    eq("id", 1)
}
```

New:
```kotlin
supabase.postgrest.from("countries").select {
    count(Count.EXACT)
    filter {
        eq("id", 1)
    }
}
```

**Insert**

Old:
```kotlin
supabase.postgrest.from("countries").update(country, returning = Returning.REPRESENTATION) { //Returning is representation by default
    eq("id", 1)
}
```

New:
```kotlin
supabase.postgrest.from("countries").update(country) {
    select() //Without this the "returning" parameter is `MINIMAL`, meaning you will not receive the data. 
    filter {
        eq("id", 1)
    }
}
```

The same applies for all other database methods. Additionally, new methods have been added to this builder:

Example:
```kotlin
val result = supabase.postgrest["messages"].select {
     single() //receive an object rather than an array
     count(Count.EXACT) //receive amount of database entries
     limit(10) //limit amount of results
     range(2, 3) //change range of results
     select() //return the data when updating/deleting/upserting (same as settings 'returning' to REPRESENTATION before)
     csv() //Receive the data as csv
     geojson() //Receive the data as geojson
     explain(/* */) //Debug queries
     filter {
          eq("id", 1)
     }
}
```

## Compose Auth

Compose Auth also had some renames:
- Rename `ComposeAuth#rememberLoginWithGoogle` to `ComposeAuth#rememberSignInWithGoogle`
- Rename `ComposeAuth#rememberLoginWithApple` to `ComposeAuth#rememberSignInWithApple`
- Rename `ComposeAuth#rememberSignOut` to `ComposeAuth#rememberSignOutWithGoogle`

Additionally, Native Google Auth on Android will now use the Credential Manager for Android 14+ devices once again.

</details>
# Changelog

### 3.0.0 - 27.09.2024


# Ktor 3

Starting with `2.7.0`, supabase-kt now uses Ktor 3. This brings WASM support, but projects using Ktor 2 will be incompatible.
Ktor  `3.0.0-rc-1` is used in this release.

# Changes

### Rename gotrue-kt to auth-kt and rename the package name

- **The `gotrue-kt` module is no longer being published starting with version `3.0.0`. Use the new `auth-kt` module.**
- **Rename `auth-kt` package name from `io.github.jan.supabase.gotrue` to `io.github.jan.supabase.auth`**.

### Support for WASM-JS

New `wasm-js` target for **supabase-kt**, **auth-kt**, **storage-kt**, **functions-kt**, **postgrest-kt**, **realtime-kt**, **compose-auth**, **compose-auth-ui**, **apollo-graphql** and the new **coil3-integration** by @jan-tennert in #311

### New plugin: coil3-integration

Support for Coil3 and all Compose Multiplatform targets under a new plugin by @jan-tennert in #428. [Checkout the documentation](https://github.com/supabase-community/supabase-kt/tree/master/plugins/Coil3Integration).
**The "old" coil 2 integration is still available and hasn't changed.**

### Auth

- Remove `Auth#modifyUser()`
- Remove `MfaApi#loggedInUsingMfa`, `MfaApi#loggedInUsingMfaFlow`, `MfaApi#isMfaEnabled`, `MfaApi#isMfaEnabledFlow`
- Refactor SessionStatus by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/725
  - Move `SessionStatus` to its own `status` package
  - Rename `SessionStatus#LoadingFromStorage` to `SessionStatus#Initializing`
  - Rename and refactor `SessionStatus#NetworkError` to `SessionStatus#RefreshFailure(cause)`
    *Note: The cause can be either `RefreshFailureCause#NetworkError` or `RefreshFailureCause#InternalServerError`. In both cases the refreshing will be retried and the session not cleared from storage. During that time, the session is obviously not usable.*

### Apollo GraphQL

- Migrate to Apollo GraphQL 4.0.0 by @jan-tennert in #692

### Storage

**Rework the uploading & downloading methods by @jan-tennert in #729**
- Each uploading method (upload, update, uploadAsFlow ...) now has a `options` DSL. Currently, you can configure three things:
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

### Postgrest

- Move all optional function parameters for `PostgrestQueryBuilder#select()`, `insert()`, `upsert()` and `Postgrest#rpc()` to the request DSL by @jan-tennert in #716
  Example:
```kotlin
supabase.from("table").upsert(myValue) {
    defaultToNull = false
    ignoreDuplicates = false
}
```
- Move the non-parameter variant of `Postgrest#rpc()` to the `Postgrest` interface. It was an extension function before by @jan-tennert in #726
- Add a non-generic parameter variant of `Postgrest#rpc()` to the `Postgrest` interface. This function will be called from the existing generic variant by @jan-tennert in #726
- Add a `schema` property to the `Postgrest#rpc` DSL by @jan-tennert in #716

### Realtime

- Refactor internal event system for maintainability and readability by @jan-tennert #696
- `RealtimeChannel#presenceChangeFlow` is now a member function of `RealtimeChannel`. (It was an extension function before) by @jan-tennert in #697
- Move the implementation for `RealtimeChannel#broadcastFlow` and `RealtimeChannel#postgresChangeFlow` to a member function of `RealtimeChannel`. (Doesn't change anything in the public API) by @jan-tennert in #697
- Make the setter of `PostgresChangeFilter` private

## 2.6.0 - August 16, 2024

### Core

- Update Kotlin to `2.0.10`

### Postgrest

- Expose `headers` and `params` in `PostgrestRequestBuilder` by @jan-tennert in #689

You can now set custom headers & url parameters while making a postgrest request

### Auth

- **Add support for third-party auth by @jan-tennert in #688**
  
  You can now use third-party auth providers like Firebase Auth instead of Supabase Auth by specifying a `AccessTokenProvider` in the `SupabaseClientBuilder`:
    ```kotlin
    val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
        accessToken = {
              //fetch the third party token
             "my-token"
        }
    }
    ```
  This will be used for the `Authorization` header and other modules like Realtime and Storage integrations!
  **Note:** The `Auth` plugin cannot be used in combination and will throw an exception if used when setting `accessToken`.
- **Changes to Multi-Factor-Authentication by @jan-tennert in #681**
  - Refactor the syntax for enrolling MFA factors and add support for the Phone factor type:
    ```kotlin
    //Enrolling a phone factor
    val factor = client.auth.mfa.enroll(FactorType.Phone, friendlyName) {
        phone = "+123456789"
    }
    
    //Enrolling a TOTP factor
    val factor = client.auth.mfa.enroll(FactorType.TOTP, friendlyName) {
        issuer = "Issuer"
    }
    ```
  - Add a `channel` parameter to `MfaApi#createChallenge` to allow sending phone MFA messages to either `SMS` or `WHATSAPP`.
  - Deprecate `MfaApi#loggedInUsingMfa` and `MfaApi#isMfaEnabled` & their flow variants in favor of `MfaApi#status` and `MfaApi#statusFlow`:
    ```kotlin
    val (enabled, active) = client.auth.mfa.status
    
    //Flow variant
    client.auth.mfa.statusFlow.collect { (enabled, active) ->
        processChange(enabled, active)
    }
    ```
- Add `SlackOIDC` `OAuthProvider` by @jan-tennert in #688

### Realtime

- Remove client-side rate-limiting in #678 by @jan-tennert 
- Fix broadcasting to a private channel via the HTTP API in #673 by @jan-tennert 
- Fix callbacks not getting removed correctly in the `CallbackManager` in #673 by @jan-tennert 
- Change internal realtime implementation to be more robust and testable in #673 by @jan-tennert 
  - Add `Realtime.Config#websocketFactory`: This is highly internal and should be only modified if you know what you are doing

### Storage

- The `StorageApi#authenticatedRequest` method is now suspending
- All uploading methods will now return a `FileUploadResponse` instead of a `String`, which includes the actual path and some other properties.

## 2.5.4 - July 27, 2024

### Realtime
- Fix deletion in `postgresListDataFlow` not working correctly by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/671

## 2.5.3 - July 21, 2024

### Auth

- Add support for verifying a token hash by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/657

### Miscellaneous
- Add link to a RESTful service sample built with supabase-kt by @hieuwu in https://github.com/supabase-community/supabase-kt/pull/664

## 2.5.2 - July 08, 2024

### Realtime
- fix(realtime): add private to Realtime by @grdsdev in https://github.com/supabase-community/supabase-kt/pull/642
Add support for specifying if a channel is private via `RealtimeChannelBuilder#isPrivate`

### Compose Auth
- Fix Google sign in prompt in composeAuth by @Aaron246 in https://github.com/supabase-community/supabase-kt/pull/648
- fix(auth): fix sign in with Apple in ComposeAuth by @grdsdev in https://github.com/supabase-community/supabase-kt/pull/654

## 2.5.1 - June 28, 2024

### Auth

- Ignore certain status codes when signing out a user by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/633
  Failure of signing out because of invalid/expired JWTs will now be ignored and the local session cleared.

## 2.5.0 - June 9, 2024

### Auth
* Add error code enum for Auth API errors by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/618
  All rest errors containing a `error_code` field will now throw a `AuthRestException` rather than generic `BadRequestExceptions`, etc.
  `AuthRestException`s contain a `errorCode` field of the type `AuthErrorCode` containing all known error codes.
  Two error codes have their own exceptions (but obviously inherit from `AuthRestException)`: `AuthWeakPasswordException` and `AuthSessionMissingException`.
  API errors not containing this field will throw the generic exceptions.

* Handle `weak_password` and `session_not_found` auth error codes by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/596
  There is now a new subclass of `RestException`: `AuthRestExcepton` which will be a super class for exceptions based on error codes. Currently, there are two new exceptions: `AuthWeakPasswordException` and `AuthSessionMissingException`
* Fix/improve session expiry calculation by @JOsacky in #610

### Postgrest

* Add columns header to `PostgrestQueryBuilder#insert` by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/611
  This fixes an issue when inserting a list of objects where some objects might not have all keys.
* Add `defaultToNull` parameter to `PostgrestQueryBuilder#insert`

### Realtime

* Use `Postgrest`s `propertyConversionMethod` for getting the property name of primary keys
* Add the possibility to have a multi-column PK in realtime by @iruizmar in https://github.com/supabase-community/supabase-kt/pull/614
* Fix presences updates for `RealtimeChannel#presenceDataFlow` working incorrectly by @JOsacky in https://github.com/supabase-community/supabase-kt/pull/607

### Compose Auth Ui

* Add `isError` parameter to Compose Auth Ui fields by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/612
* Add AuthUiExperimental annotation by @iruizmar in https://github.com/supabase-community/supabase-kt/pull/603
  All composables now have a `AuthUiExperimental` annotation instead of `SupabaseExperimental`

### Misc

* Update README.md by @rafalgawlik in https://github.com/supabase-community/supabase-kt/pull/605

## 2.4.3 - June 3, 2024

### Auth

- Fix a major bug causing sessions to be refreshed later than they should be in https://github.com/supabase-community/supabase-kt/commit/af9143d6f32c9c97c38870e8bdd20863edcbc38e by @jan-tennert
  _The `expiresAt` property was saved incorrectly, delaying the expiry date._

## 2.4.2 - May 19, 2024

### Auth

- Remove OtpType deprecation notice and clarify documentation by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/595
  The `SIGNUP` and `MAGIC_LINK` `OtpType`s are no longer deprecated as they are used for resending OTPs.
- Store session before emitting Authenticated state by @iruizmar in https://github.com/supabase-community/supabase-kt/pull/600
  This fixes a bug where it's possible to cancel any sign-in method and the `sessionStatus` gets set to `Authenticated` without the session actually saving to storage due to the cancellation.

## 2.4.1 - May 11, 2024

### Compose Auth UI

* Fix "Login in with" typo by @JOsacky in https://github.com/supabase-community/supabase-kt/pull/588

### Postgrest
* Include columns in upsert request & change default parameter value for `defaultToNull` to `true` by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/590
  This fixes an issue when upserting a list of objects where some objects might not have all keys.

## 2.4.0 - May 9, 2024

### Core

- Update Kotlin to **2.0.0-RC2** and Compose to **1.6.10-rc01** by @jan-tennert in #581
- Rethrow `CancellationException`s in network requests by @jan-tennert in #578
- Log the actual error if a network request fails by @jan-tennert in https://github.com/supabase-community/supabase-kt/commit/0c93053e9ddb192f7238d719617d96eb71702bfd

### Auth

- Auth changes & fixes by @jan-tennert in #568
    - The method `Auth#linkIdentity` will now return the OAuth URL if the config value `ExternalAuthConfigDefaults.automaticallyOpenUrl` is set to false.
      Otherwise, null.
    - Fix the `autoRefresh` default value for `Auth#importSession` not being set to `config.alwaysAutoRefresh`
    - Add `codeVerifier` parameter to `MemoryCodeVerifierCache` to be able to set an initial value
    - Add missing `captchaToken` config option in the `OTP` auth provider
- Change the default session key for the `SettingsSessionManager` to work with multiple instances on the same device and add a `key` parameter to the constructor in case you want a custom key by @MohamedRejeb in #572

### Realtime

- Add new experimental extension functions to retrieve initial data and listen for updates without using realtime channels by @jan-tennert #579:
```kotlin
//Not a suspending function, subscribing and unsubscribing is handled internally
val myProductFlow: Flow<Product> = supabase.from("products").selectSingleValueAsFlow(Product::id) {
    Product::id eq 2
}.collect {
    println(it)
}
```
```kotlin
val productsFlow: Flow<List<Product>> = supabase.from("products").selectAsFlow(Product::id, filter = FilterOperation("id", FilterOperator.GT, 2)).collect {
    println(it)
}
```
This requires both `Realtime` and `Postgrest` to be installed within the SupabaseClient.

### Storage

- Prohibit uploading empty data to a bucket by @hieuwu in #577

### Functions

- Add new `region` parameter to invoke functions which allows changing the region where the Edge Function will be invoked in. Defaults to `Functions.Config#defaultRegion` (which is `FunctionRegion.ANY`) by @jan-tennert in #580 

## 2.3.1 - April 20, 2024

### Storage

- Make the default value for `Storage.Config.resumable#cache` `null` to prevent a NullPointerException in testing enviroments. (If it's set to null, the default cache implementation will be used)
- Fix `BucketApi#createSignedUploadUrl` returning an invalid url
- Fix `BucketApi#createSignedUrl` not correctly adding transformation parameters
- Fix `BucketApi#createSignedUrl` returning an invalid url
- Fix `BucketListFilter#sortBy` not working correctly
- Add missing `destinationBucket` parameter to `BucketApi#copy` and `BucketApi#move` by @jan-tennert in #562

### Auth

- Move `enableLifecycleCallbacks` to the common source set (still only used on Android) by @jan-tennert in #561
- Add `AuthConfig#minimalSettings` (useful for servers or testing, disabling session storage and uses only in-memory caches) by @jan-tennert in #561

### Realtime

- Remove the suspend modifier for `RealtimeChannel#postgresListDataFlow` by @jan-tennert in #563
- When a postgres flow is closed, the joining payload will now be correctly cleaned by @jan-tennert in #563

## 2.3.0 - April 15, 2024

### Core

- Update Kotlin to `2.0.0-RC1`

### Postgrest

- Add referenced table parameter to `or` and `and` by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/551
- Fix nested `or` blocks working incorrectly
- Auto remove line breaks and white spaces when unquoted on Columns.Raw by @iruizmar in https://github.com/supabase-community/supabase-kt/pull/544
- Add support for using GET when calling database functions by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/538
  There is now a `method` parameter which takes in a `RpcMethod`. This can either be `POST`, `HEAD` or `GET`. The head parameter has been removed.

### Realtime

- Stabilize new flow presence and postgres methods by @jan-tennert in #535

### Auth

- Stabilize linking identities and signing in anonymously by @jan-tennert in #535
- Rename `Auth#modifyUser` to `Auth#updateUser` by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/534
- Return full a full `UserInfo` on sign up by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/528
  The `signUpWith` method will now return a `UserInfo` object for the `Email`, `Phone` and `IDToken` instead of separate Result objects.
- Allow customizing the custom tab intent on Android by @jan-tennert in https://github.com/supabase-community/supabase-kt/pull/529



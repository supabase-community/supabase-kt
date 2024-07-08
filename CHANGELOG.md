# Changelog

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



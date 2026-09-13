# Road to official / next major version

This document holds suggested refactors that can be done in the transition of migrating the library to an official one. A lot of them come from the community. 
Features which are unlikely are highlighted as such.

## Core

### Separate ID value classes

Currently, all IDs (UID, bucket id, etc.) are held as a string. This makes them hard to (1) distinguish (2) ensure type safety. 
Also, if IDs need additional transformations (like ensuring correct URL encoding), a separate class would make this cleaner then using a helper method each time.

### Find a better way for (rest) error/exception handling; also leaking exceptions

Currently, it can be very hard to guess which exception is thrown when the API returns an error response. Some modules like Auth and Storage have specific error codes which are the most useful for devs, 
but these are often in specific `RestException` subtypes like `AuthRestException`. Also, the whole `RestException` structure is very unclear. Generally options would be:
1. Keep exceptions but make the hierarchy more explicit and easier to understand.
2. Use a JS style approach with `val (result, error) = table.select(...)`, where result is null if there is an error
3. Use a sealed-class style approach with, e.g.:
```kotlin
when(val result = table.select(...)) {
    is Success -> println(result.data)
    is RestError -> // handle
    // maybe other types like network errors etc.    
}
```

Second, there have been multiple instances where exceptions like serialization errors leak through the API and are even harder to handle as a dev.

### Unify method/class naming and also DSL approach

Specifically DSL builders are named sometimes very differently like Builder, Options, Parameter. This should be revised and maybe unified. 
Also, a lot of them use a different approach like some builders are serializable classes, while some have a distinct `build()` method for serialization. Im

### Immutable client config

Currently, after creating the client, you can still modify each plugin's config, which shouldn't be done and can lead to unexpected behavior.

## Auth

### Reconsider auto refresh

Currently, the auto refresh works via coroutines and `delay`. This works but is not really ideal especially for lifecycle driven platforms like Android, iOS where it would be better to use alternatives.
Also, this approach currently has rare bugs which can cause the session to expire anyway. Alternatives might be
1. Lifecycle-driven session management / system aware work schedulers: Makes it possible to better refresh the session in the background
2. Check the expiration dynamically before a request. This is kinda done but only if the session expired. Then the client forces a refresh.

### Separate `auth` and `auth-native` modules. This text is copied from #1291. This approach has been favorised by the community.
**`auth-kt` really only provides:**
  (1) Methods to call the Supabase API (now with split `auth.signInWithPassword()`, `auth.signInWithOtp()`, `auth.signInWithIdToken()`, etc for SDK parity)
  (2) Handle session management, if not disabled (this PR also makes all API related methods return a `UserSession` and also an option to disable the session importing into memory, so it is even easier for server-side applications)

**`auth-native` provides:**
- Seamless platform specific implementations for OAuth, Native Auth, Passkeys, Lifecycle Management, Deeplinks (of depending on the platform)

The only thing you have to do:
```kotlin
install(Auth.withNative())
```

Note that the base `auth-kt` doesn't have any of these methods, since they would need platform specific dependencies. 
If users decide to make their own implementation they can just use `auth.getOAuthUrl(...)` and `auth.passkeys`

### New syntax (more aligned with the other SDKs)

**Sign in [With Email() or Phone()]**
```kotlin
val session = supabase.auth.signInWithPassword(Email("myEmail@example.com"), password = "123345") {
        // ...
}
```

**Sign in with OTP**
```kotlin
supabase.auth.signInWithOtp(Email("myEmail@example.com")) {
      // ...
}
```

**Sign up [with Email() or Phone()]**
```kotlin
val response = supabase.auth.signUp(Email("myEmail@example.com"), password = "12345") {
    // ...
}
println(response.session)
println(response.user)
```

**Sign in with ID Token**
```kotlin
val session = supabase.auth.signInWithIdToken(OAuthProviders.GOOGLE, token = "12343") {
    // ...
}
```
__**Native-Auth module:**__
**Sign in with OAuth**
```kotlin
supabase.auth.signInWithOAuth(OAuthProviders.GOOGLE) {
    // ...
}
```

**Linking identity**
```kotlin
supabase.auth.linkIdentity(OAuthProviders.GOOGLE) {
    // ...
}
```

**Sign in with SSO**
```kotlin
supabase.auth.signInWithSSO(SSODomain("domain")) 
supabase.auth.signInWithSSO(SSOProvider("providerId"))
```

**Native Google Auth (or OAuth as a fallback)**
```kotlin
// required for Android:
install(Auth.withNative()) {
    nativeAuth {
         googleClientId = "..."
    }
}
val result = supabase.auth.signWithGoogle()
when(result) {
    GoogleSignInResult.Cancelled -> TODO()
    GoogleSignInResult.OAuthInitiated -> TODO()
    is GoogleSignInResult.Success -> TODO()
}
```

**Native Apple Auth (or OAuth as a fallback)**
```kotlin
val result = supabase.auth.signWithApple()
when(result) {
    AppleSignInResult.Cancelled -> TODO()
    AppleSignInResult.OAuthInitiated -> TODO()
    is AppleSignInResult.Success -> TODO()
}
```

### Android / iOS OAuth related changes

**Android**

On Android, the only thing you have to do now is set the app scheme and optionally the host in the build configuration:
```kotlin
android {

    defaultConfig {
        manifestPlaceholders["supabaseAuthScheme"] = "myScheme"
        // optional: 
        manifestPlaceholders["supabaseAuthHost"] = "myHost"
    }
    
}
```

The library will take care of everything else. No more `handleDeeplinks()` or something similar. The library will also catch magic links.

**iOS**

On iOS, `ASWebAuthenticationSession` is now used for OAuth, fixing #1253 and also making the process more iOS idiomatic. For magic links / deeplinks from outside the app, you still have to use `handleDeeplinks()`, but with a new method for better visbility in Swift (`SupabaseDeeplinkHandlerKt.handleDeeplinks()`. The scheme entry in `PList.info` is still required as is setting the scheme in the config:

```
install(Auth) {
     appScheme = "myScheme" 
}
```

## PostgREST

### More idiomatic column specification

Currently, you have to provide a raw string (e.g. `"id,name,age"`) which is prone to errors especially if using a more complex expression. Solutions might be
1. #761
2. #769

## Storage

### More functional downloads/uploads

Currently, the uploading and downloading methods are very restricted. E.g. you can listen to the progress of the upload/download.
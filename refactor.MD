# Road to official / next major version

This document holds suggested refactors that can be done in the transition of migrating the library to an official one. A lot of them come from the community. Features which are unlikely are highlighted as such.

## Auth

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
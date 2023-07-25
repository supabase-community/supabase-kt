# Android Login Demo

This is a demo of an android app using Google OneTap/Native Sign-In & WebView for authentication

**Available platforms:** Android

**Modules used:** GoTrue

https://user-images.githubusercontent.com/26686035/235766941-6a62c415-e07e-4d18-9706-0a246e6821eb.mp4

# Configuration

Then you need to specify your supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/android-login/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)

If you want the Native Google Sign In, create a OAuth-Client Id for a web application with the redirect url `https://SUPABASE_ID.supabase.co/auth/v1/callback` and a OAuth-Client Id for a android app with the packageName `io.github.jan.supabase.android` and your SHA1 fingerprint (you can get the fingerprint via the gradle task `signingReport`)
then also put in the `web` client id `supabaseModule.kt` (not the android one)

For Spotify login you obviously need to enable that in the Supabase Dashboard, but you can replace that with any provider you want

# Running

Use the IDE to run the app.

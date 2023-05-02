# Android Login Demo

This is a demo of an android app using Google OneTap/Native Sign-In & WebView for authentication

**Available platforms:** Android

**Modules used:** GoTrue


# Configuration

Then you need to specify your supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/chat-demo-mpp/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)
If you want the Native Google Sign In, also create OAuth credentials for the packageName `io.github.jan.supabase.android` your SHA1 fingerprint (you can get the fingerprint via the gradle task `signingReport`)
then also put it in `supabaseModule.kt`
For Spotify login you obviously need to enable that in the Supabase Dashboard, but you can replace that with any provider you want

# Running

Use the IDE to run the app.

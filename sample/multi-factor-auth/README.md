# Multi-Factor Authentication Demo

This is a sample integrating Multi-Factor Authentication with Supabase in a Compose Multiplatform app.

**Available platforms:** Android, Desktop, JS Canvas

**Modules used:** GoTrue

https://github.com/supabase-community/supabase-kt/assets/26686035/abe3eb3d-32f4-41f0-95de-6f01468711cb

# Configuration

You need to specify your Supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/multi-factor-authentication/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)
If you want to try out Google OAuth, set it up in the Supabase dashboard and add `io.jan.supabase://login` to the registered urls.

# Running

To run the app, you need to run the following commands in the root directory of the project:

    ./gradlew :sample:multi-factor-auth:desktop:runDistributable (Desktop)
    ./gradlew :sample:multi-factor-auth:web:jsBrowserDevelopmentRun (JS Canvas)

For android, use the IDE to run the app.

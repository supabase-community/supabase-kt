# Chat App Demo

This is a demo app of integrating multi-factor authentication with supabase in a compose multiplatform app.

**Available platforms:** Android, Desktop, JS Canvas

**Modules used:** GoTrue

# Configuration

You need to specify your supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/multi-factor-authentication/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)
If you want Google login to work, set it up in the supabase dashboard.

# Running

To run the app, you need to run the following commands:

    ./gradlew :desktop:runDistributable (Desktop)
    ./gradlew :web:jsBrowserDevelopmentRun (JS Canvas)

For android, use the IDE to run the app.

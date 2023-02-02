# Chat App Demo

This is a demo of a chat app using Compose Multiplatform, Koin and supabase-kt.
Available platforms: Android, Desktop, JS Canvas

# Configuration

To run the app, you need to create a supabase project and create a table called `messages` with the following columns:

![img.png](img.png)

Then you need to specify your supabase url and key in [supabaseModule.kt](src/commonMain/kotlin/com/example/chatapp/supabaseModule.kt)

# Running

To run the app, you need to run the following commands:

    ./gradlew :desktop:runDistributable
    ./gradlew :web:jsBrowserDevelopmentRun

On android use the IDE to run the app.
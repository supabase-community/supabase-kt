# Chat App Demo

This is a demo of a chat app using Compose Multiplatform, Koin and supabase-kt.

**Available platforms:** Android, Desktop, JS Canvas

**Modules used:** Realtime, GoTrue and Postgrest 


https://user-images.githubusercontent.com/26686035/216403906-b190423d-1df3-497c-8091-66df85558fa5.mp4


# Configuration

To run the app, you need to create a supabase project and create a table called `messages` with the following columns:

![image](https://user-images.githubusercontent.com/26686035/216403760-067b563f-621c-435e-887b-0ef2086854a1.png)

Then you need to specify your supabase url and key in [supabaseModule.kt](src/commonMain/kotlin/com/example/chatapp/supabaseModule.kt)

# Running

To run the app, you need to run the following commands:

    ./gradlew :desktop:runDistributable (Desktop)
    ./gradlew :web:jsBrowserDevelopmentRun (JS Canvas)

For android, use the IDE to run the app.

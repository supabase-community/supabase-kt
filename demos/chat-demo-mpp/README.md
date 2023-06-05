# Chat App Demo

This is a demo of a chat app using Compose Multiplatform, Koin and supabase-kt.

**Available platforms:** Android, Desktop, JS Canvas

**Modules used:** Realtime, GoTrue and Postgrest 

https://user-images.githubusercontent.com/26686035/216710629-d809ff58-cd3b-449f-877f-4c6c773daec4.mp4


# Configuration

To run the app, you need to create a supabase project and create a table called `messages` with the following columns:

![image](https://user-images.githubusercontent.com/26686035/216403760-067b563f-621c-435e-887b-0ef2086854a1.png)

Then you need to specify your supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/chat-demo-mpp/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)
If you want Google login to work, set it up in the supabase dashboard and add `io.jan.supabase://login` to the registered urls.

# Running

To run the app, you need to run the following commands:

    ./gradlew :desktop:runDistributable (Desktop)
    ./gradlew :web:jsBrowserDevelopmentRun (JS Canvas)

For android, use the IDE to run the app.

# Chat App Multiplatform

This is a demo of a chat app using Compose Multiplatform, Koin and supabase-kt.

**Available platforms:** Android, iOS, Desktop, JS Canvas

**Modules used:** Realtime, Auth*, Postgrest, Compose Auth UI

* Integrated flows: Password, Google login & password recovery

https://user-images.githubusercontent.com/26686035/216710629-d809ff58-cd3b-449f-877f-4c6c773daec4.mp4

# Configuration

To run the app, you need to create a Supabase project and create a table called `messages` with the following columns:

![image](https://user-images.githubusercontent.com/26686035/216403760-067b563f-621c-435e-887b-0ef2086854a1.png)

Then you need to specify your Supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/chat-demo-mpp/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)
If you want Google login to work, set it up in the Supabase dashboard and add `io.jan.supabase://login` to the registered urls.

# Running
To run the app, you need to run the following commands in the root directory of the project:

    ./gradlew :sample:chat-demo-mpp:desktop:runDistributable (Desktop)
    ./gradlew :sample:chat-demo-mpp:web:jsBrowserDevelopmentRun (JS Canvas)

For android, use the IDE to run the app.

# iOS Development Setup
Set up with XCode 15 and run the app on iOS 17
Open the project with workplace file (`chatdemoios.xcworkspace`)

Open project settings
Go to `Build phases` section, create New Run Script Phase with this value
```cmd
cd "$SRCROOT/.."
./gradlew :sample:chat-demo-mpp:common:embedAndSignAppleFrameworkForXcode
```
<img width="650" alt="Screenshot 2023-12-29 at 13 02 54" src="https://github.com/hieuwu/supabase-kt/assets/43868345/9563bd68-96c9-4e98-b38a-d03bd595e413">

Then move run script to be above `Compile Sources`
<img width="650" alt="Screenshot 2023-12-29 at 13 03 00" src="https://github.com/hieuwu/supabase-kt/assets/43868345/1ccb1949-f27a-452a-a3eb-b82cb8f187c2">

Switch to `Build Settings` and set these values
- **Framework Search Paths** - `$(SRCROOT)/../common/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`

- **Other linker flags** - `$(inherited) -framework common`

<img width="650" alt="Screenshot 2023-12-29 at 12 59 40" src="https://github.com/hieuwu/supabase-kt/assets/43868345/9467e58a-a28c-4973-ab95-61f13b756b44">
<img width="630" alt="Screenshot 2023-12-29 at 12 59 53" src="https://github.com/hieuwu/supabase-kt/assets/43868345/1cf30293-6957-4ae3-a27f-6815936a0c2b">

At this point, the app can be run either with Android Studio with iOS configuration or XCode

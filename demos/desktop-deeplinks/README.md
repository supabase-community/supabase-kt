# Multi-Factor Authentication Demo

This is a demo of integrating deeplink authentication with Supabase on Desktop and Android.

On Desktop it uses [Conveyor](https://conveyor.hydraulic.dev/9.2/) to create an installer and register the url protocol.
To allow the app to have a single instance running, it uses [unique4j](https://github.com/prat-man/unique4j). See [Main.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/desktop-deeplinks/desktop/src/main/kotlin/Main.kt) to learn more.

**Available platforms:** Android, Desktop

**Modules used:** GoTrue

# Configuration

You need to specify your supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/desktop-deeplinks/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)
If you want Google login to work, set it up in the supabase dashboard.

### Building

On Android just build the APK with the IDE.

On Desktop you have to install [Conveyor](https://conveyor.hydraulic.dev/9.2/) and run `conveyor make site` in `/desktop`. 

Then you can serve the download site e.g. with npx: `npx serve desktop/output`

To test it, go to **localhost:3000/download** and download the installer. This will register the url protocol and install the app.

# Running

To run the app, you need to run the following commands:

    ./gradlew :desktop:runDistributable (Desktop)

For android, use the IDE to run the app.

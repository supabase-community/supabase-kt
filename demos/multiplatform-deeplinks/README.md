# Multiplatform Deeplinking Demo

This is a demo of integrating deeplink authentication with Supabase on Desktop and Android.

**Available platforms:** Android, Desktop

**Modules used:** GoTrue

## How it works (Desktop)

On Desktop it uses [Conveyor](https://conveyor.hydraulic.dev/9.2/) to create an installer which registers the url protocol on installation.
To allow the app to have a single instance running, this demo uses [unique4j](https://github.com/prat-man/unique4j).

Unique4j uses a server to let different instance communicate with each other. If you are for example after you successfully log in with Google, you get redirected to the registered url protocol and a second instance opens. This second instance sends the data to the first instance and closes itself. (If there is another instance running) 

# Configuration

You need to specify your supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/multiplatform-deeplinks/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt).

If you want Google login to work, [set it up](https://supabase.com/docs/guides/auth/social-login) in the supabase dashboard and add `io.github.jan.supabase://login` to the registered urls.

### Building

On Android just build the APK with the IDE. Authentication should work without any additional setup.

On Desktop you have to install [Conveyor](https://conveyor.hydraulic.dev/9.2/) and run `conveyor make site` in `/desktop`. 

Then you can serve the download site e.g. with npx: `npx serve desktop/output`

To test it, go to **localhost:3000/download** and download the installer. This will register the url protocol and install the app.
After it installed, provider login and sign up should work without even having the application open.

**Note:** Desktop and Android use the same url protocol, so you users can open sign up emails on either platform.



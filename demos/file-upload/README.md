# File Upload Demo

This is a demo of a file upload demo using Compose Multiplatform, Koin and supabase-kt.

**Available platforms:** Android, Desktop

**Modules used:** Storage

*Video*


# Configuration

To run the app, you need to create a supabase project and create a public bucket with the permissions for anonymous users to upload files.

Then you need to specify your bucket id, supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/chat-demo-mpp/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)

# Running

To run the app, you need to run the following commands:

    ./gradlew :desktop:runDistributable (Desktop)

For android, use the IDE to run the app.

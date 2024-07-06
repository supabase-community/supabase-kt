# File Upload Demo

This is a demo of a file upload application using Compose Multiplatform, Koin and supabase-kt.

**Available platforms:** Android, Desktop

**Modules used:** Storage

https://user-images.githubusercontent.com/26686035/233403165-26d87217-a0c9-4abd-b677-c367a5f1e4b9.mp4

# Configuration

To run the app, you need to create a Supabase project and create a public bucket with the permissions for anonymous users to upload files.

Then you need to specify your bucket id, Supabase url and key in [supabaseModule.kt](https://github.com/supabase-community/supabase-kt/blob/master/demos/file-upload/common/src/commonMain/kotlin/io/github/jan/supabase/common/di/supabaseModule.kt)

# Running

To run the app, you need to run the following commands:

    ./gradlew :sample:file-upload:desktop:runDistributable (Desktop)

For android, use the IDE to run the app.

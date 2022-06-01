# SupaCompose

A framework for building android & desktop apps with Supabase

https://user-images.githubusercontent.com/26686035/169712245-e090e33b-8472-49c8-a512-947d5ed889d5.mp4

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supacompose/Supacompose)](https://search.maven.org/artifact/io.github.jan-tennert.supacompose/Supacompose)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supacompose:Supacompose-[module]:VERSION")
}
```

# Creating a client

To create a client simply call the createClient top level function:

```kotlin
val client = createSupabaseClient {
    supabaseUrl = System.getenv("SUPABASE_URL") //without https:// !
    supabaseKey = System.getenv("SUPABASE_KEY")

    install(Auth) {
        //on desktop you have to set the session file. On android and web it's managed by the plugin
        sessionFile = File("C:\\Users\\user\\AppData\\Local\\SupaCompose\\usersession.json")
    }
    //install other plugins
    install(Postgrest)
    install(Storage)
}
```

# Features

#### Authentication

<details><summary>Feature table</summary>


|         | Login                                            | Signup                                           | Verifying (Signup, Password Reset, Invite) | Logout | Otp |
|---------|--------------------------------------------------|--------------------------------------------------|--------------------------------------------|--------|-----|
| Desktop | phone, password, oauth2 via callback http server | phone, password, oauth2 via callback http server | only with token                            | ✅      | ❌   |
| Android | phone, password, oauth2 via deeplinks            | phone, password, oauth2 via deeplinks            | token, url via deeplinks                   | ✅      | ✅   |
| Web     | phone, password, oauth2                          | phone, password, oauth2                          | token, url                                 | ✅      | ✅   |

❌ = will not be implemented \
✅ = implemented

Session saving: ✅

</details>

<details><summary>Authentication with Desktop</summary>
<p>

<b> To add OAuth support, add this link to the redirect urls in supabase </b>

![img.png](.github/images/desktop_supabase.png)

```kotlin
suspend fun main() {
    val client = createSupabaseClient {
        supabaseUrl = System.getenv("SUPABASE_URL")
        supabaseKey = System.getenv("SUPABASE_KEY")

        install(Auth)
    }
    application {
        Window(::exitApplication) {
            val session by client.auth.currentSession.collectAsState()
            val scope = rememberCoroutineScope()
            if (session != null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Logged in as ${session?.user?.email}")
                }
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    Column {
                        TextField(email, { email = it }, placeholder = { Text("Email") })
                        TextField(
                            password,
                            { password = it },
                            placeholder = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Button(onClick = {
                            scope.launch {
                                client.auth.signUpWith(Email) {
                                    this.email = email
                                    this.password = password
                                }
                            }
                        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Login")
                        }
                        Button(
                            {
                                scope.launch {
                                    client.auth.loginWith(Discord) {
                                        onFail = {
                                            when (it) {
                                                is OAuthFail.Timeout -> {
                                                    println("Timeout")
                                                }
                                                is OAuthFail.Error -> {
                                                    //log error
                                                }
                                            }
                                        }
                                        timeout = 50.seconds
                                        htmlTitle = "SupaCompose"
                                        htmlText = "Logged in. You may continue in the app."
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(painterResource("discord_icon.svg"), "", modifier = Modifier.size(25.dp))
                            Text("Log in with Discord")
                        }
                    }
                }

            }
        }
    }

}
```

</p>
</details>

<details><summary>Authentication with Android</summary>

<p>
 <b> When you set the deep link scheme and host in the supabase deeplink plugin and in the android manifest you have to remember to set the additional redirect url in the subabase auth settings. E.g. if you have supacompose as your scheme and login as your host set this to the additional redirect url: </b>

![img.png](.github/images/img.png)
</p>

<blockquote>

<details><summary>MainActivity</summary>
<p>
<b> Note: you should probably use a viewmodel for suspending functions from the SupaCompose library </b>
</p>

<p>

```kotlin
class MainActivity : AppCompatActivity() {

    val supabaseClient = createSupabaseClient {

        supabaseUrl = "your supabase url"
        supabaseKey = "your supabase key"

        install(Auth) {
            scheme = "supacompose"
            host = "login"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAndroid(supabaseClient) //if you don't call this function the library will throw an error when trying to authenticate with oauth
        setContent {
            MaterialTheme {
                val session by supabaseClient.auth.currentSession.collectAsState()
                println(session)
                val scope = rememberCoroutineScope()
                if (session != null) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Logged in as ${session?.user?.email}")
                    }
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        Column {
                            TextField(email, { email = it }, placeholder = { Text("Email") })
                            TextField(
                                password,
                                { password = it },
                                placeholder = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation()
                            )
                            Button(onClick = {
                                scope.launch {
                                    supabaseClient.auth.loginWith(Email) {
                                        this.email = email
                                        this.password = password
                                    }
                                }
                            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text("Login")
                            }
                            Button(
                                {
                                    scope.launch {
                                        client.auth.loginWith(Discord) {
                                            onFail = {
                                                when (it) {
                                                    is OAuthFail.Timeout -> {
                                                        println("Timeout")
                                                    }
                                                    is OAuthFail.Error -> {
                                                        //log error
                                                    }
                                                }
                                            }
                                            timeout = 50.seconds
                                            htmlTitle = "SupaCompose"
                                            htmlText = "Logged in. You may continue in the app."
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(painterResource("discord_icon.svg"), "", modifier = Modifier.size(25.dp))
                                Text("Log in with Discord")
                            }
                        }
                    }
                }
            }
        }
    }

}
```

</p>
</details>

<details><summary>AndroidManifest</summary>
<p>

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="io.github.jan.supacompose.android">

    <uses-permission android:name="android.permission.INTERNET"/>

    <application
            android:allowBackup="false"
            android:supportsRtl="true"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
                <action android:name="android.intent.action.VIEW"/>
                <category android:name="android.intent.category.DEFAULT"/>
                <category android:name="android.intent.category.BROWSABLE"/>
                <!-- This is important for deeplinks. -->
                <data android:scheme="supacompose"
                      android:host="login"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```

</p>
</details>

</blockquote>

</details>

<details><summary>Authentication with Web</summary>

<p>

```kotlin
val client = createSupabaseClient {
    supabaseUrl = ""
    supabaseKey = ""

    install(Auth)
}
client.auth.initializeWeb()

renderComposable(rootElementId = "root") {
    val session by client.auth.currentSession.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    if(session != null) {
        Span({ style { padding(15.px) } }) {
            Text("Logged in as ${session!!.user?.email}")
        }
    } else {
        EmailInput(email) {
            onInput {
                email = it.value
            }
        }
        PasswordInput(password) {
            onInput {
                password = it.value
            }
        }
        Button({
            onClick {
                scope.launch {
                    client.auth.loginWith(Email) {
                        this.email = email
                        this.password = password
                    }
                }
            }
        }) {
            Text("Login")
        }
        Button({
            onClick {
                scope.launch {
                    client.auth.loginWith(Discord)
                }
            }
        }) {
            Text("Login with Discord")
        }
    }
}
```

</p>

</details>


#### Database/Postgres

<details><summary>Make database calls</summary>

```kotlin
//a data class for a message

data class Message(val text: String, @SerialName("author_id") val authorId: String, val id: Int)

```

<b>If you use the syntax with property references the client will automatically look for @SerialName annotiations on your class property and if it has one it will use the value as the column name. (Only JVM)</b>

<blockquote>

<details><summary>Select</summary>

```kotlin
client.postgrest["messages"]
    .select<Message> {
        //you can use that syntax
        Message::authorId eq "someid"
        Message::text neq "This is a text!"
        Message::authorId isIn listOf("test", "test2")

        //or this. But they are the same
        eq("author_id", "someid")
        neq("text", "This is a text!")
        isIn("author_id", listOf("test", "test2"))
    }
````

</details>

<details><summary>Insert</summary>

```kotlin
client.postgrest["messages"]
    .insert<Message>(Message("This is a text!", "someid", 1))
````

</details>

<details><summary>Update</summary>

```kotlin
client.postgrest["messages"]
    .update<Message>(
        {
            Message::text setTo "This is the edited text!"
        }
    ) {
        Message::id eq 2
    }
````

</details>

<details><summary>Delete</summary>

```kotlin
client.postgrest["messages"]
    .delete<Message> {
        Message::id eq 2
    }
````

</details>

</blockquote>

</details>

#### Storage

<details><summary>Managing buckets</summary>

```kotlin
//create a bucket
client.storage.createBucket(name = "images", id = "images", public = false)

//empty bucket
client.storage.emptyBucket(id = "images")

//and so on
```

</details>

<details><summary>Uploading files</summary>

```kotlin
val bucket = client.storage["images"]

//upload a file (jvm)
bucket.upload("landscape.png", File("landscape.png"))

//download a file (jvm)
bucket.downloadTo("landscape.png", File("landscape.png"))

//copy a file

bucket.copy("landscape.png", "landscape2.png")

//and so on
```

</details>

#### Realtime

<details><summary>Listening for database changes (not final syntax)</summary>

```kotlin
val session by client.auth.currentSession.collectAsState()
val status by client.realtime.status.collectAsState()

if (session != null) {
    LaunchedEffect(Unit) {
        client.realtime.connect()
    }
}
if (status == Realtime.Status.CONNECTED) {
    LaunchedEffect(Unit) {
        client.realtime.createChannel("public", "products")
            .on(RealtimeChannel.Action.INSERT) {
                println(it.record<Message>())
            }
            .join()
    }
}

```

</details>

# Credits 

- Postgres Syntax inspired by https://github.com/supabase-community/postgrest-kt
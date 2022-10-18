# Supabase-kt

A Kotlin Multiplatform Client for Supabase.
Supported targets:
- JVM
- Android
- JS (Browser)
- _iOS (planned)_

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:[module e.g. functions-kt or gotrue-kt]:VERSION")

    //add ktor client engine (if you don't already have one, see https://ktor.io/docs/http-client-engines.html for all engines)
    //e.g. the CIO engine
    implementation("io.ktor:ktor-client-cio:KTOR_VERSION")
}
```

# Creating a client

To create a client simply call the createSupabase top level function:

```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://PROJECT_ID.supabase.co",
    supabaseKey = "YOUR_KEY"
) {
    install(GoTrue)
    //install other plugins
    install(Postgrest)
    install(Storage)
}
```

# Features

#### Core

<details><summary>Creating a custom plugin</summary>

```kotlin
class MyPlugin(private val config: MyPlugin.Config): SupabasePlugin {

    fun doSomethingCool() {
        println("something cool")
    }
    
    data class Config(var someSetting: Boolean = false)

    companion object : SupabasePluginProvider<Config, MyPlugin> {

        override val key = "myplugin" //this key is used to identify the plugin when retrieving it

        override fun createConfig(init: Config.() -> Unit): Config {
            //used to create the configuration object for the plugin
            return Config().apply(init)
        }

        override fun setup(builder: SupabaseClientBuilder, config: Config) {
            //modify the supabase client builder
        }

        override fun create(supabaseClient: SupabaseClient, config: Config): MyPlugin {
            //modify the supabase client and return the final plugin instance
            return MyPlugin(config)
        }

    }

}

//make an easy extension for accessing the plugin
val SupabaseClient.myplugin get() = pluginManager.getPlugin<MyPlugin>("myplugin")

//then install it:
val client = createSupabaseClient {
    install(MyPlugin) {
        someSetting = true
    }
}
```

</details>

<details><summary>Initialize the logger</summary>
If you want so see logs for supabase-kt you have to initialize the logger:

```kotlin
Napier.base(DebugAntilog())
```
</details>

#### [Authentication (GoTrue)](/GoTrue)

#### [Database/Postgres](/Postgrest)

#### [Storage](/Storage)

#### [Realtime](/Realtime)

#### [Functions (Edge Functions)](/Functions)

# Credits

- Postgres Syntax inspired by https://github.com/supabase-community/postgrest-kt
- Plugin system inspired by ktor

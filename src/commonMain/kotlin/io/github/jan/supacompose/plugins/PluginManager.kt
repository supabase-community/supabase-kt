package io.github.jan.supacompose.plugins

class PluginManager(@PublishedApi internal val plugins: Map<String, SupacomposePlugin>) {

    inline fun <reified Plugin> getPluginOrNull(key: String): Plugin? {
        return plugins[key] as? Plugin
    }

    inline fun <reified Plugin> getPlugin(key: String): Plugin {
        println(key)
        return getPluginOrNull(key) ?: throw IllegalStateException("Plugin $key not installed or not of type ${Plugin::class}")
    }

    suspend inline fun closeAllPlugins() {
        plugins.values.forEach { it.close() }
    }

    fun getAllInstalledPlugins() = plugins

}
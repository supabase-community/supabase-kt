package io.github.jan.supacompose.plugins

class PluginManager(val installedPlugins: Map<String, SupacomposePlugin>) {

    inline fun <reified Plugin> getPluginOrNull(key: String): Plugin? {
        return installedPlugins[key] as? Plugin
    }

    inline fun <reified Plugin> getPlugin(key: String): Plugin {
        return getPluginOrNull(key) ?: throw IllegalStateException("Plugin $key not installed or not of type ${Plugin::class}")
    }

    suspend inline fun closeAllPlugins() {
        installedPlugins.values.forEach { it.close() }
    }

}
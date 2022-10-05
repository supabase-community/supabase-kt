package io.github.jan.supacompose.plugins

/**
 * The plugin manager is used to manage installed plugins
 */
class PluginManager(val installedPlugins: Map<String, SupacomposePlugin>) {

    /**
     * Retrieve an installed plugin by its [key] or null if no plugin with the given key is installed
     */
    inline fun <reified Plugin> getPluginOrNull(key: String): Plugin? {
        return installedPlugins[key] as? Plugin
    }

    /**
     * Retrieve an installed plugin by its [key] or throw an [IllegalArgumentException] if no plugin with the given key is installed
     */
    inline fun <reified Plugin> getPlugin(key: String): Plugin {
        return getPluginOrNull(key) ?: throw IllegalStateException("Plugin $key not installed or not of type ${Plugin::class}")
    }

    /**
     * Closes all installed plugins
     */
    suspend inline fun closeAllPlugins() {
        installedPlugins.values.forEach { it.close() }
    }

}
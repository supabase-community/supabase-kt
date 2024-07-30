package io.github.jan.supabase.collections

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/**
 * A multiplatform, thread-safe [MutableMap], implemented using AtomicFU. Thanks to the author of [klogging](https://github.com/klogging/klogging)!
 */
@SupabaseInternal
class AtomicMutableMap<K, V>(
    vararg pairs: Pair<K, V>
) : MutableMap<K, V> {

    private val map = atomic(mapOf(*pairs))

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.value.toMutableMap().entries
    override val keys: MutableSet<K>
        get() = map.value.toMutableMap().keys
    override val size: Int
        get() = map.value.size
    override val values: MutableCollection<V>
        get() = map.value.toMutableMap().values

    override fun clear() {
        map.update { emptyMap() }
    }

    override fun isEmpty(): Boolean = map.value.isEmpty()

    override fun remove(key: K): V? {
        var removedValue: V? = null
        map.update { current ->
            buildMap {
                putAll(current)
                removedValue = remove(key)
            }
        }
        return removedValue
    }

    override fun putAll(from: Map<out K, V>) = map.update { current ->
        buildMap {
            putAll(current)
            putAll(from)
        }
    }

    override fun put(key: K, value: V): V? {
        var previousValue: V? = null
        map.update { current ->
            buildMap {
                putAll(current)
                previousValue = put(key, value)
            }
        }
        return previousValue
    }

    override fun get(key: K): V? = map.value[key]

    override fun containsValue(value: V): Boolean = map.value.containsValue(value)

    override fun containsKey(key: K): Boolean = map.value.containsKey(key)
}
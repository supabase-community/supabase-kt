package io.supabase.collections

import io.supabase.annotations.SupabaseInternal
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

/**
 * A multiplatform, thread-safe [MutableList], implemented using AtomicFU. Thanks to the author of [klogging](https://github.com/klogging/klogging)!
 */
@SupabaseInternal
class AtomicMutableList<E>(
    vararg elements: E
) : MutableList<E> {

    private val list = atomic(listOf(*elements))

    override val size: Int
        get() = list.value.size

    override fun clear(): Unit = list.update { buildList { emptyList<E>() } }

    override fun addAll(elements: Collection<E>): Boolean {
        var changed = false
        list.update { current ->
            buildList {
                addAll(current)
                changed = addAll(elements)
            }
        }
        return changed
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        var changed = false
        list.update { current ->
            buildList {
                addAll(current)
                changed = addAll(index, elements)
            }
        }
        return changed
    }

    override fun add(index: Int, element: E) = list.update { current ->
        buildList {
            addAll(current)
            add(index, element)
        }
    }

    override fun add(element: E): Boolean {
        list.update { current ->
            buildList {
                addAll(current)
                add(element)
            }
        }
        return true
    }

    override fun get(index: Int): E = list.value[index]

    override fun isEmpty(): Boolean = list.value.isEmpty()

    override fun iterator(): MutableIterator<E> = list.value.toMutableList().iterator()

    override fun listIterator(): MutableListIterator<E> = list.value.toMutableList().listIterator()

    override fun listIterator(index: Int): MutableListIterator<E> = list.value.toMutableList().listIterator(index)

    override fun removeAt(index: Int): E {
        checkElementIndex(index, size)
        val removed: E = get(index)
        list.update { current ->
            buildList {
                addAll(current)
                removeAt(index)
            }
        }
        return removed
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> =
        list.value.toMutableList().subList(fromIndex, toIndex)

    override fun set(index: Int, element: E): E {
        checkElementIndex(index, size)
        val replaced: E = get(index)
        list.update { current ->
            buildList {
                addAll(current)
                set(index, element)
            }
        }
        return replaced
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var modified = false
        list.update { current ->
            buildList {
                addAll(current)
                modified = retainAll(elements)
            }
        }
        return modified
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var modified = false
        list.update { current ->
            buildList {
                addAll(current)
                modified = removeAll(elements)
            }
        }
        return modified
    }

    override fun remove(element: E): Boolean {
        var modified = false
        list.update { current ->
            buildList {
                addAll(current)
                modified = remove(element)
            }
        }
        return modified
    }

    override fun lastIndexOf(element: E): Int = list.value.lastIndexOf(element)

    override fun indexOf(element: E): Int = list.value.indexOf(element)

    override fun containsAll(elements: Collection<E>): Boolean = list.value.containsAll(elements)

    override fun contains(element: E): Boolean = list.value.contains(element)

    /**
     * Copied from Kotlin [AbstractList] internal code.
     */
    private fun checkElementIndex(index: Int, size: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("index: $index, size: $size")
        }
    }
}
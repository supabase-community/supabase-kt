package io.github.jan.supabase.postgrest.query

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.serializer

object RawQueryHelper {
    /**
     * Generates a query string for a given class, including multiple objects with their properties.
     * Use to query multiple objects with foreign keys
     *
     * @return A string representing the query, e.g., "className(prop1, prop2)" or "prop1, prop2: prop2(subProp)".
     *
     * @throws kotlinx.serialization.SerializationException If the class [T] is not
     *         serializable or lacks a serializer.
     *
     * Example usage:
     * ```
     * @Serializable
     * private data class MessageDto(
     *     @SerialName("content") val content: String,
     *     @SerialName("recipient")val recipient: UserDto,
     *     @SerialName("sender") val createdBy: UserDto,
     *     @SerialName("created_at") val createdAt: String,
     * )
     *
     * @Serializable
     * private data class UserDto(
     *     @SerialName("id")  val id: String,
     *     @SerialName("username") val username: String,
     *     @SerialName("email") val email: String,
     * )
     *```
     * ```
     * val query = RawQueryHelper.queryWithMultipleForeignKeys<MessageDto>()
     * // Returns
     * """
     * content,
     * recipient: recipient(id, username, email),
     * sender: sender(id, username, email),
     * created_at
     * """
     * // Perform query with Postgrest
     * val columns = Columns.raw(query)
     * val messages = postgrest.from("messages")
     *                 .select(
     *                     columns = columns
     *                 ) {
     *                     filter {
     *                         eq(queryParam, queryValue)
     *                     }
     *                 }.decodeList<MessageDto>()
     * ```
     */
    inline fun <reified T> queryWithMultipleForeignKeys(): String {
        val lowercasedClassName = T::class.simpleName?.lowercase()
        val descriptor: SerialDescriptor = serializer<T>().descriptor
        return buildKeyString(descriptor, lowercasedClassName ?: "")
    }

    /**
     * Generates a query string for a given class, including properties and one object that
     * foreign key refers to.
     * Used to query one object request with one foreign key
     *
     * @param T The reified type parameter representing the class to generate a query string
     *          for. The class must be annotated with [kotlinx.serialization.Serializable]
     *          for serialization support.
     * @param customizedClassName An optional string to override the name used for nested
     *                            objects in the query. If null, the original property name
     *                            is used.
     * @return A formatted query string listing the properties of the class. Nested classes
     *         are represented with their properties in a nested format (e.g.,
     *         "name: name(subProp)"), and properties are separated by commas and newlines,
     *         with no trailing comma.
     *
     * @throws kotlinx.serialization.SerializationException If the class [T] is not
     *         serializable or lacks a serializer.
     *
     * Example usage:
     * ```
     * @Serializable
     * data class UserDto(
     *     @SerialName("id") val id: String,
     *     @SerialName("username") val username: String
     * )
     *
     * @Serializable
     * data class System(
     *     @SerialName("name") val name: String,
     *     @SerialName("owner") val owner: UserDto
     * )
     *```
     *```
     * val query = RawQueryHelper.queryWithForeignKey<System>()
     * // Returns:
     * """
     * name,
     * owner: owner(id, username)
     * """
     *
     * val customQuery = RawQueryHelper.queryWithForeignKey<System>("user")
     * // Returns:
     * """
     * name,
     * user(id, username)
     * """
     * // Perform query with Postgrest
     * val columns = Columns.raw(query)
     * val systems = postgrest.from("systems")
     *                 .select(
     *                     columns = columns
     *                 ) {
     *                     filter {
     *                         eq(queryParam, queryValue)
     *                     }
     *                 }.decodeList<System>()
     *```
     */
    inline fun <reified T> queryWithForeignKey(customizedClassName: String? = null): String {
        val descriptor: SerialDescriptor = serializer<T>().descriptor
        val properties = descriptor.elementNames.mapIndexed { index, name ->
            val elementDescriptor = descriptor.getElementDescriptor(index)
            if (elementDescriptor.kind is StructureKind.CLASS && !elementDescriptor.isInline) {
                val updatedName = customizedClassName ?: name
                "$updatedName${buildKeyString(elementDescriptor, "")}"
            } else {
                name
            }
        }
        val result = properties.mapIndexed { index, property ->
            if (index < properties.size - 1) "$property," else property
        }.joinToString("\n")
        result.removeSuffix(",")
        return result
    }

    fun buildKeyString(descriptor: SerialDescriptor, className: String): String {
        val containsNonInlineClass = (0 until descriptor.elementsCount).any { index ->
            val elementDescriptor = descriptor.getElementDescriptor(index)
            elementDescriptor.kind is StructureKind.CLASS && !elementDescriptor.isInline
        }

        if (!containsNonInlineClass) {
            val propertyNames = (0 until descriptor.elementsCount).joinToString(", ") { index ->
                descriptor.getElementName(index)
            }
            return "$className($propertyNames)"
        } else {
            val properties = descriptor.elementNames.mapIndexed { index, name ->
                val elementDescriptor = descriptor.getElementDescriptor(index)
                if (elementDescriptor.kind is StructureKind.CLASS && !elementDescriptor.isInline) {
                    // For nested classes, the prefix is "elementName: elementName"
                    // and the recursive call passes the simple name of the nested class
                    // for the "nestedName(props)" part.
                    val prefix = "$name: $name"
                    "$prefix${buildKeyString(elementDescriptor, "")}"
                } else {
                    name
                }
            }
            val result = properties.mapIndexed { index, property ->
                if (index < properties.size - 1) "$property," else property
            }.joinToString("\n")
            result.removeSuffix(",")
            return result
        }
    }
}
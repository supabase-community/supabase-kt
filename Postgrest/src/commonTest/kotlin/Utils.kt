import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder

@SupabaseInternal
inline fun postgrestRequest(
    schema: String = "public",
    propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE,
    block: PostgrestRequestBuilder.() -> Unit
): PostgrestRequestBuilder {
    val filter = PostgrestRequestBuilder(schema, propertyConversionMethod)
    filter.block()
    return filter
}
import io.supabase.annotations.SupabaseInternal
import io.supabase.postgrest.PropertyConversionMethod
import io.supabase.postgrest.query.PostgrestRequestBuilder

@SupabaseInternal
inline fun postgrestRequest(propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE, block: PostgrestRequestBuilder.() -> Unit): PostgrestRequestBuilder {
    val filter = PostgrestRequestBuilder(propertyConversionMethod)
    filter.block()
    return filter
}
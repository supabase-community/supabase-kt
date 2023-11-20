package io.github.jan.supabase.postgrest.query.filter

/**
 * Used to search rows using a full text search. See [Postgrest](https://postgrest.org/en/stable/api.html#full-text-search) for more information
 */
enum class TextSearchType(val identifier: String) {
    TSVECTOR("tsvector"),
    PLAINTO("plainto"),
    PHRASETO("phraseto"),
    WEBSEARCH("websearch")
}
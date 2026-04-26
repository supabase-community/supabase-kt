package io.github.jan.supabase.postgrest.query

// TODO: make this better
internal sealed interface AcceptHeader {
    data object Single: AcceptHeader
    data object Json: AcceptHeader
    data object CSV: AcceptHeader
    data object GeoJson: AcceptHeader

    companion object {
        fun single(stripNulls: Boolean) =
            "application/vnd.pgrst.object+json" + if(stripNulls) ";nulls=stripped" else ""

        fun json(stripNulls: Boolean) =
            if(stripNulls) "application/vnd.pgrst.array+json;nulls=stripped" else "application/json"

        fun csv() = "text/csv"

        fun geojson() = "application/geo+json"

        fun explain(
            options: String,
            mediaType: String,
            format: String
        ) = "application/vnd.pgrst.plan+${format}; for=\"${mediaType}\"; options=${options};"
    }

}

internal data class ExplainData(
    val options: String,
    val format: String
)
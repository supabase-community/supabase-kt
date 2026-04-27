package io.github.jan.supabase.postgrest.query

internal sealed interface AcceptHeader {
    data object Single: AcceptHeader {
        operator fun invoke(stripNulls: Boolean) =
            "application/vnd.pgrst.object+json" + if(stripNulls) ";nulls=stripped" else ""
    }
    data object Json: AcceptHeader {
        operator fun invoke(stripNulls: Boolean) =
            if(stripNulls) "application/vnd.pgrst.array+json;nulls=stripped" else "application/json"
    }
    data object CSV: AcceptHeader {
        operator fun invoke() = "text/csv"
    }
    data object GeoJson: AcceptHeader {
        operator fun invoke() = "application/geo+json"
    }

}

internal data class ExplainData(
    val options: String,
    val format: String
) {

    operator fun invoke(
        mediaType: String,
    ) = "application/vnd.pgrst.plan+${format}; for=\"${mediaType}\"; options=${options};"

}
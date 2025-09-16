import io.github.jan.supabase.CurrentPlatformTarget
import io.github.jan.supabase.PlatformTarget
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.formUrlEncode
import io.ktor.http.parametersOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Instant

@Serializable
data class TestData(@SerialName("created_at") val createdAt: Instant)

class PostgrestFilterBuilderTest {

    @Test
    fun eq() {
        val filter = filterToString {
            eq("id", 1)
        }
        assertEquals("id=eq.1", filter)
    }

    @Test
    fun neq() {
        val filter = filterToString {
            neq("id", 1)
        }
        assertEquals("id=neq.1", filter)
    }

    @Test
    fun gt() {
        val filter = filterToString {
            gt("id", 1)
        }
        assertEquals("id=gt.1", filter)
    }

    @Test
    fun gte() {
        val filter = filterToString {
            gte("id", 1)
        }
        assertEquals("id=gte.1", filter)
    }

    @Test
    fun lt() {
        val filter = filterToString {
            lt("id", 1)
        }
        assertEquals("id=lt.1", filter)
    }

    @Test
    fun lte() {
        val filter = filterToString {
            lte("id", 1)
        }
        assertEquals("id=lte.1", filter)
    }

    @Test
    fun like() {
        val filter = filterToString {
            ilike("name", "_n*")
        }
        assertEquals("name=ilike._n*", filter)
    }

    @Test
    fun match() {
        val filter = filterToString {
            match("name", "person")
        }
        assertEquals("name=match.person", filter)
    }

    @Test
    fun imatch() {
        val filter = filterToString {
            imatch("name", "person")
        }
        assertEquals("name=imatch.person", filter)
    }

    @Test
    fun ilike() {
        val filter = filterToString {
            ilike("name", "_n_")
        }
        assertEquals("name=ilike._n_", filter)
    }

    @Test
    fun isIn() {
        val filter = filterToString {
            isIn("id", listOf(1, 2, 3))
        }
        assertEquals("id=in.(1,2,3)", filter)
    }

    @Test
    fun exact() {
        val filter = filterToString {
            exact("id", null)
        }
        assertEquals("id=is.null", filter)
    }

    @Test
    fun contains() {
        val filter = filterToString {
            contains("name", listOf("n", "a", "m"))
        }
        assertEquals("name=cs.{n,a,m}", filter)
    }

    @Test
    fun contained() {
        val filter = filterToString {
            contained("id", listOf(1,2,3))
        }
        assertEquals("id=cd.{1,2,3}", filter)
    }

    @Test
    fun overlaps() {
        val filter = filterToString {
            overlaps("id", listOf(1,2,3))
        }
        assertEquals("id=ov.{1,2,3}", filter)
    }

    @Test
    fun and_or() {
        val filter = filterToString {
            and {
                eq("id", 1)
                or {
                    eq("id", 2)
                    eq("id", 3)
                }
            }
        }
        assertEquals("and=(id.eq.1,or(id.eq.2,id.eq.3))", filter)
    }

    @Test
    fun and_referenced_table() {
        val filter = filterToString {
            and(referencedTable = "shops") {
                eq("id", 1)
                eq("id", 2)
            }
        }
        assertEquals("shops.and=(id.eq.1,id.eq.2)", filter)
    }

    @Test
    fun or_referenced_table() {
        val filter = filterToString {
            or(referencedTable = "shops") {
                eq("id", 1)
                eq("id", 2)
            }
        }
        assertEquals("shops.or=(id.eq.1,id.eq.2)", filter)
    }

    @Test
    fun and_negated() {
        val filter = filterToString {
            and(negate = true) {
                eq("id", 1)
                eq("id", 2)
            }
        }
        assertEquals("not.and=(id.eq.1,id.eq.2)", filter)
    }

    @Test
    fun or_negated() {
        val filter = filterToString {
            or(negate = true) {
                eq("id", 1)
                eq("id", 2)
            }
        }
        assertEquals("not.or=(id.eq.1,id.eq.2)", filter)
    }

    @Test
    fun and_empty() {
        val filter = filterToString {
            and { }
        }
        assertEquals("", filter)
    }

    @Test
    fun or_empty() {
        val filter = filterToString {
            or { }
        }
        assertEquals("", filter)
    }

    @Test
    fun sl() {
        val filter = filterToString {
            sl("id", 1L to 10L)
        }
        assertEquals("id=sl.(1,10)", filter)
    }

    @Test
    fun sr() {
        val filter = filterToString {
            sr("id", 1L to 10L)
        }
        assertEquals("id=sr.(1,10)", filter)
    }

    @Test
    fun nxl() {
        val filter = filterToString {
            nxl("id", 1L to 10L)
        }
        assertEquals("id=nxl.(1,10)", filter)
    }

    @Test
    fun nxr() {
        val filter = filterToString {
            nxr("id", 1L to 10L)
        }
        assertEquals("id=nxr.(1,10)", filter)
    }

    @Test
    fun adj() {
        val filter = filterToString {
            adjacent("id", 1L to 10L)
        }
        assertEquals("id=adj.(1,10)", filter)
    }

    @Test
    fun propertyConversionWithSnakeCase() {
        assertEquals("created_at", PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE(TestData::createdAt))
    }

    @Test
    fun propertyConversionWithSerialName() {
        if(CurrentPlatformTarget in listOf(PlatformTarget.JVM, PlatformTarget.ANDROID)) {
            assertEquals("created_at", PropertyConversionMethod.SERIAL_NAME(TestData::createdAt))
        } else {
            assertFails { PropertyConversionMethod.SERIAL_NAME(TestData::createdAt) }
        }
    }

    private fun filterToString(builder: PostgrestFilterBuilder.() -> Unit): String {
        return PostgrestFilterBuilder(PropertyConversionMethod.NONE).apply(block = builder).params.mapValues { (_, value) -> listOf(value.first()) }.let {
            parametersOf(it).formUrlEncode()
        }.decodeURLQueryComponent()
    }

}
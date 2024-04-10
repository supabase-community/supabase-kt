import io.github.jan.supabase.postgrest.query.Columns
import kotlin.test.Test
import kotlin.test.assertEquals

class ColumnsTest {

    @Test
    fun testSingleLineColumns() {
        val columns = Columns.raw("id")
        assertEquals("id", columns.value)
        val columns2 = Columns.raw("id,name")
        assertEquals("id,name", columns2.value)
    }

    @Test
    fun testMultiLineColumns() {
        val columns = Columns.raw(
            """
            id,
            name
            """.trimIndent()
        )
        assertEquals("id,name", columns.value)
    }

    @Test
    fun testMultiLineColumnsWithWhitespaces() {
        val columns = Columns.raw(
            """
            id  ,
            name  
            """.trimIndent()
        )
        assertEquals("id,name", columns.value)
    }

    @Test
    fun testMultiLineColumnsWithQuotedWhitespaces() {
        val columns = Columns.raw(
            """
            "id  ",
            name
            """.trimIndent()
        )
        assertEquals("\"id  \",name", columns.value)
    }

}
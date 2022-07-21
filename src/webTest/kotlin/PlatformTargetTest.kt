import io.github.jan.supacompose.CurrentPlatformTarget
import io.github.jan.supacompose.PlatformTarget
import kotlin.test.Test
import kotlin.test.assertEquals

actual class PlatformTargetTest {

    @Test
    actual fun testPlatformTarget() {
        assertEquals(PlatformTarget.WEB, CurrentPlatformTarget, "The current platform target should be WEB")
    }

}
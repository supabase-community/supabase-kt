import io.github.jan.supabase.CurrentPlatformTarget
import io.github.jan.supabase.PlatformTarget
import kotlin.test.Test
import kotlin.test.assertEquals

actual class PlatformTargetTest {

    @Test
    actual fun testPlatformTarget() {
        assertEquals(PlatformTarget.MACOS, CurrentPlatformTarget, "The current platform target should be MACOS")
    }

}
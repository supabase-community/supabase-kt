import io.github.jan.supacompose.CurrentPlatformTarget
import io.github.jan.supacompose.PlatformTarget
import org.junit.Test
import kotlin.test.assertEquals

actual class PlatformTargetTest {

    @Test
    actual fun testPlatformTarget() {
        assertEquals(PlatformTarget.ANDROID, CurrentPlatformTarget, "The current platform target should be ANDROID")
    }

}
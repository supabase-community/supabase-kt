import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.logging.SupabaseLoggingProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SupabaseLoggerTest {

    class MockLoggingProcessor(
        private val logLevel: LogLevel,
        private val writeChannel: SendChannel<String>,
        private val coroutineScope: CoroutineScope
    ) : SupabaseLoggingProcessor {

        override fun isEnabled(level: LogLevel): Boolean {
            return level <= logLevel
        }

        override fun processLog(
            level: LogLevel,
            tag: String,
            throwable: Throwable?,
            message: String
        ) {
           coroutineScope.launch {
               writeChannel.send(level.name + tag + message)
           }
        }
    }

    @Test
    fun testLogging() = runTest {
        val channel = Channel<String>()
        val logger = SupabaseLogger(LogLevel.DEBUG, "MyTag", { MockLoggingProcessor(it, channel, backgroundScope)} )
        logger.log(LogLevel.DEBUG, null, "Test")
        advanceUntilIdle()
        assertEquals("DEBUGMyTagTest", channel.receive())
    }

}
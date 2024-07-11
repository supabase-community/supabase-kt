import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

suspend inline fun <reified T> Flow<T>.waitForValue(value: T) = filter { it == value }.first()
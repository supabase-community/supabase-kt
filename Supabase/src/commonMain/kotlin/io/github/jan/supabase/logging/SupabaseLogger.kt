package io.github.jan.supabase.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import kotlin.concurrent.Volatile

/**
 * An interface for logging in Supabase plugins.
 */
abstract class SupabaseLogger {

    /**
     * The minimum log level to handle for this logger. If null, [SupabaseClient.DEFAULT_LOG_LEVEL] will be used.
     */
    abstract val level: LogLevel?

    /**
     * Log a message with the given [level] and [message]. An optional [throwable] can be provided.
     * @param level The log level
     * @param throwable An optional throwable
     * @param message The message to log
     */
    abstract fun log(level: LogLevel, throwable: Throwable? = null, message: String)

    /**
     * Log a message with the given [level] and [message]. An optional [throwable] can be provided.
     * @param level The log level
     * @param throwable An optional throwable
     * @param message The message to log
     */
    inline fun log(level: LogLevel, throwable: Throwable? = null, message: () -> String) {
        if (level >= (this.level ?: SupabaseClient.DEFAULT_LOG_LEVEL)) {
            log(level, throwable, message())
        }
    }

    /**
     * Set the log level for this logger. If set to `null`, [SupabaseClient.DEFAULT_LOG_LEVEL] will be used.
     * @param level The log level
     */
    @SupabaseInternal
    abstract fun setLevel(level: LogLevel?)

}

/**
 * A logger implementation using the Kermit logger.
 * @param level The minimum log level for this logger.
 * @param tag The tag for this logger
 */
internal class KermitSupabaseLogger(
    initialLevel: LogLevel?,
    tag: String,
) : SupabaseLogger() {

    @Volatile
    override var level: LogLevel? = initialLevel
        private set

    private val logger: Logger = Logger(
        config = loggerConfigInit(platformLogWriter(), minSeverity = Severity.Debug),
        tag = tag,
    )

    override fun log(level: LogLevel, throwable: Throwable?, message: String) {
        if (level >= getLevelOrDefault()) {
            logger.processLog(level.toSeverity(), logger.tag, throwable, message)
        }
    }

    private fun LogLevel.toSeverity() = when (this) {
        LogLevel.DEBUG -> Severity.Debug
        LogLevel.INFO -> Severity.Info
        LogLevel.WARNING -> Severity.Warn
        LogLevel.ERROR -> Severity.Error
        LogLevel.NONE -> Severity.Assert
    }

    @SupabaseInternal
    override fun setLevel(level: LogLevel?) {
        this.level = level
    }

    private fun getLevelOrDefault(): LogLevel {
        return this.level ?: SupabaseClient.DEFAULT_LOG_LEVEL
    }

}

/**
 * Log a debug message with an optional [throwable]
 * @param throwable An optional throwable
 * @param message The message to log
 */
inline fun SupabaseLogger.d(throwable: Throwable? = null, message: () -> String) {
    log(LogLevel.DEBUG, throwable, message)
}

/**
 * Log an info message with an optional [throwable]
 * @param throwable An optional throwable
 * @param message The message to log
 */
inline fun SupabaseLogger.i(throwable: Throwable? = null, message: () -> String) {
    log(LogLevel.INFO, throwable, message)
}

/**
 * Log a warning message with an optional [throwable]
 * @param throwable An optional throwable
 * @param message The message to log
 */
inline fun SupabaseLogger.w(throwable: Throwable? = null, message: () -> String) {
    log(LogLevel.WARNING, throwable, message)
}

/**
 * Log an error message with an optional [throwable]
 * @param throwable An optional throwable
 * @param message The message to log
 */
inline fun SupabaseLogger.e(throwable: Throwable? = null, message: () -> String) {
    log(LogLevel.ERROR, throwable, message)
}
package io.supabase.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.supabase.SupabaseClient
import io.supabase.annotations.SupabaseInternal

/**
 * An interface for logging in Supabase plugins.
 */
abstract class SupabaseLogger {

    /**
     * The log level for this logger. If null, the [SupabaseClient.Companion.DEFAULT_LOG_LEVEL] will be used.
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
        if (level >= (this.level ?: SupabaseClient.Companion.DEFAULT_LOG_LEVEL)) {
            log(level, throwable, message())
        }
    }

    /**
     * Set the log level for this logger
     * @param level The log level
     */
    @SupabaseInternal
    abstract fun setLevel(level: LogLevel)

}

/**
 * A logger implementation using the Kermit logger.
 * @param level The log level for this logger. If null, the [SupabaseClient.Companion.DEFAULT_LOG_LEVEL] will be used.
 * @param tag The tag for this logger
 * @param logger The Kermit logger
 */
class KermitSupabaseLogger(level: LogLevel?, tag: String, private val logger: Logger = Logger.withTag(tag)):
    SupabaseLogger() {

    override var level: LogLevel? = level
        private set

    @SupabaseInternal
    override fun setLevel(level: LogLevel) {
        this.level = level
    }

    override fun log(level: LogLevel, throwable: Throwable?, message:  String) {
        logger.log(level.toSeverity(), logger.tag, throwable, message)
    }

    private fun LogLevel.toSeverity() = when(this) {
        LogLevel.DEBUG -> Severity.Debug
        LogLevel.INFO -> Severity.Info
        LogLevel.WARNING -> Severity.Warn
        LogLevel.ERROR -> Severity.Error
        LogLevel.NONE -> Severity.Assert
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
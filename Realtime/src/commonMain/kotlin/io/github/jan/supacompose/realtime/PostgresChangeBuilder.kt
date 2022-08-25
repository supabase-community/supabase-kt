package io.github.jan.supacompose.realtime

class PostgresChangeBuilder {

    var schema: String = ""
    var table: String? = null
    var filter: String? = null
    private var event = "*"
    private var handler: Any.() -> Unit = {}

    fun update(handler: PostgresAction.Update.() -> Unit) {
        event = "UPDATE"
        this.handler = {
            handler(this as? PostgresAction.Update ?: throw IllegalStateException("Not a PostgresAction.Update"))
        }
    }

    fun insert(handler: PostgresAction.Insert.() -> Unit) {
        event = "INSERT"
        this.handler = {
            handler(this as? PostgresAction.Insert ?: throw IllegalStateException("Not a PostgresAction.Insert"))
        }
    }

    fun delete(handler: PostgresAction.Delete.() -> Unit) {
        event = "DELETE"
        this.handler = {
            handler(this as? PostgresAction.Delete ?: throw IllegalStateException("Not a PostgresAction.Delete"))
        }
    }

    fun select(handler: PostgresAction.Select.() -> Unit) {
        event = "SELECT"
        this.handler = {
            handler(this as? PostgresAction.Select ?: throw IllegalStateException("Not a PostgresAction.Select"))
        }
    }
    fun all(handler: PostgresAction.() -> Unit) {
        event = "*"
        this.handler = {
            handler(this as? PostgresAction ?: throw IllegalStateException("Not a PostgresAction.All"))
        }
    }

    fun toBinding() = RealtimeBinding.PostgrestRealtimeBinding(
        PostgresJoinConfig(schema, table, filter, event),
        handler
    )

}
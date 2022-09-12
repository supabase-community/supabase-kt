package io.github.jan.supacompose.realtime

sealed class EventType(val name: String) {
    object NotSet : EventType("")
    object All: EventType("*")
    object Insert: EventType("INSERT")
    object Update: EventType("UPDATE")
    object Delete: EventType("DELETE")
    object Select : EventType("SELECT")
}

sealed class PostgresChangeBuilder {

    abstract var schema: String
    abstract var table: String?
    abstract var filter: String?
    var event: EventType = EventType.NotSet

    class CallbackBasedBuilder : PostgresChangeBuilder() {
        override var schema: String = ""
        override var table: String? = null
        override var filter: String? = null

        @PublishedApi internal var handler: PostgresAction.() -> Unit = {}

        fun update(handler: PostgresAction.Update.() -> Unit) {
            event = EventType.Update
            this.handler = {
                handler(this as? PostgresAction.Update ?: throw IllegalStateException("Not a PostgresAction.Update"))
            }
        }

        fun insert(handler: PostgresAction.Insert.() -> Unit) {
            event = EventType.Insert
            this.handler = {
                handler(this as? PostgresAction.Insert ?: throw IllegalStateException("Not a PostgresAction.Insert"))
            }
        }

        fun delete(handler: PostgresAction.Delete.() -> Unit) {
            event = EventType.Delete
            this.handler = {
                handler(this as? PostgresAction.Delete ?: throw IllegalStateException("Not a PostgresAction.Delete"))
            }
        }

        fun select(handler: PostgresAction.Select.() -> Unit) {
            event = EventType.Select
            this.handler = {
                handler(this as? PostgresAction.Select ?: throw IllegalStateException("Not a PostgresAction.Select"))
            }
        }
        fun all(handler: PostgresAction.() -> Unit) {
            event = EventType.All
            this.handler = {
                handler(this as? PostgresAction ?: throw IllegalStateException("Not a PostgresAction.All"))
            }
        }

    }

    class FlowBasedBuilder : PostgresChangeBuilder() {
        override var schema: String = ""
        override var table: String? = null
        override var filter: String? = null
    }

    fun buildConfig() = PostgresJoinConfig(schema, table, filter, if(event == EventType.NotSet) throw IllegalStateException("Event type not set") else event.name)

}
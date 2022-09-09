package io.github.jan.supacompose.realtime

sealed interface PostgresChangeBuilder {

    var schema: String
    var table: String?
    var filter: String?
    var event: String

    class CallbackBasedBuilder : PostgresChangeBuilder {
        override var schema: String = ""
        override var table: String? = null
        override var filter: String? = null
        override var event: String = ""

        @PublishedApi internal var handler: PostgresAction.() -> Unit = {}

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

    }

    class FlowBasedBuilder : PostgresChangeBuilder {
        override var schema: String = ""
        override var table: String? = null
        override var filter: String? = null
        override var event: String = ""
    }

    fun buildConfig() = PostgresJoinConfig(schema, table, filter, event)

}
package io.github.jan.supabase.auth.providers

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

sealed interface LoginIdentifier {

    abstract fun put(builder: JsonObjectBuilder)

}

data class Email(val address: String) : LoginIdentifier {

    override fun put(builder: JsonObjectBuilder) {
        builder.put("email", address)
    }


}

data class Phone(val number: String) : LoginIdentifier {

    enum class Channel(val value: String) {
        /**
         * Send the confirmation via SMS
         */
        SMS("sms"),

        /**
         * Send the confirmation via WhatsApp. **Note:** WhatsApp is only supported by Twilio
         */
        WHATSAPP("whatsapp");
    }

    override fun put(builder: JsonObjectBuilder) {
        builder.put("phone", number)
    }


}
import io.github.jan.supabase.gotrue.user.AppMetadata
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class GoTrueMock {

    val engine = MockEngine {
        handleRequest(it) ?: respondInternalError("Invalid route")
    }

    private suspend fun MockRequestHandleScope.handleRequest(request: HttpRequestData): HttpResponseData? {
        val url = request.url
        val urlWithoutQuery = url.encodedPath
        val query = url.parameters
        return when {
            urlWithoutQuery.endsWith("token") -> handleLogin(request)
            else -> null
        }
    }

    private suspend fun MockRequestHandleScope.handleLogin(request: HttpRequestData): HttpResponseData {
        if(request.method != HttpMethod.Post) respondBadRequest("Invalid method")
        if(!request.url.parameters.contains("grant_type")) return respondBadRequest("grant_type is required")
        if(request.url.parameters["grant_type"] != "password") return respondBadRequest("grant_type must be password")
        val body = try {
            Json.decodeFromString<JsonObject>(request.body.toByteArray().decodeToString())
        } catch(e: Exception) {
            return respondBadRequest("Invalid body")
        }
        if(!body.containsKey("password")) return respondBadRequest("password is required")
        val password = body["password"]?.jsonPrimitive?.contentOrNull ?: ""
        return when {
            body.containsKey("email") -> {
                if(password != "password") return respondBadRequest("Invalid password")
                respondValidSession()
            }
            body.containsKey("phone") -> {
                if(password != "password") return respondBadRequest("Invalid password")
                respondValidSession()
            }
            else -> respondBadRequest("email or phone is required")
        }
    }

    private fun MockRequestHandleScope.respondValidSession(): HttpResponseData {
        return respond(Json.encodeToString(UserSession("some_token", "refresh_token", 200, "token_type", UserInfo(aud = "", appMetadata = AppMetadata("", listOf()), id = ""))))
    }

    private fun MockRequestHandleScope.respondInternalError(message: String): HttpResponseData {
        return respond(message, HttpStatusCode.InternalServerError)
    }

    private fun MockRequestHandleScope.respondBadRequest(message: String): HttpResponseData {
        return respond(message, HttpStatusCode.BadRequest)
    }


}
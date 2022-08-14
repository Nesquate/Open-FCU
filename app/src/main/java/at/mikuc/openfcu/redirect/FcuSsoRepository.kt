package at.mikuc.openfcu.redirect

import android.util.Log
import at.mikuc.openfcu.TAG
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import javax.inject.Inject
import kotlinx.serialization.json.Json

const val SSO_URL = "https://service206-sds.fcu.edu.tw/mobileservice/RedirectService.svc/Redirect"

class FcuSsoRepository @Inject constructor() {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { coerceInputValues = true })
        }
    }

    suspend fun singleSignOn(request: SSORequest): SSOResponse? {
        return try {
            client.post(SSO_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Unknown error")
            null
        }
    }
}

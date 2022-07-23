package at.mikuc.openfcu.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mikuc.openfcu.TAG
import at.mikuc.openfcu.repository.UserPreferencesRepository
import at.mikuc.openfcu.repository.UserPreferencesRepository.Companion.KEY_ID
import at.mikuc.openfcu.repository.UserPreferencesRepository.Companion.KEY_PASSWORD
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.QRCode.Companion.DEFAULT_CELL_SIZE
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

data class QrCodeUiState(
    val bitmap: Bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).apply {
        eraseColor(Color.GRAY)
    },
)

@HiltViewModel
class QrCodeViewModel @Inject constructor(
    private val pref: UserPreferencesRepository,
) : ViewModel() {

    var state by mutableStateOf(QrCodeUiState())

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
        install(HttpCookies)
    }

    init {
        fetchQrCodeData()
    }

    @Serializable
    class Empty

    fun fetchQrCodeData() {
        viewModelScope.launch {
            val id = pref.get(KEY_ID) ?: return@launch
            val password = pref.get(KEY_PASSWORD) ?: return@launch
            Log.d(TAG, id)
            Log.d(TAG, password)
            val resp1 = client.post("https://service202-sds.fcu.edu.tw/FcucardQrcode/Login.aspx") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("username=$id&password=$password&appversion=2")
            }.body<String>()
            Log.d(TAG, resp1)
            val resp2 =
                client.post("https://service202-sds.fcu.edu.tw/FcucardQrcode/FcuCard.aspx/GetEncryptData") {
                    contentType(ContentType.Application.Json)
                    setBody(Empty())
                }.body<JsonElement>()
            Log.d(TAG, resp2.jsonObject.toString())
            val hexString = resp2
                .jsonObject["d"]
                ?.jsonObject?.get("hexString")
                ?.jsonPrimitive?.content ?: return@launch
            val bitmap =
                QRCode(hexString).render(margin = DEFAULT_CELL_SIZE).nativeImage() as Bitmap
            state = state.copy(bitmap = bitmap)
        }
    }
}
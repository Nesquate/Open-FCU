package at.mikuc.openfcu.timetable

import at.mikuc.openfcu.TIMETABLE_DATA_URL
import at.mikuc.openfcu.setting.Credential
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import javax.inject.Inject

class FcuTimetableRepository @Inject constructor() {
    private val client = HttpClient(CIO) {
        followRedirects = false
        install(ContentNegotiation) { json() }
        install(HttpCookies)
    }

    suspend fun fetchTimetable(credential: Credential): List<Section>? {
        val resp = client.post(TIMETABLE_DATA_URL) {
            contentType(ContentType.Application.Json)
            setBody(credential)
        }.body<TimetableResponseDTO>()
        return resp.timeTableTw?.map { it.toSection() }?.toList()
    }

}
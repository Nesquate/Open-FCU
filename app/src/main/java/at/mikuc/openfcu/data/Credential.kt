package at.mikuc.openfcu.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Credential(
    @SerialName("Account") val id: String,
    @SerialName("Password") val password: String,
)
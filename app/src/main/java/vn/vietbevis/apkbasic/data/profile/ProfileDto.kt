package vn.vietbevis.apkbasic.data.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.UserProfile

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("currency") val currency: String = "VND",
)

fun ProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    displayName = displayName,
    currency = currency,
)

fun UserProfile.toDto(): ProfileDto = ProfileDto(
    id = id,
    displayName = displayName,
    currency = currency,
)

package vn.vietbevis.apkbasic.data.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.core.common.DateCodecs
import vn.vietbevis.apkbasic.domain.model.UserProfile

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("currency") val currency: String = "VND",
    @SerialName("avatar") val avatar: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

fun ProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    displayName = displayName,
    currency = currency,
    avatar = avatar,
    updatedAt = updatedAt?.let { DateCodecs.isoToEpochMillis(it) } ?: 0L,
)

fun UserProfile.toDto(): ProfileDto = ProfileDto(
    id = id,
    displayName = displayName,
    currency = currency,
    avatar = avatar,
    updatedAt = if (updatedAt > 0) DateCodecs.epochMillisToIso(updatedAt) else null,
)

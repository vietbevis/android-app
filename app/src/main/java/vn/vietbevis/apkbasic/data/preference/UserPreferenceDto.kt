package vn.vietbevis.apkbasic.data.preference

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.AppLanguage
import vn.vietbevis.apkbasic.domain.model.ThemeMode
import vn.vietbevis.apkbasic.domain.model.UserPreference
import vn.vietbevis.apkbasic.domain.model.WeekStart

@Serializable
data class UserPreferenceDto(
    @SerialName("user_id") val userId: String,
    @SerialName("language") val language: String,
    @SerialName("theme_mode") val themeMode: String,
    @SerialName("currency") val currency: String,
    @SerialName("week_starts_on") val weekStartsOn: String,
    @SerialName("default_wallet_id") val defaultWalletId: String? = null,
)

fun UserPreferenceDto.toDomain(): UserPreference = UserPreference(
    userId = userId,
    language = if (language == "en") AppLanguage.ENGLISH else AppLanguage.VIETNAMESE,
    themeMode = when (themeMode) {
        "light" -> ThemeMode.LIGHT
        "system" -> ThemeMode.SYSTEM
        else -> ThemeMode.DARK
    },
    currency = currency,
    weekStartsOn = if (weekStartsOn == "sunday") WeekStart.SUNDAY else WeekStart.MONDAY,
    defaultWalletId = defaultWalletId,
)

fun UserPreference.toDto(): UserPreferenceDto = UserPreferenceDto(
    userId = userId,
    language = if (language == AppLanguage.ENGLISH) "en" else "vi",
    themeMode = when (themeMode) {
        ThemeMode.DARK -> "dark"
        ThemeMode.LIGHT -> "light"
        ThemeMode.SYSTEM -> "system"
    },
    currency = currency,
    weekStartsOn = if (weekStartsOn == WeekStart.SUNDAY) "sunday" else "monday",
    defaultWalletId = defaultWalletId,
)

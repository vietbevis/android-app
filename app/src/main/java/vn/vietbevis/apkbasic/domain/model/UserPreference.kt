package vn.vietbevis.apkbasic.domain.model

data class UserPreference(
    val userId: String,
    val language: AppLanguage = AppLanguage.VIETNAMESE,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val currency: String = Money.DEFAULT_CURRENCY,
    val weekStartsOn: WeekStart = WeekStart.MONDAY,
    val defaultWalletId: String? = null,
)

enum class AppLanguage {
    VIETNAMESE,
    ENGLISH,
}

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
}

enum class WeekStart {
    MONDAY,
    SUNDAY,
}

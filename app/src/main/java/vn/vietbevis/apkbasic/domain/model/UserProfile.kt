package vn.vietbevis.apkbasic.domain.model

data class UserProfile(
    val id: String,
    val displayName: String?,
    val currency: String = Money.DEFAULT_CURRENCY,
)

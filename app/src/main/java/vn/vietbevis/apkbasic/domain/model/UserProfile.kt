package vn.vietbevis.apkbasic.domain.model

data class UserProfile(
    val id: String,
    val email: String? = null,
    val displayName: String?,
    val currency: String = Money.DEFAULT_CURRENCY,
    val avatar: String? = null,
    val updatedAt: Long = 0,
)

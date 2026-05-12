package vn.vietbevis.apkbasic.domain.model

data class Transaction(
    val id: String,
    val userId: String,
    val walletId: String,
    val categoryId: String?,
    val type: TransactionType,
    val amount: Money,
    val note: String?,
    val occurredAtEpochMillis: Long,
    val photoPath: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationLabel: String? = null,
)

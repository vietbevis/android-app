package vn.vietbevis.apkbasic.domain.model

data class Transfer(
    val id: String,
    val userId: String,
    val fromWalletId: String,
    val toWalletId: String,
    val amount: Money,
    val fee: Money = Money.vnd(0),
    val occurredAtEpochMillis: Long,
    val note: String? = null,
)

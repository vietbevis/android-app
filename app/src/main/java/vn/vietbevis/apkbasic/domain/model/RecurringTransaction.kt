package vn.vietbevis.apkbasic.domain.model

data class RecurringTransaction(
    val id: String,
    val userId: String,
    val walletId: String,
    val categoryId: String?,
    val type: TransactionType,
    val amount: Money,
    val schedule: RecurringSchedule,
    val nextRunEpochMillis: Long,
    val note: String? = null,
    val isEnabled: Boolean = true,
    val isArchived: Boolean = false,
)

data class RecurringSchedule(
    val frequency: RecurringFrequency,
    val interval: Int = 1,
    val dayOfMonth: Int? = null,
    val dayOfWeek: Int? = null,
)

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

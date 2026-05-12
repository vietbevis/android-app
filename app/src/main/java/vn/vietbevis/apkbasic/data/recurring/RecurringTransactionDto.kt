package vn.vietbevis.apkbasic.data.recurring

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.core.common.DateCodecs
import vn.vietbevis.apkbasic.data.category.toTransactionType
import vn.vietbevis.apkbasic.data.category.toWireValue
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.RecurringFrequency
import vn.vietbevis.apkbasic.domain.model.RecurringSchedule
import vn.vietbevis.apkbasic.domain.model.RecurringTransaction

@Serializable
data class RecurringTransactionDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("wallet_id") val walletId: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("type") val type: String,
    @SerialName("amount") val amount: Double,
    @SerialName("frequency") val frequency: String,
    @SerialName("interval_count") val intervalCount: Int = 1,
    @SerialName("day_of_month") val dayOfMonth: Int? = null,
    @SerialName("day_of_week") val dayOfWeek: Int? = null,
    @SerialName("next_run_at") val nextRunAt: String,
    @SerialName("note") val note: String? = null,
    @SerialName("is_enabled") val isEnabled: Boolean = true,
    @SerialName("is_archived") val isArchived: Boolean = false,
)

fun RecurringTransactionDto.toDomain(): RecurringTransaction = RecurringTransaction(
    id = id,
    userId = userId,
    walletId = walletId,
    categoryId = categoryId,
    type = type.toTransactionType(),
    amount = Money.vnd(amount.toLong()),
    schedule = RecurringSchedule(
        frequency = frequency.toRecurringFrequency(),
        interval = intervalCount,
        dayOfMonth = dayOfMonth,
        dayOfWeek = dayOfWeek,
    ),
    nextRunEpochMillis = DateCodecs.isoToEpochMillis(nextRunAt),
    note = note,
    isEnabled = isEnabled,
    isArchived = isArchived,
)

fun RecurringTransaction.toDto(): RecurringTransactionDto = RecurringTransactionDto(
    id = id,
    userId = userId,
    walletId = walletId,
    categoryId = categoryId,
    type = type.toWireValue(),
    amount = amount.minorUnits.toDouble(),
    frequency = schedule.frequency.toWireValue(),
    intervalCount = schedule.interval,
    dayOfMonth = schedule.dayOfMonth,
    dayOfWeek = schedule.dayOfWeek,
    nextRunAt = DateCodecs.epochMillisToIso(nextRunEpochMillis),
    note = note,
    isEnabled = isEnabled,
    isArchived = isArchived,
)

private fun String.toRecurringFrequency(): RecurringFrequency = when (this) {
    "daily" -> RecurringFrequency.DAILY
    "weekly" -> RecurringFrequency.WEEKLY
    "monthly" -> RecurringFrequency.MONTHLY
    "yearly" -> RecurringFrequency.YEARLY
    else -> RecurringFrequency.MONTHLY
}

private fun RecurringFrequency.toWireValue(): String = when (this) {
    RecurringFrequency.DAILY -> "daily"
    RecurringFrequency.WEEKLY -> "weekly"
    RecurringFrequency.MONTHLY -> "monthly"
    RecurringFrequency.YEARLY -> "yearly"
}

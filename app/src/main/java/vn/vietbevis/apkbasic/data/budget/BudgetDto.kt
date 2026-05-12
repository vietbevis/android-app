package vn.vietbevis.apkbasic.data.budget

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.Budget
import vn.vietbevis.apkbasic.domain.model.BudgetCycle
import vn.vietbevis.apkbasic.domain.model.BudgetScope
import vn.vietbevis.apkbasic.domain.model.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable
data class BudgetDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("amount") val amount: Double,
    @SerialName("cycle") val cycle: String,
    @SerialName("scope_type") val scopeType: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("is_archived") val isArchived: Boolean = false,
)

fun BudgetDto.toDomain(): Budget = Budget(
    id = id,
    userId = userId,
    name = name,
    amount = Money.vnd(amount.toLong()),
    cycle = cycle.toBudgetCycle(),
    scope = if (scopeType == "category" && categoryId != null) {
        BudgetScope.Category(categoryId)
    } else {
        BudgetScope.Total
    },
    color = color,
    icon = icon,
    startsAtEpochMillis = startsAt?.dateToEpochMillis(),
    isArchived = isArchived,
)

fun Budget.toDto(): BudgetDto {
    val categoryId = (scope as? BudgetScope.Category)?.categoryId
    return BudgetDto(
        id = id,
        userId = userId,
        name = name,
        amount = amount.minorUnits.toDouble(),
        cycle = cycle.toWireValue(),
        scopeType = if (categoryId == null) "total" else "category",
        categoryId = categoryId,
        color = color,
        icon = icon,
        startsAt = startsAtEpochMillis?.epochMillisToDate(),
        isArchived = isArchived,
    )
}

fun String.toBudgetCycle(): BudgetCycle = when (this) {
    "daily" -> BudgetCycle.DAILY
    "weekly" -> BudgetCycle.WEEKLY
    "biweekly" -> BudgetCycle.BIWEEKLY
    "monthly" -> BudgetCycle.MONTHLY
    "yearly" -> BudgetCycle.YEARLY
    "custom" -> BudgetCycle.CUSTOM
    else -> error("Unsupported budget cycle: $this")
}

fun BudgetCycle.toWireValue(): String = when (this) {
    BudgetCycle.DAILY -> "daily"
    BudgetCycle.WEEKLY -> "weekly"
    BudgetCycle.BIWEEKLY -> "biweekly"
    BudgetCycle.MONTHLY -> "monthly"
    BudgetCycle.YEARLY -> "yearly"
    BudgetCycle.CUSTOM -> "custom"
}

private val dateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

private fun Long.epochMillisToDate(): String = dateFormatter.format(Date(this))

private fun String.dateToEpochMillis(): Long =
    runCatching { dateFormatter.parse(this)?.time ?: 0L }.getOrDefault(0L)

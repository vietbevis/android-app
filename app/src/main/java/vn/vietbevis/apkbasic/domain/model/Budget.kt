package vn.vietbevis.apkbasic.domain.model

data class Budget(
    val id: String,
    val userId: String,
    val name: String,
    val amount: Money,
    val cycle: BudgetCycle,
    val scope: BudgetScope,
    val color: String? = null,
    val icon: String? = null,
    val startsAtEpochMillis: Long? = null,
    val isArchived: Boolean = false,
)

enum class BudgetCycle {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM,
}

sealed interface BudgetScope {
    data object Total : BudgetScope
    data class Category(val categoryId: String) : BudgetScope
}

package vn.vietbevis.apkbasic.domain.model

data class MonthlyBudget(
    val id: String,
    val userId: String,
    val amount: Money,
    val budgetMonth: String, // Format: YYYY-MM-01
    val categoryBudgets: List<CategoryBudget> = emptyList()
)

data class CategoryBudget(
    val id: String? = null,
    val monthlyBudgetId: String,
    val categoryId: String,
    val amount: Money
)

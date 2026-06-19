package vn.vietbevis.apkbasic.data.budget

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.CategoryBudget
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.MonthlyBudget

@Serializable
data class MonthlyBudgetDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("amount") val amount: Double,
    @SerialName("budget_month") val budgetMonth: String,
)

@Serializable
data class CategoryBudgetDto(
    @SerialName("id") val id: String? = null,
    @SerialName("monthly_budget_id") val monthlyBudgetId: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("amount") val amount: Double,
)

fun MonthlyBudgetDto.toDomain(categoryBudgets: List<CategoryBudget> = emptyList()): MonthlyBudget = MonthlyBudget(
    id = id,
    userId = userId,
    amount = Money.vnd(amount.toLong()),
    budgetMonth = budgetMonth,
    categoryBudgets = categoryBudgets
)

fun MonthlyBudget.toDto(): MonthlyBudgetDto = MonthlyBudgetDto(
    id = id,
    userId = userId,
    amount = amount.minorUnits.toDouble(),
    budgetMonth = budgetMonth
)

fun CategoryBudgetDto.toDomain(): CategoryBudget = CategoryBudget(
    id = id,
    monthlyBudgetId = monthlyBudgetId,
    categoryId = categoryId,
    amount = Money.vnd(amount.toLong())
)

fun CategoryBudget.toDto(): CategoryBudgetDto = CategoryBudgetDto(
    id = id,
    monthlyBudgetId = monthlyBudgetId,
    categoryId = categoryId,
    amount = amount.minorUnits.toDouble()
)

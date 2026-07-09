package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.CategoryBudget
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.MonthlyBudget

interface BudgetRepository {
    // New Monthly Budget methods
    suspend fun getMonthlyBudget(userId: String, budgetMonth: String): Result<MonthlyBudget?>
    suspend fun upsertMonthlyBudget(budget: MonthlyBudget): Result<MonthlyBudget>
    suspend fun upsertCategoryBudgets(budgets: List<CategoryBudget>): Result<Unit>
    suspend fun listMonthlyBudgets(userId: String): Result<List<MonthlyBudget>>
    suspend fun deleteCategoryBudgets(monthlyBudgetId: String, categoryIds: List<String>): Result<Unit>

    // Helper for checking spend without needing the full Budget object if we already have specific params
    suspend fun getSpentAmount(
        userId: String,
        categoryId: String?,
        monthStartMillis: Long,
        monthEndMillis: Long
    ): Result<Money>
}

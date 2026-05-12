package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Budget

interface BudgetRepository {
    suspend fun listBudgets(includeArchived: Boolean = false): Result<List<Budget>>
    suspend fun createBudget(budget: Budget): Result<Budget>
    suspend fun updateBudget(budget: Budget): Result<Budget>
    suspend fun archiveBudget(budgetId: String): Result<Unit>
}

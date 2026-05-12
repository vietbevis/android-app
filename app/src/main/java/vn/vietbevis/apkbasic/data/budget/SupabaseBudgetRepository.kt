package vn.vietbevis.apkbasic.data.budget

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Budget
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository

class SupabaseBudgetRepository(
    private val client: SupabaseClient,
) : BudgetRepository {
    override suspend fun listBudgets(includeArchived: Boolean): Result<List<Budget>> = appResult {
        client.from("budgets")
            .select {
                if (!includeArchived) {
                    filter { eq("is_archived", false) }
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<BudgetDto>()
            .map { it.toDomain() }
    }

    override suspend fun createBudget(budget: Budget): Result<Budget> = appResult {
        client.from("budgets").insert(budget.toDto())
        readBudget(budget.id)
    }

    override suspend fun updateBudget(budget: Budget): Result<Budget> = appResult {
        client.from("budgets")
            .update(budget.toDto()) {
                filter { eq("id", budget.id) }
            }
        readBudget(budget.id)
    }

    override suspend fun archiveBudget(budgetId: String): Result<Unit> = appResult {
        client.from("budgets")
            .update({
                set("is_archived", true)
            }) {
                filter { eq("id", budgetId) }
            }
        Unit
    }

    private suspend fun readBudget(budgetId: String): Budget =
        client.from("budgets")
            .select {
                filter { eq("id", budgetId) }
            }
            .decodeSingle<BudgetDto>()
            .toDomain()
}

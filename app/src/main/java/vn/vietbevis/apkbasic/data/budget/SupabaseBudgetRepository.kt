package vn.vietbevis.apkbasic.data.budget

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.DateCodecs
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.data.transaction.TransactionDto
import vn.vietbevis.apkbasic.domain.model.CategoryBudget
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.MonthlyBudget
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository

class SupabaseBudgetRepository(
    private val client: SupabaseClient,
) : BudgetRepository {

    override suspend fun getSpentAmount(
        userId: String,
        categoryId: String?,
        monthStartMillis: Long,
        monthEndMillis: Long
    ): Result<Money> = appResult {
        val startIso = DateCodecs.epochMillisToIso(monthStartMillis)
        val endIso = DateCodecs.epochMillisToIso(monthEndMillis)

        val response = client.from("transactions")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("type", "expense")
                    gte("occurred_at", startIso)
                    lt("occurred_at", endIso)
                    if (categoryId != null) {
                        eq("category_id", categoryId)
                    }
                }
            }
            .decodeList<TransactionDto>()

        val total = response.sumOf { it.amount.toLong() }
        Money.vnd(total)
    }

    override suspend fun getMonthlyBudget(userId: String, budgetMonth: String): Result<MonthlyBudget?> = appResult {
        val budgetDto = client.from("monthly_budgets")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("budget_month", budgetMonth)
                }
            }
            .decodeSingleOrNull<MonthlyBudgetDto>() ?: return@appResult null

        val categoryDtos = client.from("category_budgets")
            .select {
                filter {
                    eq("monthly_budget_id", budgetDto.id)
                }
            }
            .decodeList<CategoryBudgetDto>()

        budgetDto.toDomain(categoryDtos.map { it.toDomain() })
    }

    override suspend fun upsertMonthlyBudget(budget: MonthlyBudget): Result<MonthlyBudget> = appResult {
        client.from("monthly_budgets").upsert(budget.toDto()) {
            onConflict = "user_id,budget_month"
        }
        getMonthlyBudget(budget.userId, budget.budgetMonth).getOrThrow()!!
    }

    override suspend fun upsertCategoryBudgets(budgets: List<CategoryBudget>): Result<Unit> = appResult {
        if (budgets.isEmpty()) return@appResult
        client.from("category_budgets").upsert(budgets.map { it.toDto() }) {
            onConflict = "monthly_budget_id,category_id"
        }
    }

    override suspend fun listMonthlyBudgets(userId: String): Result<List<MonthlyBudget>> = appResult {
        client.from("monthly_budgets")
            .select {
                filter { eq("user_id", userId) }
                order("budget_month", Order.DESCENDING)
            }
            .decodeList<MonthlyBudgetDto>()
            .map { it.toDomain() }
    }

    override suspend fun deleteCategoryBudgets(monthlyBudgetId: String, categoryIds: List<String>): Result<Unit> = appResult {
        if (categoryIds.isEmpty()) return@appResult
        client.from("category_budgets").delete {
            filter {
                eq("monthly_budget_id", monthlyBudgetId)
                isIn("category_id", categoryIds)
            }
        }
    }
}

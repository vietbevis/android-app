package vn.vietbevis.apkbasic.data.recurring

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.RecurringTransaction
import vn.vietbevis.apkbasic.domain.repository.RecurringTransactionRepository

class SupabaseRecurringTransactionRepository(
    private val client: SupabaseClient,
) : RecurringTransactionRepository {
    override suspend fun listRecurringTransactions(includeArchived: Boolean): Result<List<RecurringTransaction>> = appResult {
        client.from("recurring_transactions")
            .select {
                if (!includeArchived) {
                    filter { eq("is_archived", false) }
                }
                order("next_run_at", Order.ASCENDING)
            }
            .decodeList<RecurringTransactionDto>()
            .map { it.toDomain() }
    }

    override suspend fun createRecurringTransaction(recurringTransaction: RecurringTransaction): Result<RecurringTransaction> = appResult {
        client.from("recurring_transactions").insert(recurringTransaction.toDto())
        readRecurringTransaction(recurringTransaction.id)
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction): Result<RecurringTransaction> = appResult {
        client.from("recurring_transactions").update(recurringTransaction.toDto()) {
            filter { eq("id", recurringTransaction.id) }
        }
        readRecurringTransaction(recurringTransaction.id)
    }

    override suspend fun archiveRecurringTransaction(recurringTransactionId: String): Result<Unit> = appResult {
        client.from("recurring_transactions").update({ set("is_archived", true) }) {
            filter { eq("id", recurringTransactionId) }
        }
        Unit
    }

    private suspend fun readRecurringTransaction(id: String): RecurringTransaction =
        client.from("recurring_transactions")
            .select { filter { eq("id", id) } }
            .decodeSingle<RecurringTransactionDto>()
            .toDomain()
}

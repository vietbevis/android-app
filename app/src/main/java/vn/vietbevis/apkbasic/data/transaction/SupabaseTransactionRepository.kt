package vn.vietbevis.apkbasic.data.transaction

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.DateCodecs
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository

class SupabaseTransactionRepository(
    private val client: SupabaseClient,
) : TransactionRepository {
    override suspend fun listTransactions(
        monthStartEpochMillis: Long,
        monthEndEpochMillis: Long,
    ): Result<List<Transaction>> = appResult {
        client.from("transactions")
            .select {
                filter {
                    gte("occurred_at", DateCodecs.epochMillisToIso(monthStartEpochMillis))
                    lt("occurred_at", DateCodecs.epochMillisToIso(monthEndEpochMillis))
                }
                order("occurred_at", Order.DESCENDING)
            }
            .decodeList<TransactionDto>()
            .map { it.toDomain(DateCodecs.isoToEpochMillis(it.occurredAt)) }
    }

    override suspend fun getTransaction(transactionId: String): Result<Transaction> = appResult {
        client.from("transactions")
            .select {
                filter { eq("id", transactionId) }
            }
            .decodeSingle<TransactionDto>()
            .let { it.toDomain(DateCodecs.isoToEpochMillis(it.occurredAt)) }
    }

    override suspend fun createTransaction(transaction: Transaction): Result<Transaction> = appResult {
        client.from("transactions")
            .insert(transaction.toDto(DateCodecs.epochMillisToIso(transaction.occurredAtEpochMillis)))
        readTransaction(transaction.id)
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> = appResult {
        client.from("transactions")
            .update(transaction.toDto(DateCodecs.epochMillisToIso(transaction.occurredAtEpochMillis))) {
                filter { eq("id", transaction.id) }
            }
        readTransaction(transaction.id)
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> = appResult {
        client.from("transactions")
            .delete {
                filter { eq("id", transactionId) }
        }
        Unit
    }

    private suspend fun readTransaction(transactionId: String): Transaction =
        client.from("transactions")
            .select {
                filter { eq("id", transactionId) }
            }
            .decodeSingle<TransactionDto>()
            .let { it.toDomain(DateCodecs.isoToEpochMillis(it.occurredAt)) }
}

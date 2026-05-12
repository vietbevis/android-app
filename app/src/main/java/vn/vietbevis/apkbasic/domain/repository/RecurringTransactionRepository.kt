package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.RecurringTransaction

interface RecurringTransactionRepository {
    suspend fun listRecurringTransactions(includeArchived: Boolean = false): Result<List<RecurringTransaction>>
    suspend fun createRecurringTransaction(recurringTransaction: RecurringTransaction): Result<RecurringTransaction>
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction): Result<RecurringTransaction>
    suspend fun archiveRecurringTransaction(recurringTransactionId: String): Result<Unit>
}

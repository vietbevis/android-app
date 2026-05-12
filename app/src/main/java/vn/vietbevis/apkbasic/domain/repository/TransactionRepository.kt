package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Transaction

interface TransactionRepository {
    suspend fun listTransactions(monthStartEpochMillis: Long, monthEndEpochMillis: Long): Result<List<Transaction>>
    suspend fun getTransaction(transactionId: String): Result<Transaction>
    suspend fun createTransaction(transaction: Transaction): Result<Transaction>
    suspend fun updateTransaction(transaction: Transaction): Result<Transaction>
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
}

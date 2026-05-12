package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Transfer

interface TransferRepository {
    suspend fun listTransfers(monthStartEpochMillis: Long, monthEndEpochMillis: Long): Result<List<Transfer>>
    suspend fun createTransfer(transfer: Transfer): Result<Transfer>
    suspend fun deleteTransfer(transferId: String): Result<Unit>
}

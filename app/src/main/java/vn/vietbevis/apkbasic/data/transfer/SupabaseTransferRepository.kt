package vn.vietbevis.apkbasic.data.transfer

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Transfer
import vn.vietbevis.apkbasic.domain.repository.TransferRepository

class SupabaseTransferRepository(
    private val client: SupabaseClient,
) : TransferRepository {
    override suspend fun listTransfers(monthStartEpochMillis: Long, monthEndEpochMillis: Long): Result<List<Transfer>> = appResult {
        client.from("transfers")
            .select {
                filter {
                    gte("occurred_at", vn.vietbevis.apkbasic.core.common.DateCodecs.epochMillisToIso(monthStartEpochMillis))
                    lt("occurred_at", vn.vietbevis.apkbasic.core.common.DateCodecs.epochMillisToIso(monthEndEpochMillis))
                }
                order("occurred_at", Order.DESCENDING)
            }
            .decodeList<TransferDto>()
            .map { it.toDomain() }
    }

    override suspend fun createTransfer(transfer: Transfer): Result<Transfer> = appResult {
        client.from("transfers").insert(transfer.toDto())
        transfer
    }

    override suspend fun deleteTransfer(transferId: String): Result<Unit> = appResult {
        client.from("transfers").delete {
            filter { eq("id", transferId) }
        }
        Unit
    }
}

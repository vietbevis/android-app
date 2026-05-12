package vn.vietbevis.apkbasic.data.wallet

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.repository.WalletRepository

class SupabaseWalletRepository(
    private val client: SupabaseClient,
) : WalletRepository {
    override suspend fun listWallets(includeArchived: Boolean): Result<List<Wallet>> = appResult {
        client.from("wallets")
            .select {
                if (!includeArchived) {
                    filter { eq("is_archived", false) }
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<WalletDto>()
            .map { it.toDomain() }
    }

    override suspend fun createWallet(wallet: Wallet): Result<Wallet> = appResult {
        client.from("wallets")
            .insert(wallet.toDto())
        readWallet(wallet.id)
    }

    override suspend fun updateWallet(wallet: Wallet): Result<Wallet> = appResult {
        client.from("wallets")
            .update(wallet.toDto()) {
                filter { eq("id", wallet.id) }
            }
        readWallet(wallet.id)
    }

    override suspend fun archiveWallet(walletId: String): Result<Unit> = appResult {
        client.from("wallets")
            .update({
                set("is_archived", true)
            }) {
                filter { eq("id", walletId) }
        }
        Unit
    }

    private suspend fun readWallet(walletId: String): Wallet =
        client.from("wallets")
            .select {
                filter { eq("id", walletId) }
            }
            .decodeSingle<WalletDto>()
            .toDomain()
}

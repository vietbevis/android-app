package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Wallet

interface WalletRepository {
    suspend fun listWallets(includeArchived: Boolean = false): Result<List<Wallet>>
    suspend fun createWallet(wallet: Wallet): Result<Wallet>
    suspend fun updateWallet(wallet: Wallet): Result<Wallet>
    suspend fun archiveWallet(walletId: String): Result<Unit>
}

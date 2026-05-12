package vn.vietbevis.apkbasic.domain.model

data class Wallet(
    val id: String,
    val userId: String,
    val name: String,
    val type: WalletType,
    val initialBalance: Money = Money.vnd(0),
    val isArchived: Boolean = false,
)

enum class WalletType {
    CASH,
    BANK,
    E_WALLET,
    OTHER,
}

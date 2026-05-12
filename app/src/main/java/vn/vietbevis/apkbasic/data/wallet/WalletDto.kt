package vn.vietbevis.apkbasic.data.wallet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.WalletType

@Serializable
data class WalletDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("initial_balance") val initialBalance: Double = 0.0,
    @SerialName("is_archived") val isArchived: Boolean = false,
)

fun WalletDto.toDomain(): Wallet = Wallet(
    id = id,
    userId = userId,
    name = name,
    type = type.toWalletType(),
    initialBalance = Money.vnd(initialBalance.toLong()),
    isArchived = isArchived,
)

fun Wallet.toDto(): WalletDto = WalletDto(
    id = id,
    userId = userId,
    name = name,
    type = type.toWireValue(),
    initialBalance = initialBalance.minorUnits.toDouble(),
    isArchived = isArchived,
)

fun String.toWalletType(): WalletType = when (this) {
    "cash" -> WalletType.CASH
    "bank" -> WalletType.BANK
    "e_wallet" -> WalletType.E_WALLET
    "other" -> WalletType.OTHER
    else -> error("Unsupported wallet type: $this")
}

fun WalletType.toWireValue(): String = when (this) {
    WalletType.CASH -> "cash"
    WalletType.BANK -> "bank"
    WalletType.E_WALLET -> "e_wallet"
    WalletType.OTHER -> "other"
}

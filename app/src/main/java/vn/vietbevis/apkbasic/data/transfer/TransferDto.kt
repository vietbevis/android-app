package vn.vietbevis.apkbasic.data.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.core.common.DateCodecs
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transfer

@Serializable
data class TransferDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("from_wallet_id") val fromWalletId: String,
    @SerialName("to_wallet_id") val toWalletId: String,
    @SerialName("amount") val amount: Double,
    @SerialName("fee") val fee: Double = 0.0,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("note") val note: String? = null,
)

fun TransferDto.toDomain(): Transfer = Transfer(
    id = id,
    userId = userId,
    fromWalletId = fromWalletId,
    toWalletId = toWalletId,
    amount = Money.vnd(amount.toLong()),
    fee = Money.vnd(fee.toLong()),
    occurredAtEpochMillis = DateCodecs.isoToEpochMillis(occurredAt),
    note = note,
)

fun Transfer.toDto(): TransferDto = TransferDto(
    id = id,
    userId = userId,
    fromWalletId = fromWalletId,
    toWalletId = toWalletId,
    amount = amount.minorUnits.toDouble(),
    fee = fee.minorUnits.toDouble(),
    occurredAt = DateCodecs.epochMillisToIso(occurredAtEpochMillis),
    note = note,
)

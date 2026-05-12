package vn.vietbevis.apkbasic.data.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.data.category.toTransactionType
import vn.vietbevis.apkbasic.data.category.toWireValue
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction

@Serializable
data class TransactionDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("wallet_id") val walletId: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("type") val type: String,
    @SerialName("amount") val amount: Double,
    @SerialName("note") val note: String? = null,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("photo_path") val photoPath: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("location_label") val locationLabel: String? = null,
)

fun TransactionDto.toDomain(occurredAtEpochMillis: Long): Transaction = Transaction(
    id = id,
    userId = userId,
    walletId = walletId,
    categoryId = categoryId,
    type = type.toTransactionType(),
    amount = Money.vnd(amount.toLong()),
    note = note,
    occurredAtEpochMillis = occurredAtEpochMillis,
    photoPath = photoPath,
    latitude = latitude,
    longitude = longitude,
    locationLabel = locationLabel,
)

fun Transaction.toDto(occurredAtIsoString: String): TransactionDto = TransactionDto(
    id = id,
    userId = userId,
    walletId = walletId,
    categoryId = categoryId,
    type = type.toWireValue(),
    amount = amount.minorUnits.toDouble(),
    note = note,
    occurredAt = occurredAtIsoString,
    photoPath = photoPath,
    latitude = latitude,
    longitude = longitude,
    locationLabel = locationLabel,
)

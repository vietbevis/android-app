package vn.vietbevis.apkbasic.data.investment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.Investment
import vn.vietbevis.apkbasic.domain.model.InvestmentStatus
import vn.vietbevis.apkbasic.domain.model.InvestmentType
import vn.vietbevis.apkbasic.domain.model.Money

@Serializable
data class InvestmentDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("principal") val principal: Double,
    @SerialName("current_value") val currentValue: Double? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("status") val status: String,
)

fun InvestmentDto.toDomain(): Investment = Investment(
    id = id,
    userId = userId,
    name = name,
    type = type.toInvestmentType(),
    principal = Money.vnd(principal.toLong()),
    currentValue = currentValue?.toLong()?.let(Money::vnd),
    note = note,
    status = status.toInvestmentStatus(),
)

fun Investment.toDto(): InvestmentDto = InvestmentDto(
    id = id,
    userId = userId,
    name = name,
    type = type.toWireValue(),
    principal = principal.minorUnits.toDouble(),
    currentValue = currentValue?.minorUnits?.toDouble(),
    note = note,
    status = status.toWireValue(),
)

private fun String.toInvestmentType(): InvestmentType = when (this) {
    "savings" -> InvestmentType.SAVINGS
    "stock" -> InvestmentType.STOCK
    "fund" -> InvestmentType.FUND
    "crypto" -> InvestmentType.CRYPTO
    "gold" -> InvestmentType.GOLD
    else -> InvestmentType.OTHER
}

private fun String.toInvestmentStatus(): InvestmentStatus = when (this) {
    "closed" -> InvestmentStatus.CLOSED
    "archived" -> InvestmentStatus.ARCHIVED
    else -> InvestmentStatus.ACTIVE
}

private fun InvestmentType.toWireValue(): String = when (this) {
    InvestmentType.SAVINGS -> "savings"
    InvestmentType.STOCK -> "stock"
    InvestmentType.FUND -> "fund"
    InvestmentType.CRYPTO -> "crypto"
    InvestmentType.GOLD -> "gold"
    InvestmentType.OTHER -> "other"
}

private fun InvestmentStatus.toWireValue(): String = when (this) {
    InvestmentStatus.ACTIVE -> "active"
    InvestmentStatus.CLOSED -> "closed"
    InvestmentStatus.ARCHIVED -> "archived"
}

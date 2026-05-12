package vn.vietbevis.apkbasic.data.category

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.TransactionType

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("transaction_type") val transactionType: String,
    @SerialName("icon") val icon: String? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("is_archived") val isArchived: Boolean = false,
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    userId = userId,
    name = name,
    transactionType = transactionType.toTransactionType(),
    icon = icon,
    color = color,
    isDefault = isDefault,
    isArchived = isArchived,
)

fun Category.toDto(): CategoryDto = CategoryDto(
    id = id,
    userId = userId,
    name = name,
    transactionType = transactionType.toWireValue(),
    icon = icon,
    color = color,
    isDefault = isDefault,
    isArchived = isArchived,
)

fun String.toTransactionType(): TransactionType = when (this) {
    "income" -> TransactionType.INCOME
    "expense" -> TransactionType.EXPENSE
    else -> error("Unsupported transaction type: $this")
}

fun TransactionType.toWireValue(): String = when (this) {
    TransactionType.INCOME -> "income"
    TransactionType.EXPENSE -> "expense"
}

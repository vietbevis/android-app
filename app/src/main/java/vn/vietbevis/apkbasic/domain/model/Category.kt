package vn.vietbevis.apkbasic.domain.model

data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val transactionType: TransactionType,
    val icon: String? = null,
    val color: String? = null,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
)

package vn.vietbevis.apkbasic.domain.model

data class Investment(
    val id: String,
    val userId: String,
    val name: String,
    val type: InvestmentType,
    val principal: Money,
    val currentValue: Money? = null,
    val note: String? = null,
    val status: InvestmentStatus = InvestmentStatus.ACTIVE,
)

enum class InvestmentType {
    SAVINGS,
    STOCK,
    FUND,
    CRYPTO,
    GOLD,
    OTHER,
}

enum class InvestmentStatus {
    ACTIVE,
    CLOSED,
    ARCHIVED,
}

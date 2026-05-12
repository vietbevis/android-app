package vn.vietbevis.apkbasic.domain.model

data class Loan(
    val id: String,
    val userId: String,
    val type: LoanType,
    val name: String,
    val counterpartyName: String? = null,
    val principal: Money,
    val interest: LoanInterest,
    val repaymentMethod: LoanRepaymentMethod,
    val startAtEpochMillis: Long,
    val dueAtEpochMillis: Long? = null,
    val status: LoanStatus = LoanStatus.ACTIVE,
    val note: String? = null,
)

enum class LoanType {
    BORROWED,
    LENT,
    INSTALLMENT,
}

data class LoanInterest(
    val inputMode: LoanInterestInputMode,
    val calculationMethod: LoanInterestCalculationMethod,
    val value: Money,
)

enum class LoanInterestInputMode {
    PERCENT_PER_YEAR,
    FIXED_AMOUNT,
}

enum class LoanInterestCalculationMethod {
    SIMPLE,
    DECLINING_BALANCE,
    INITIAL_BALANCE,
    ACTUAL_DAYS,
}

enum class LoanRepaymentMethod {
    MANUAL,
    MONTHLY,
    CUSTOM,
}

enum class LoanStatus {
    ACTIVE,
    PAID,
    ARCHIVED,
}

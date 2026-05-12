package vn.vietbevis.apkbasic.data.loan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.Loan
import vn.vietbevis.apkbasic.domain.model.LoanInterest
import vn.vietbevis.apkbasic.domain.model.LoanInterestCalculationMethod
import vn.vietbevis.apkbasic.domain.model.LoanInterestInputMode
import vn.vietbevis.apkbasic.domain.model.LoanRepaymentMethod
import vn.vietbevis.apkbasic.domain.model.LoanStatus
import vn.vietbevis.apkbasic.domain.model.LoanType
import vn.vietbevis.apkbasic.domain.model.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable
data class LoanDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("type") val type: String,
    @SerialName("name") val name: String,
    @SerialName("counterparty_name") val counterpartyName: String? = null,
    @SerialName("principal") val principal: Double,
    @SerialName("interest_input_mode") val interestInputMode: String,
    @SerialName("interest_method") val interestMethod: String,
    @SerialName("interest_value") val interestValue: Double,
    @SerialName("repayment_method") val repaymentMethod: String,
    @SerialName("start_at") val startAt: String,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("status") val status: String,
    @SerialName("note") val note: String? = null,
)

fun LoanDto.toDomain(): Loan = Loan(
    id = id,
    userId = userId,
    type = type.toLoanType(),
    name = name,
    counterpartyName = counterpartyName,
    principal = Money.vnd(principal.toLong()),
    interest = LoanInterest(
        inputMode = interestInputMode.toLoanInterestInputMode(),
        calculationMethod = interestMethod.toLoanInterestCalculationMethod(),
        value = Money.vnd(interestValue.toLong()),
    ),
    repaymentMethod = repaymentMethod.toLoanRepaymentMethod(),
    startAtEpochMillis = startAt.dateToEpochMillis(),
    dueAtEpochMillis = dueAt?.dateToEpochMillis(),
    status = status.toLoanStatus(),
    note = note,
)

fun Loan.toDto(): LoanDto = LoanDto(
    id = id,
    userId = userId,
    type = type.toWireValue(),
    name = name,
    counterpartyName = counterpartyName,
    principal = principal.minorUnits.toDouble(),
    interestInputMode = interest.inputMode.toWireValue(),
    interestMethod = interest.calculationMethod.toWireValue(),
    interestValue = interest.value.minorUnits.toDouble(),
    repaymentMethod = repaymentMethod.toWireValue(),
    startAt = startAtEpochMillis.epochMillisToDate(),
    dueAt = dueAtEpochMillis?.epochMillisToDate(),
    status = status.toWireValue(),
    note = note,
)

private fun String.toLoanType(): LoanType = when (this) {
    "borrowed" -> LoanType.BORROWED
    "lent" -> LoanType.LENT
    "installment" -> LoanType.INSTALLMENT
    else -> LoanType.BORROWED
}

private fun String.toLoanInterestInputMode(): LoanInterestInputMode = when (this) {
    "fixed_amount" -> LoanInterestInputMode.FIXED_AMOUNT
    else -> LoanInterestInputMode.PERCENT_PER_YEAR
}

private fun String.toLoanInterestCalculationMethod(): LoanInterestCalculationMethod = when (this) {
    "declining_balance" -> LoanInterestCalculationMethod.DECLINING_BALANCE
    "initial_balance" -> LoanInterestCalculationMethod.INITIAL_BALANCE
    "actual_days" -> LoanInterestCalculationMethod.ACTUAL_DAYS
    else -> LoanInterestCalculationMethod.SIMPLE
}

private fun String.toLoanRepaymentMethod(): LoanRepaymentMethod = when (this) {
    "monthly" -> LoanRepaymentMethod.MONTHLY
    "custom" -> LoanRepaymentMethod.CUSTOM
    else -> LoanRepaymentMethod.MANUAL
}

private fun String.toLoanStatus(): LoanStatus = when (this) {
    "paid" -> LoanStatus.PAID
    "archived" -> LoanStatus.ARCHIVED
    else -> LoanStatus.ACTIVE
}

private fun LoanType.toWireValue(): String = when (this) {
    LoanType.BORROWED -> "borrowed"
    LoanType.LENT -> "lent"
    LoanType.INSTALLMENT -> "installment"
}

private fun LoanInterestInputMode.toWireValue(): String = when (this) {
    LoanInterestInputMode.PERCENT_PER_YEAR -> "percent_per_year"
    LoanInterestInputMode.FIXED_AMOUNT -> "fixed_amount"
}

private fun LoanInterestCalculationMethod.toWireValue(): String = when (this) {
    LoanInterestCalculationMethod.SIMPLE -> "simple"
    LoanInterestCalculationMethod.DECLINING_BALANCE -> "declining_balance"
    LoanInterestCalculationMethod.INITIAL_BALANCE -> "initial_balance"
    LoanInterestCalculationMethod.ACTUAL_DAYS -> "actual_days"
}

private fun LoanRepaymentMethod.toWireValue(): String = when (this) {
    LoanRepaymentMethod.MANUAL -> "manual"
    LoanRepaymentMethod.MONTHLY -> "monthly"
    LoanRepaymentMethod.CUSTOM -> "custom"
}

private fun LoanStatus.toWireValue(): String = when (this) {
    LoanStatus.ACTIVE -> "active"
    LoanStatus.PAID -> "paid"
    LoanStatus.ARCHIVED -> "archived"
}

private val dateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

private fun String.dateToEpochMillis(): Long = runCatching { dateFormatter.parse(this)?.time ?: 0L }.getOrDefault(0L)

private fun Long.epochMillisToDate(): String = dateFormatter.format(Date(this))

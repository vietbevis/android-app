package vn.vietbevis.apkbasic.domain.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class Money(
    val minorUnits: Long,
    val currency: String = DEFAULT_CURRENCY,
) {
    init {
        require(currency.isNotBlank()) { "Currency must not be blank." }
    }

    val isPositive: Boolean = minorUnits > 0

    fun formatVnd(): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '.'
        }
        return DecimalFormat("#,###", symbols).format(minorUnits) + " d"
    }

    companion object {
        const val DEFAULT_CURRENCY = "VND"

        fun vnd(amount: Long): Money = Money(amount, DEFAULT_CURRENCY)
    }
}

package vn.vietbevis.apkbasic.feature.capture

import java.math.BigDecimal
import java.math.RoundingMode

data class CalculatorState(
    val display: String = "0",
    val storedValue: BigDecimal? = null,
    val pendingOperator: CalculatorOperator? = null,
    val resetDisplayOnDigit: Boolean = false,
) {
    val amountInput: String
        get() = display.substringBefore(".").filter(Char::isDigit).trimStart('0').ifBlank { "0" }
}

enum class CalculatorOperator(val symbol: String) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("x"),
    DIVIDE("÷"),
}

sealed interface CalculatorKey {
    data class Digit(val value: String) : CalculatorKey
    data object Decimal : CalculatorKey
    data object TripleZero : CalculatorKey
    data object Clear : CalculatorKey
    data object Backspace : CalculatorKey
    data class Operator(val operator: CalculatorOperator) : CalculatorKey
    data object Equals : CalculatorKey
}

object CalculatorEngine {
    fun press(state: CalculatorState, key: CalculatorKey): CalculatorState = when (key) {
        CalculatorKey.Backspace -> backspace(state)
        CalculatorKey.Clear -> CalculatorState()
        CalculatorKey.Decimal -> appendDecimal(state)
        CalculatorKey.Equals -> resolve(state)
        CalculatorKey.TripleZero -> appendDigit(state, "000")
        is CalculatorKey.Digit -> appendDigit(state, key.value)
        is CalculatorKey.Operator -> applyOperator(state, key.operator)
    }

    private fun appendDigit(state: CalculatorState, digit: String): CalculatorState {
        val base = if (state.resetDisplayOnDigit) "0" else state.display
        val next = if (base == "0") digit.trimStart('0').ifBlank { "0" } else base + digit
        return state.copy(display = next.take(12), resetDisplayOnDigit = false)
    }

    private fun appendDecimal(state: CalculatorState): CalculatorState {
        if (state.display.contains(".")) return state
        return state.copy(display = state.display + ".", resetDisplayOnDigit = false)
    }

    private fun backspace(state: CalculatorState): CalculatorState {
        if (state.resetDisplayOnDigit) return state.copy(display = "0", resetDisplayOnDigit = false)
        val next = state.display.dropLast(1).ifBlank { "0" }
        return state.copy(display = next)
    }

    private fun applyOperator(state: CalculatorState, operator: CalculatorOperator): CalculatorState {
        val resolved = resolve(state)
        return resolved.copy(
            storedValue = resolved.display.toBigDecimalOrZero(),
            pendingOperator = operator,
            resetDisplayOnDigit = true,
        )
    }

    private fun resolve(state: CalculatorState): CalculatorState {
        val stored = state.storedValue ?: return state
        val operator = state.pendingOperator ?: return state
        val current = state.display.toBigDecimalOrZero()
        val result = when (operator) {
            CalculatorOperator.ADD -> stored + current
            CalculatorOperator.SUBTRACT -> stored - current
            CalculatorOperator.MULTIPLY -> stored * current
            CalculatorOperator.DIVIDE -> if (current.compareTo(BigDecimal.ZERO) == 0) stored else stored.divide(current, 2, RoundingMode.HALF_UP)
        }.max(BigDecimal.ZERO)
        return state.copy(
            display = result.stripTrailingZeros().toPlainString().substringBefore(".").take(12).ifBlank { "0" },
            storedValue = null,
            pendingOperator = null,
            resetDisplayOnDigit = true,
        )
    }

    private fun String.toBigDecimalOrZero(): BigDecimal =
        runCatching { BigDecimal(this) }.getOrDefault(BigDecimal.ZERO)
}

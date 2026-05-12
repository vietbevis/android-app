package vn.vietbevis.apkbasic.feature.capture

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {
    @Test
    fun digitsBuildAmount() {
        val state = CalculatorState()
            .press(CalculatorKey.Digit("1"))
            .press(CalculatorKey.Digit("5"))
            .press(CalculatorKey.TripleZero)

        assertEquals("15000", state.amountInput)
    }

    @Test
    fun additionResolvesToPositiveAmount() {
        val state = CalculatorState()
            .press(CalculatorKey.Digit("1"))
            .press(CalculatorKey.TripleZero)
            .press(CalculatorKey.Operator(CalculatorOperator.ADD))
            .press(CalculatorKey.Digit("5"))
            .press(CalculatorKey.TripleZero)
            .press(CalculatorKey.Equals)

        assertEquals("6000", state.amountInput)
    }

    @Test
    fun subtractionNeverReturnsNegativeAmount() {
        val state = CalculatorState()
            .press(CalculatorKey.Digit("5"))
            .press(CalculatorKey.Operator(CalculatorOperator.SUBTRACT))
            .press(CalculatorKey.Digit("9"))
            .press(CalculatorKey.Equals)

        assertEquals("0", state.amountInput)
    }

    @Test
    fun backspaceClearsLastDigit() {
        val state = CalculatorState()
            .press(CalculatorKey.Digit("1"))
            .press(CalculatorKey.Digit("2"))
            .press(CalculatorKey.Backspace)

        assertEquals("1", state.amountInput)
    }

    private fun CalculatorState.press(key: CalculatorKey): CalculatorState =
        CalculatorEngine.press(this, key)
}

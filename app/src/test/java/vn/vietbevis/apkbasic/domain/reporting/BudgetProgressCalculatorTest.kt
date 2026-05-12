package vn.vietbevis.apkbasic.domain.reporting

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.vietbevis.apkbasic.domain.model.Money

class BudgetProgressCalculatorTest {
    @Test
    fun calculatesRemainingPercentAndDailyAllowance() {
        val progress = BudgetProgressCalculator.calculate(
            budget = Money.vnd(100_000),
            spent = Money.vnd(30_000),
            daysRemaining = 20,
        )

        assertEquals(70_000, progress.remaining.minorUnits)
        assertEquals(30, progress.percentSpent)
        assertEquals(3_500, progress.dailyAllowance.minorUnits)
    }

    @Test
    fun overBudgetKeepsRemainingAndDailyAllowanceAtZero() {
        val progress = BudgetProgressCalculator.calculate(
            budget = Money.vnd(100_000),
            spent = Money.vnd(130_000),
            daysRemaining = 5,
        )

        assertEquals(0, progress.remaining.minorUnits)
        assertEquals(130, progress.percentSpent)
        assertEquals(0, progress.dailyAllowance.minorUnits)
    }
}

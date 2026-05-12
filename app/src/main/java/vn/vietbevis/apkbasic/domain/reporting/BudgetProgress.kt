package vn.vietbevis.apkbasic.domain.reporting

import vn.vietbevis.apkbasic.domain.model.Money

data class BudgetProgress(
    val spent: Money,
    val budget: Money,
    val remaining: Money,
    val percentSpent: Int,
    val dailyAllowance: Money,
)

object BudgetProgressCalculator {
    fun calculate(
        budget: Money,
        spent: Money,
        daysRemaining: Int,
    ): BudgetProgress {
        val remainingMinor = (budget.minorUnits - spent.minorUnits).coerceAtLeast(0)
        val percent = if (budget.minorUnits <= 0) {
            0
        } else {
            ((spent.minorUnits.toDouble() / budget.minorUnits.toDouble()) * 100).toInt().coerceAtMost(999)
        }
        val safeDays = daysRemaining.coerceAtLeast(1)
        return BudgetProgress(
            spent = spent,
            budget = budget,
            remaining = Money.vnd(remainingMinor),
            percentSpent = percent,
            dailyAllowance = Money.vnd(remainingMinor / safeDays),
        )
    }
}

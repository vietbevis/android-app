package vn.vietbevis.apkbasic.domain.reporting

import vn.vietbevis.apkbasic.domain.model.Money

data class BudgetProgress(
    val spent: Money,
    val budget: Money,
    val remaining: Money,
    val percentSpent: Int,
    val dailyAllowance: Money,
    val weeklyAllowance: Money? = null,
)

object BudgetProgressCalculator {
    fun calculate(
        budget: Money,
        spent: Money,
        daysRemaining: Int,
        daysLeftInWeek: Int? = null,
    ): BudgetProgress {
        val remainingMinor = (budget.minorUnits - spent.minorUnits).coerceAtLeast(0)
        val percent = if (budget.minorUnits <= 0) {
            0
        } else {
            ((spent.minorUnits.toDouble() / budget.minorUnits.toDouble()) * 100).toInt().coerceAtMost(999)
        }
        val safeDays = daysRemaining.coerceAtLeast(1)
        val daily = remainingMinor / safeDays
        
        val weekly = if (daysLeftInWeek != null) {
            val daysToAccountFor = minOf(daysLeftInWeek, safeDays)
            Money.vnd(daily * daysToAccountFor)
        } else null

        return BudgetProgress(
            spent = spent,
            budget = budget,
            remaining = Money.vnd(remainingMinor),
            percentSpent = percent,
            dailyAllowance = Money.vnd(daily),
            weeklyAllowance = weekly
        )
    }
}

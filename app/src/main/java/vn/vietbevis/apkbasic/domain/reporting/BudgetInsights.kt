package vn.vietbevis.apkbasic.domain.reporting

import vn.vietbevis.apkbasic.domain.model.Money
import java.util.Calendar

data class BudgetInsights(
    val monthlyLimit: Money,
    val totalSpent: Money,
    val remainingMonth: Money,
    val percentSpent: Int,
    val isExceeded: Boolean,
    val exceededAmount: Money,
    val suggestedWeekly: Money,
    val suggestedDaily: Money,
)

object BudgetInsightsCalculator {
    fun calculate(
        monthlyLimit: Money,
        totalSpent: Money,
        now: Long = System.currentTimeMillis()
    ): BudgetInsights {
        val remaining = (monthlyLimit.minorUnits - totalSpent.minorUnits).coerceAtLeast(0L)
        val isExceeded = totalSpent.minorUnits > monthlyLimit.minorUnits
        val exceededAmount = if (isExceeded) totalSpent.minorUnits - monthlyLimit.minorUnits else 0L
        
        val percent = if (monthlyLimit.minorUnits > 0) {
            ((totalSpent.minorUnits.toDouble() / monthlyLimit.minorUnits.toDouble()) * 100).toInt()
        } else 0

        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)
        
        val weeksRemaining = (daysRemaining / 7.0).coerceAtLeast(1.0)
        
        return BudgetInsights(
            monthlyLimit = monthlyLimit,
            totalSpent = totalSpent,
            remainingMonth = Money.vnd(remaining),
            percentSpent = percent,
            isExceeded = isExceeded,
            exceededAmount = Money.vnd(exceededAmount),
            suggestedWeekly = Money.vnd((remaining / weeksRemaining).toLong()),
            suggestedDaily = Money.vnd(remaining / daysRemaining)
        )
    }
}

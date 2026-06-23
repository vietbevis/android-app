package vn.vietbevis.apkbasic.domain.reporting

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.vietbevis.apkbasic.domain.model.Money
import java.util.Calendar

class BudgetInsightsCalculatorTest {

    @Test
    fun calculatesDailyAndWeeklyBudgetCorrectly() {
        // Assume monthly limit 3,100,000, spent 0
        // Total remaining = 3,100,000
        // If it's the 1st of a 31-day month, days remaining = 31
        // Daily = 3,100,000 / 31 = 100,000
        
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MAY, 1, 12, 0) // May 1st, 2024 is Wednesday
        val now = calendar.timeInMillis
        
        // Wed=4, Thu=5, Fri=6, Sat=7, Sun=1.
        // Days left in week (Wed-Sun) = 5
        
        val insights = BudgetInsightsCalculator.calculate(
            monthlyLimit = Money.vnd(3_100_000),
            totalSpent = Money.vnd(0),
            now = now
        )
        
        assertEquals(100_000, insights.suggestedDaily.minorUnits)
        assertEquals(500_000, insights.suggestedWeekly.minorUnits)
    }

    @Test
    fun calculatesWeeklyBudgetAtEndOfWeek() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MAY, 5, 12, 0) // May 5th, 2024 is Sunday
        val now = calendar.timeInMillis
        
        // Days remaining in month (May has 31 days): 31 - 5 + 1 = 27
        // Days left in week: 1 (Sunday)
        
        val insights = BudgetInsightsCalculator.calculate(
            monthlyLimit = Money.vnd(270_000),
            totalSpent = Money.vnd(0),
            now = now
        )
        
        assertEquals(10_000, insights.suggestedDaily.minorUnits)
        assertEquals(10_000, insights.suggestedWeekly.minorUnits)
    }

    @Test
    fun weeklyBudgetLimitedByEndOfMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MAY, 30, 12, 0) // May 30th, 2024 is Thursday
        val now = calendar.timeInMillis
        
        // Days remaining in month: 31 - 30 + 1 = 2
        // Days left in week: Thu(4), Fri(3), Sat(2), Sun(1) -> 4
        // min(4, 2) = 2
        
        val insights = BudgetInsightsCalculator.calculate(
            monthlyLimit = Money.vnd(20_000),
            totalSpent = Money.vnd(0),
            now = now
        )
        
        assertEquals(10_000, insights.suggestedDaily.minorUnits)
        assertEquals(20_000, insights.suggestedWeekly.minorUnits)
    }
}

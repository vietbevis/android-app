package vn.vietbevis.apkbasic.domain.service

import android.util.Log
import vn.vietbevis.apkbasic.core.notification.BudgetNotificationHelper
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository
import java.util.Calendar
import java.util.Locale

class BudgetMonitor(
    private val budgetRepository: BudgetRepository,
    private val notificationHelper: BudgetNotificationHelper,
) {
    suspend fun checkBudgetsAfterTransaction(transaction: Transaction) {
        Log.d("BudgetMonitor", "Checking budgets for transaction: ${transaction.id}, amount: ${transaction.amount.formatVnd()}")
        if (transaction.type != TransactionType.EXPENSE) {
            Log.d("BudgetMonitor", "Not an expense, skipping")
            return
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = transaction.occurredAtEpochMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthStart = calendar.timeInMillis
        val monthStr = String.format(Locale.US, "%04d-%02d-01", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        val budgetResult = budgetRepository.getMonthlyBudget(transaction.userId, monthStr)
        val monthlyBudget = budgetResult.getOrNull() ?: run {
            Log.d("BudgetMonitor", "No budget set for $monthStr, skipping check")
            return
        }

        // 1. Check Monthly Total Budget
        val totalSpentResult = budgetRepository.getSpentAmount(transaction.userId, null, monthStart, monthEnd)
        totalSpentResult.onSuccess { totalSpent ->
            if (totalSpent.minorUnits > monthlyBudget.amount.minorUnits) {
                notificationHelper.showBudgetExceededNotification(
                    budgetName = "Ngân sách tổng tháng ${calendar.get(Calendar.MONTH) + 1}",
                    limit = monthlyBudget.amount,
                    spent = totalSpent
                )
            }
        }

        // 2. Check Category Budget if specific category was used
        transaction.categoryId?.let { catId ->
            val catBudget = monthlyBudget.categoryBudgets.find { it.categoryId == catId }
            if (catBudget != null) {
                val catSpentResult = budgetRepository.getSpentAmount(transaction.userId, catId, monthStart, monthEnd)
                catSpentResult.onSuccess { catSpent ->
                    if (catSpent.minorUnits > catBudget.amount.minorUnits) {
                        notificationHelper.showBudgetExceededNotification(
                            budgetName = "Danh mục chi tiêu",
                            limit = catBudget.amount,
                            spent = catSpent
                        )
                    }
                }
            }
        }
    }
}

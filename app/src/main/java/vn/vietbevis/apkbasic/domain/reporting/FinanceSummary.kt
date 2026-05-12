package vn.vietbevis.apkbasic.domain.reporting

import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.Wallet

data class FinanceSummary(
    val income: Money,
    val expense: Money,
    val netChange: Money,
    val walletBalances: List<WalletBalance>,
    val expenseByCategory: List<CategoryTotal>,
    val recentTransactions: List<Transaction>,
)

data class WalletBalance(
    val wallet: Wallet,
    val balance: Money,
)

data class CategoryTotal(
    val category: Category?,
    val amount: Money,
)

object FinanceSummaryCalculator {
    fun summarize(
        transactions: List<Transaction>,
        wallets: List<Wallet>,
        categories: List<Category>,
        recentLimit: Int = 5,
    ): FinanceSummary {
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount.minorUnits }
        val expense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount.minorUnits }

        val categoryById = categories.associateBy { it.id }
        val expenseByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (categoryId, categoryTransactions) ->
                CategoryTotal(
                    category = categoryId?.let(categoryById::get),
                    amount = Money.vnd(categoryTransactions.sumOf { it.amount.minorUnits }),
                )
            }
            .sortedByDescending { it.amount.minorUnits }

        val walletBalances = wallets.map { wallet ->
            val walletDelta = transactions
                .filter { it.walletId == wallet.id }
                .sumOf {
                    when (it.type) {
                        TransactionType.INCOME -> it.amount.minorUnits
                        TransactionType.EXPENSE -> -it.amount.minorUnits
                    }
                }
            WalletBalance(wallet = wallet, balance = Money.vnd(wallet.initialBalance.minorUnits + walletDelta))
        }

        return FinanceSummary(
            income = Money.vnd(income),
            expense = Money.vnd(expense),
            netChange = Money.vnd(income - expense),
            walletBalances = walletBalances,
            expenseByCategory = expenseByCategory,
            recentTransactions = transactions
                .sortedByDescending { it.occurredAtEpochMillis }
                .take(recentLimit),
        )
    }
}

package vn.vietbevis.apkbasic.domain.reporting

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.WalletType

class FinanceSummaryCalculatorTest {
    @Test
    fun summarizesIncomeExpenseAndBalances() {
        val wallet = Wallet(
            id = "wallet-1",
            userId = "user-1",
            name = "Tien mat",
            type = WalletType.CASH,
            initialBalance = Money.vnd(100_000),
        )
        val food = Category(
            id = "cat-food",
            userId = "user-1",
            name = "An uong",
            transactionType = TransactionType.EXPENSE,
        )

        val summary = FinanceSummaryCalculator.summarize(
            transactions = listOf(
                transaction("income", wallet.id, null, TransactionType.INCOME, 500_000),
                transaction("expense-1", wallet.id, food.id, TransactionType.EXPENSE, 45_000),
                transaction("expense-2", wallet.id, food.id, TransactionType.EXPENSE, 55_000),
            ),
            wallets = listOf(wallet),
            categories = listOf(food),
        )

        assertEquals(500_000, summary.income.minorUnits)
        assertEquals(100_000, summary.expense.minorUnits)
        assertEquals(400_000, summary.netChange.minorUnits)
        assertEquals(500_000, summary.walletBalances.single().balance.minorUnits)
        assertEquals(100_000, summary.expenseByCategory.single().amount.minorUnits)
        assertEquals(food, summary.expenseByCategory.single().category)
    }

    private fun transaction(
        id: String,
        walletId: String,
        categoryId: String?,
        type: TransactionType,
        amount: Long,
    ) = Transaction(
        id = id,
        userId = "user-1",
        walletId = walletId,
        categoryId = categoryId,
        type = type,
        amount = Money.vnd(amount),
        note = null,
        occurredAtEpochMillis = id.length.toLong(),
        photoPath = null,
    )
}

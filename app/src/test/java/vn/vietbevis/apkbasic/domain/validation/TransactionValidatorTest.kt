package vn.vietbevis.apkbasic.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.WalletType

class TransactionValidatorTest {
    @Test
    fun validTransactionReturnsNoErrors() {
        val transaction = transaction()
        val wallet = wallet()
        val category = category()

        assertTrue(TransactionValidator.validate(transaction, wallet, category).isEmpty())
    }

    @Test
    fun rejectsInvalidAmountAndMissingWallet() {
        val transaction = transaction(amount = Money.vnd(0))

        assertEquals(
            listOf(
                TransactionValidationError.InvalidAmount,
                TransactionValidationError.MissingWallet,
            ),
            TransactionValidator.validate(transaction, wallet = null, category = null),
        )
    }

    @Test
    fun rejectsCategoryTypeMismatch() {
        val errors = TransactionValidator.validate(
            transaction = transaction(type = TransactionType.EXPENSE),
            wallet = wallet(),
            category = category(transactionType = TransactionType.INCOME),
        )

        assertEquals(listOf(TransactionValidationError.CategoryTypeMismatch), errors)
    }

    private fun transaction(
        type: TransactionType = TransactionType.EXPENSE,
        amount: Money = Money.vnd(45_000),
    ) = Transaction(
        id = "transaction-id",
        userId = "user-id",
        walletId = "wallet-id",
        categoryId = "category-id",
        type = type,
        amount = amount,
        note = "Coffee",
        occurredAtEpochMillis = 1_700_000_000_000,
        photoPath = null,
    )

    private fun wallet() = Wallet(
        id = "wallet-id",
        userId = "user-id",
        name = "Tien mat",
        type = WalletType.CASH,
    )

    private fun category(
        transactionType: TransactionType = TransactionType.EXPENSE,
    ) = Category(
        id = "category-id",
        userId = "user-id",
        name = "An uong",
        transactionType = transactionType,
    )
}

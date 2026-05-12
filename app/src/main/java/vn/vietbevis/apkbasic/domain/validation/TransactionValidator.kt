package vn.vietbevis.apkbasic.domain.validation

import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.Wallet

object TransactionValidator {
    fun validate(
        transaction: Transaction,
        wallet: Wallet?,
        category: Category?,
    ): List<TransactionValidationError> {
        val errors = mutableListOf<TransactionValidationError>()

        if (transaction.userId.isBlank()) errors += TransactionValidationError.MissingUser
        if (!transaction.amount.isPositive) errors += TransactionValidationError.InvalidAmount
        if (wallet == null) {
            errors += TransactionValidationError.MissingWallet
        } else {
            if (wallet.isArchived) errors += TransactionValidationError.ArchivedWallet
            if (wallet.userId != transaction.userId) errors += TransactionValidationError.WalletBelongsToAnotherUser
        }

        if (category != null) {
            if (category.isArchived) errors += TransactionValidationError.ArchivedCategory
            if (category.userId != transaction.userId) errors += TransactionValidationError.CategoryBelongsToAnotherUser
            if (category.transactionType != transaction.type) errors += TransactionValidationError.CategoryTypeMismatch
        }

        if (transaction.occurredAtEpochMillis <= 0) errors += TransactionValidationError.InvalidOccurredAt

        return errors
    }
}

enum class TransactionValidationError {
    MissingUser,
    InvalidAmount,
    MissingWallet,
    ArchivedWallet,
    WalletBelongsToAnotherUser,
    ArchivedCategory,
    CategoryBelongsToAnotherUser,
    CategoryTypeMismatch,
    InvalidOccurredAt,
}

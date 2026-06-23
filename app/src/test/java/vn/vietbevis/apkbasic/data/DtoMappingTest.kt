package vn.vietbevis.apkbasic.data

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.vietbevis.apkbasic.data.category.CategoryDto
import vn.vietbevis.apkbasic.data.category.toDomain
import vn.vietbevis.apkbasic.data.category.toDto
import vn.vietbevis.apkbasic.data.transaction.TransactionDto
import vn.vietbevis.apkbasic.data.transaction.toDomain
import vn.vietbevis.apkbasic.data.transaction.toDto
import vn.vietbevis.apkbasic.data.wallet.WalletDto
import vn.vietbevis.apkbasic.data.wallet.toDomain
import vn.vietbevis.apkbasic.data.wallet.toDto
import vn.vietbevis.apkbasic.data.profile.ProfileDto
import vn.vietbevis.apkbasic.data.profile.toDomain
import vn.vietbevis.apkbasic.data.profile.toDto
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.Transaction
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.WalletType

class DtoMappingTest {
    @Test
    fun profileDtoMapsToDomainAndBack() {
        val dto = ProfileDto(
            id = "user-id",
            displayName = "Viet Hoang",
            avatar = "https://example.com/avatar.jpg",
            updatedAt = "2026-05-20T14:00:00.123Z"
        )

        val domain = dto.toDomain()

        assertEquals("Viet Hoang", domain.displayName)
        assertEquals("https://example.com/avatar.jpg", domain.avatar)
        assertEquals(1779285600123L, domain.updatedAt)
        
        assertEquals(dto, domain.toDto())
    }

    @Test
    fun walletDtoMapsToDomainAndBack() {
        val dto = WalletDto(
            id = "wallet-id",
            userId = "user-id",
            name = "Tien mat",
            type = "cash",
            initialBalance = 100_000.0,
        )

        val domain = dto.toDomain()

        assertEquals(WalletType.CASH, domain.type)
        assertEquals(Money.vnd(100_000), domain.initialBalance)
        assertEquals(dto, domain.toDto())
    }

    @Test
    fun categoryDtoMapsTransactionType() {
        val domain = CategoryDto(
            id = "category-id",
            userId = "user-id",
            name = "An uong",
            transactionType = "expense",
        ).toDomain()

        assertEquals(TransactionType.EXPENSE, domain.transactionType)
        assertEquals("expense", domain.toDto().transactionType)
    }

    @Test
    fun transactionDtoMapsMoneyAndType() {
        val dto = TransactionDto(
            id = "transaction-id",
            userId = "user-id",
            walletId = "wallet-id",
            categoryId = "category-id",
            type = "expense",
            amount = 45_000.0,
            note = "Coffee",
            occurredAt = "2026-05-12T00:00:00Z",
            photoPath = "user-id/transaction-id/photo.jpg",
        )

        val domain = dto.toDomain(
            occurredAtEpochMillis = 1_777_000_000_000,
            updatedAtEpochMillis = 0L
        )

        assertEquals(TransactionType.EXPENSE, domain.type)
        assertEquals(Money.vnd(45_000), domain.amount)
        assertEquals(dto, domain.toDto("2026-05-12T00:00:00Z"))
    }
}

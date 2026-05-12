package vn.vietbevis.apkbasic.data.bootstrap

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.data.profile.ProfileDto
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.Wallet
import vn.vietbevis.apkbasic.domain.model.WalletType
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import java.util.UUID

class OnboardingBootstrapper(
    private val supabaseClient: SupabaseClient,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend fun ensureDefaults(profile: UserProfile): Result<Unit> = appResult {
        supabaseClient.from("profiles").upsert(ProfileDto(id = profile.id))

        val wallets = walletRepository.listWallets(includeArchived = false).getOrThrow()
        if (wallets.isEmpty()) {
            walletRepository.createWallet(
                Wallet(
                    id = UUID.randomUUID().toString(),
                    userId = profile.id,
                    name = "Tiền mặt",
                    type = WalletType.CASH,
                    initialBalance = Money.vnd(0),
                ),
            ).getOrThrow()
        }

        val categories = categoryRepository.listCategories().getOrThrow()
        val existingNames = categories.map { it.name to it.transactionType }.toSet()
        defaultCategories(profile.id)
            .filterNot { it.name to it.transactionType in existingNames }
            .forEach { categoryRepository.createCategory(it).getOrThrow() }
    }

    private fun defaultCategories(userId: String): List<Category> = listOf(
        Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Ăn uống",
            transactionType = TransactionType.EXPENSE,
            icon = "restaurant",
            color = "#E85D75",
            isDefault = true,
        ),
        Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Di chuyển",
            transactionType = TransactionType.EXPENSE,
            icon = "directions_car",
            color = "#2F80ED",
            isDefault = true,
        ),
        Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Mua sắm",
            transactionType = TransactionType.EXPENSE,
            icon = "shopping_bag",
            color = "#F2994A",
            isDefault = true,
        ),
        Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Hóa đơn",
            transactionType = TransactionType.EXPENSE,
            icon = "receipt",
            color = "#9B51E0",
            isDefault = true,
        ),
        Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Lương",
            transactionType = TransactionType.INCOME,
            icon = "payments",
            color = "#219653",
            isDefault = true,
        ),
        Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Thu khác",
            transactionType = TransactionType.INCOME,
            icon = "add_card",
            color = "#00A3A3",
            isDefault = true,
        ),
    )
}

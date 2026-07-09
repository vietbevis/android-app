package vn.vietbevis.apkbasic.core.di

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import vn.vietbevis.apkbasic.core.notification.BudgetNotificationHelper
import vn.vietbevis.apkbasic.core.supabase.SupabaseProvider
import vn.vietbevis.apkbasic.data.auth.SupabaseAuthRepository
import vn.vietbevis.apkbasic.data.bootstrap.OnboardingBootstrapper
import vn.vietbevis.apkbasic.data.budget.SupabaseBudgetRepository
import vn.vietbevis.apkbasic.data.category.SupabaseCategoryRepository
import vn.vietbevis.apkbasic.data.photo.SupabasePhotoRepository
import vn.vietbevis.apkbasic.data.preference.SupabaseUserPreferenceRepository
import vn.vietbevis.apkbasic.data.transaction.SupabaseTransactionRepository
import vn.vietbevis.apkbasic.data.wallet.SupabaseWalletRepository
import vn.vietbevis.apkbasic.domain.repository.AuthRepository
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.PhotoRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.UserPreferenceRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository
import vn.vietbevis.apkbasic.domain.service.BudgetMonitor

class AppContainer(
    val context: Context,
    val supabaseClient: SupabaseClient = SupabaseProvider.createClient(),
) {
    val authRepository: AuthRepository = SupabaseAuthRepository(supabaseClient)
    val walletRepository: WalletRepository = SupabaseWalletRepository(supabaseClient)
    val categoryRepository: CategoryRepository = SupabaseCategoryRepository(supabaseClient)
    val transactionRepository: TransactionRepository = SupabaseTransactionRepository(supabaseClient)
    val photoRepository: PhotoRepository = SupabasePhotoRepository(supabaseClient)
    val budgetRepository: BudgetRepository = SupabaseBudgetRepository(supabaseClient)
    
    val localPreferenceRepository = vn.vietbevis.apkbasic.data.preference.LocalPreferenceRepository(context)
    val userPreferenceRepository: UserPreferenceRepository = vn.vietbevis.apkbasic.data.preference.SyncingUserPreferenceRepository(
        localRepository = localPreferenceRepository,
        remoteRepository = SupabaseUserPreferenceRepository(supabaseClient)
    )
    
    val budgetNotificationHelper = BudgetNotificationHelper(context)
    val budgetMonitor = BudgetMonitor(budgetRepository, budgetNotificationHelper)

    val onboardingBootstrapper = OnboardingBootstrapper(
        supabaseClient = supabaseClient,
        walletRepository = walletRepository,
        categoryRepository = categoryRepository,
    )
}

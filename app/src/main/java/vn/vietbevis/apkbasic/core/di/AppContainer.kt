package vn.vietbevis.apkbasic.core.di

import io.github.jan.supabase.SupabaseClient
import vn.vietbevis.apkbasic.core.supabase.SupabaseProvider
import vn.vietbevis.apkbasic.data.auth.SupabaseAuthRepository
import vn.vietbevis.apkbasic.data.bootstrap.OnboardingBootstrapper
import vn.vietbevis.apkbasic.data.budget.SupabaseBudgetRepository
import vn.vietbevis.apkbasic.data.category.SupabaseCategoryRepository
import vn.vietbevis.apkbasic.data.investment.SupabaseInvestmentRepository
import vn.vietbevis.apkbasic.data.loan.SupabaseLoanRepository
import vn.vietbevis.apkbasic.data.photo.SupabasePhotoRepository
import vn.vietbevis.apkbasic.data.preference.SupabaseUserPreferenceRepository
import vn.vietbevis.apkbasic.data.recurring.SupabaseRecurringTransactionRepository
import vn.vietbevis.apkbasic.data.sharing.SupabaseSharingRepository
import vn.vietbevis.apkbasic.data.transaction.SupabaseTransactionRepository
import vn.vietbevis.apkbasic.data.transfer.SupabaseTransferRepository
import vn.vietbevis.apkbasic.data.wallet.SupabaseWalletRepository
import vn.vietbevis.apkbasic.domain.repository.AuthRepository
import vn.vietbevis.apkbasic.domain.repository.BudgetRepository
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository
import vn.vietbevis.apkbasic.domain.repository.InvestmentRepository
import vn.vietbevis.apkbasic.domain.repository.LoanRepository
import vn.vietbevis.apkbasic.domain.repository.PhotoRepository
import vn.vietbevis.apkbasic.domain.repository.RecurringTransactionRepository
import vn.vietbevis.apkbasic.domain.repository.SharingRepository
import vn.vietbevis.apkbasic.domain.repository.TransactionRepository
import vn.vietbevis.apkbasic.domain.repository.TransferRepository
import vn.vietbevis.apkbasic.domain.repository.UserPreferenceRepository
import vn.vietbevis.apkbasic.domain.repository.WalletRepository

class AppContainer(
    val supabaseClient: SupabaseClient = SupabaseProvider.createClient(),
) {
    val authRepository: AuthRepository = SupabaseAuthRepository(supabaseClient)
    val walletRepository: WalletRepository = SupabaseWalletRepository(supabaseClient)
    val categoryRepository: CategoryRepository = SupabaseCategoryRepository(supabaseClient)
    val transactionRepository: TransactionRepository = SupabaseTransactionRepository(supabaseClient)
    val photoRepository: PhotoRepository = SupabasePhotoRepository(supabaseClient)
    val budgetRepository: BudgetRepository = SupabaseBudgetRepository(supabaseClient)
    val transferRepository: TransferRepository = SupabaseTransferRepository(supabaseClient)
    val loanRepository: LoanRepository = SupabaseLoanRepository(supabaseClient)
    val investmentRepository: InvestmentRepository = SupabaseInvestmentRepository(supabaseClient)
    val recurringTransactionRepository: RecurringTransactionRepository = SupabaseRecurringTransactionRepository(supabaseClient)
    val userPreferenceRepository: UserPreferenceRepository = SupabaseUserPreferenceRepository(supabaseClient)
    val sharingRepository: SharingRepository = SupabaseSharingRepository(supabaseClient)
    val onboardingBootstrapper = OnboardingBootstrapper(
        supabaseClient = supabaseClient,
        walletRepository = walletRepository,
        categoryRepository = categoryRepository,
    )
}

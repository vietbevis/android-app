package vn.vietbevis.apkbasic.data.loan

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Loan
import vn.vietbevis.apkbasic.domain.repository.LoanRepository

class SupabaseLoanRepository(
    private val client: SupabaseClient,
) : LoanRepository {
    override suspend fun listLoans(includeArchived: Boolean): Result<List<Loan>> = appResult {
        client.from("loans")
            .select {
                if (!includeArchived) {
                    filter { neq("status", "archived") }
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<LoanDto>()
            .map { it.toDomain() }
    }

    override suspend fun createLoan(loan: Loan): Result<Loan> = appResult {
        client.from("loans").insert(loan.toDto())
        readLoan(loan.id)
    }

    override suspend fun updateLoan(loan: Loan): Result<Loan> = appResult {
        client.from("loans").update(loan.toDto()) {
            filter { eq("id", loan.id) }
        }
        readLoan(loan.id)
    }
    override suspend fun archiveLoan(loanId: String): Result<Unit> = appResult {
        client.from("loans").update({ set("status", "archived") }) {
            filter { eq("id", loanId) }
        }
        Unit
    }

    private suspend fun readLoan(loanId: String): Loan =
        client.from("loans")
            .select {
                filter { eq("id", loanId) }
            }
            .decodeSingle<LoanDto>()
            .toDomain()
}

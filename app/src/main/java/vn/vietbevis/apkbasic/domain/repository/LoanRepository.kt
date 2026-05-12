package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Loan

interface LoanRepository {
    suspend fun listLoans(includeArchived: Boolean = false): Result<List<Loan>>
    suspend fun createLoan(loan: Loan): Result<Loan>
    suspend fun updateLoan(loan: Loan): Result<Loan>
    suspend fun archiveLoan(loanId: String): Result<Unit>
}

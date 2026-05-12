package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Investment

interface InvestmentRepository {
    suspend fun listInvestments(includeArchived: Boolean = false): Result<List<Investment>>
    suspend fun createInvestment(investment: Investment): Result<Investment>
    suspend fun updateInvestment(investment: Investment): Result<Investment>
    suspend fun archiveInvestment(investmentId: String): Result<Unit>
}

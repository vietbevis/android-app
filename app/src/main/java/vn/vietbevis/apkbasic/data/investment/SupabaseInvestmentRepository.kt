package vn.vietbevis.apkbasic.data.investment

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Investment
import vn.vietbevis.apkbasic.domain.repository.InvestmentRepository

class SupabaseInvestmentRepository(
    private val client: SupabaseClient,
) : InvestmentRepository {
    override suspend fun listInvestments(includeArchived: Boolean): Result<List<Investment>> = appResult {
        client.from("investments")
            .select {
                if (!includeArchived) {
                    filter { neq("status", "archived") }
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<InvestmentDto>()
            .map { it.toDomain() }
    }

    override suspend fun createInvestment(investment: Investment): Result<Investment> = appResult {
        client.from("investments").insert(investment.toDto())
        readInvestment(investment.id)
    }

    override suspend fun updateInvestment(investment: Investment): Result<Investment> = appResult {
        client.from("investments").update(investment.toDto()) {
            filter { eq("id", investment.id) }
        }
        readInvestment(investment.id)
    }
    override suspend fun archiveInvestment(investmentId: String): Result<Unit> = appResult {
        client.from("investments").update({ set("status", "archived") }) {
            filter { eq("id", investmentId) }
        }
        Unit
    }

    private suspend fun readInvestment(investmentId: String): Investment =
        client.from("investments")
            .select {
                filter { eq("id", investmentId) }
            }
            .decodeSingle<InvestmentDto>()
            .toDomain()
}

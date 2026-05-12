package vn.vietbevis.apkbasic.data.category

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.repository.CategoryRepository

class SupabaseCategoryRepository(
    private val client: SupabaseClient,
) : CategoryRepository {
    override suspend fun listCategories(type: TransactionType?): Result<List<Category>> = appResult {
        client.from("categories")
            .select {
                filter {
                    eq("is_archived", false)
                    if (type != null) {
                        eq("transaction_type", type.toWireValue())
                    }
                }
                order("transaction_type", Order.ASCENDING)
                order("created_at", Order.ASCENDING)
            }
            .decodeList<CategoryDto>()
            .map { it.toDomain() }
    }

    override suspend fun createCategory(category: Category): Result<Category> = appResult {
        client.from("categories")
            .insert(category.toDto())
        readCategory(category.id)
    }

    override suspend fun updateCategory(category: Category): Result<Category> = appResult {
        client.from("categories")
            .update(category.toDto()) {
                filter { eq("id", category.id) }
            }
        readCategory(category.id)
    }

    override suspend fun archiveCategory(categoryId: String): Result<Unit> = appResult {
        client.from("categories")
            .update({
                set("is_archived", true)
            }) {
                filter { eq("id", categoryId) }
        }
        Unit
    }

    private suspend fun readCategory(categoryId: String): Category =
        client.from("categories")
            .select {
                filter { eq("id", categoryId) }
            }
            .decodeSingle<CategoryDto>()
            .toDomain()
}

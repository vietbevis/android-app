package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Category
import vn.vietbevis.apkbasic.domain.model.TransactionType

interface CategoryRepository {
    suspend fun listCategories(type: TransactionType? = null): Result<List<Category>>
    suspend fun createCategory(category: Category): Result<Category>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun archiveCategory(categoryId: String): Result<Unit>
}

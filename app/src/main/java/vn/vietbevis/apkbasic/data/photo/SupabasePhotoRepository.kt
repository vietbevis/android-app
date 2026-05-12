package vn.vietbevis.apkbasic.data.photo

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.repository.PhotoRepository
import java.io.File

class SupabasePhotoRepository(
    private val client: SupabaseClient,
) : PhotoRepository {
    override suspend fun uploadTransactionPhoto(
        userId: String,
        transactionId: String,
        localPath: String,
    ): Result<String> = appResult {
        val photoFile = File(localPath)
        require(photoFile.exists()) { "Photo file does not exist." }

        val storagePath = "$userId/$transactionId/${System.currentTimeMillis()}.jpg"
        client.storage["transaction-photos"].upload(
            path = storagePath,
            data = photoFile.readBytes(),
        ) {
            upsert = false
            contentType = ContentType.Image.JPEG
        }
        storagePath
    }

    override suspend fun deleteTransactionPhoto(storagePath: String): Result<Unit> = appResult {
        client.storage["transaction-photos"].delete(storagePath)
    }
}

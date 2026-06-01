package vn.vietbevis.apkbasic.domain.repository

interface PhotoRepository {
    suspend fun uploadTransactionPhoto(
        userId: String,
        transactionId: String,
        localPath: String,
    ): Result<String>

    suspend fun deleteTransactionPhoto(storagePath: String): Result<Unit>

    suspend fun uploadAvatar(
        userId: String,
        bytes: ByteArray,
    ): Result<String>
}

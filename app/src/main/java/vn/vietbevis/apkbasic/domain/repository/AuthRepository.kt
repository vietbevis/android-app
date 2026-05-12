package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.UserProfile

interface AuthRepository {
    suspend fun currentUser(): Result<UserProfile?>
    suspend fun signIn(email: String, password: String): Result<UserProfile>
    suspend fun signUp(email: String, password: String): Result<UserProfile>
    suspend fun signOut(): Result<Unit>
}

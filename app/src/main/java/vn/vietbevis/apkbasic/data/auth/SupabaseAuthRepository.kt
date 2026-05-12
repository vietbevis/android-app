package vn.vietbevis.apkbasic.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import vn.vietbevis.apkbasic.core.common.AppError
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.data.profile.ProfileDto
import vn.vietbevis.apkbasic.data.profile.toDomain
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.repository.AuthRepository

class SupabaseAuthRepository(
    private val client: SupabaseClient,
) : AuthRepository {
    override suspend fun currentUser(): Result<UserProfile?> = appResult {
        client.auth.awaitInitialization()
        val userId = client.auth.currentUserOrNull()?.id ?: return@appResult null
        readProfile(userId) ?: UserProfile(id = userId, displayName = null)
    }

    override suspend fun signIn(email: String, password: String): Result<UserProfile> = appResult {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val userId = client.auth.currentUserOrNull()?.id ?: throw AppError.MissingSession()
        readProfile(userId) ?: UserProfile(id = userId, displayName = null)
    }

    override suspend fun signUp(email: String, password: String): Result<UserProfile> = appResult {
        val user = client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val userId = user?.id ?: client.auth.currentUserOrNull()?.id ?: throw AppError.MissingSession()
        UserProfile(id = userId, displayName = null)
    }

    override suspend fun signOut(): Result<Unit> = appResult {
        client.auth.signOut()
    }

    private suspend fun readProfile(userId: String): UserProfile? =
        client.from("profiles")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<ProfileDto>()
            ?.toDomain()
}

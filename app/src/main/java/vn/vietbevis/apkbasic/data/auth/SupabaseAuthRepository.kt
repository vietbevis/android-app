package vn.vietbevis.apkbasic.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import vn.vietbevis.apkbasic.core.common.AppError
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.data.profile.ProfileDto
import vn.vietbevis.apkbasic.data.profile.toDomain
import vn.vietbevis.apkbasic.data.profile.toDto
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.repository.AuthRepository

class SupabaseAuthRepository(
    private val client: SupabaseClient,
) : AuthRepository {
    override suspend fun currentUser(): Result<UserProfile?> = appResult {
        client.auth.awaitInitialization()
        val user = client.auth.currentUserOrNull() ?: return@appResult null
        val profile = readProfile(user.id)
        profile?.copy(email = user.email) ?: UserProfile(id = user.id, email = user.email, displayName = null)
    }

    override suspend fun signIn(email: String, password: String): Result<UserProfile> = appResult {
        val result = client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val user = client.auth.currentUserOrNull() ?: throw AppError.MissingSession()
        val profile = readProfile(user.id)
        profile?.copy(email = user.email) ?: UserProfile(id = user.id, email = user.email, displayName = null)
    }

    override suspend fun signUp(email: String, password: String): Result<UserProfile> = appResult {
        val user = client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val userId = user?.id ?: client.auth.currentUserOrNull()?.id ?: throw AppError.MissingSession()
        val emailValue = user?.email ?: client.auth.currentUserOrNull()?.email
        UserProfile(id = userId, email = emailValue, displayName = null)
    }

    override suspend fun signOut(): Result<Unit> = appResult {
        client.auth.signOut()
    }

    override suspend fun updateProfile(profile: UserProfile): Result<UserProfile> = appResult {
        val dto = profile.toDto()
        client.from("profiles").upsert(dto)
        profile
    }

    private suspend fun readProfile(userId: String): UserProfile? =
        client.from("profiles")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<ProfileDto>()
            ?.toDomain()
}

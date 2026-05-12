package vn.vietbevis.apkbasic.data.preference

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.UserPreference
import vn.vietbevis.apkbasic.domain.repository.UserPreferenceRepository

class SupabaseUserPreferenceRepository(
    private val client: SupabaseClient,
) : UserPreferenceRepository {
    override suspend fun readPreferences(): Result<UserPreference> = appResult {
        client.from("user_preferences")
            .select()
            .decodeList<UserPreferenceDto>()
            .firstOrNull()
            ?.toDomain()
            ?: error("Preferences have not been created.")
    }

    override suspend fun updatePreferences(preference: UserPreference): Result<UserPreference> = appResult {
        client.from("user_preferences").upsert(preference.toDto())
        client.from("user_preferences")
            .select { filter { eq("user_id", preference.userId) } }
            .decodeSingle<UserPreferenceDto>()
            .toDomain()
    }
}

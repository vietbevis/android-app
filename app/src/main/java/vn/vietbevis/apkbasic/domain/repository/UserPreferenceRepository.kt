package vn.vietbevis.apkbasic.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.vietbevis.apkbasic.domain.model.UserPreference

interface UserPreferenceRepository {
    val preferencesFlow: Flow<UserPreference?>
    suspend fun readPreferences(): Result<UserPreference>
    suspend fun updatePreferences(preference: UserPreference): Result<UserPreference>
}

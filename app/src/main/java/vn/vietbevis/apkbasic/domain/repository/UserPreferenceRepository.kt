package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.UserPreference

interface UserPreferenceRepository {
    suspend fun readPreferences(): Result<UserPreference>
    suspend fun updatePreferences(preference: UserPreference): Result<UserPreference>
}

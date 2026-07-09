package vn.vietbevis.apkbasic.data.preference

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vn.vietbevis.apkbasic.domain.model.UserPreference

val Context.dataStore by preferencesDataStore(name = "settings")

class LocalPreferenceRepository(private val context: Context) {
    private val PREF_KEY = stringPreferencesKey("user_preference")

    val preferencesFlow: Flow<UserPreference?> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_KEY]?.let { jsonString ->
                try {
                    Json.decodeFromString<UserPreferenceDto>(jsonString).toDomain()
                } catch (e: Exception) {
                    null
                }
            }
        }

    suspend fun savePreferences(preference: UserPreference) {
        context.dataStore.edit { preferences ->
            preferences[PREF_KEY] = Json.encodeToString(preference.toDto())
        }
    }
    
    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(PREF_KEY)
        }
    }
}

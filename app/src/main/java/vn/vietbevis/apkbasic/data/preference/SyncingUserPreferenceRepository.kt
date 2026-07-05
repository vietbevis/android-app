package vn.vietbevis.apkbasic.data.preference

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import vn.vietbevis.apkbasic.domain.model.UserPreference
import vn.vietbevis.apkbasic.domain.repository.UserPreferenceRepository

class SyncingUserPreferenceRepository(
    private val localRepository: LocalPreferenceRepository,
    private val remoteRepository: UserPreferenceRepository,
) : UserPreferenceRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val preferencesFlow: Flow<UserPreference?> = localRepository.preferencesFlow

    override suspend fun readPreferences(): Result<UserPreference> {
        return remoteRepository.readPreferences().onSuccess { preference ->
            scope.launch {
                localRepository.savePreferences(preference)
            }
        }
    }

    override suspend fun updatePreferences(preference: UserPreference): Result<UserPreference> {
        // Save locally first for immediate UI update
        localRepository.savePreferences(preference)
        
        // Then sync to remote
        return remoteRepository.updatePreferences(preference).onFailure {
            // Depending on requirements, we might want to rollback or queue the update.
            // For now, if remote fails, we keep local so it feels offline-first, but we might log it.
        }
    }
}

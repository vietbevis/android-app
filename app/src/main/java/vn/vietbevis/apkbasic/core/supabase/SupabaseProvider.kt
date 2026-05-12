package vn.vietbevis.apkbasic.core.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    fun createClient(config: SupabaseConfig = SupabaseConfig.fromBuildConfig()): SupabaseClient {
        require(config.isConfigured) {
            "Missing SUPABASE_URL or SUPABASE_PUBLISHABLE_KEY in Gradle properties, environment, or local.properties."
        }

        return createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.publishableKey,
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}

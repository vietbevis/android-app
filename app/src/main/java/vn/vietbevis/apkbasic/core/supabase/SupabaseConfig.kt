package vn.vietbevis.apkbasic.core.supabase

import vn.vietbevis.apkbasic.BuildConfig

data class SupabaseConfig(
    val url: String,
    val publishableKey: String,
) {
    val isConfigured: Boolean = url.isNotBlank() && publishableKey.isNotBlank()

    companion object {
        fun fromBuildConfig(): SupabaseConfig = SupabaseConfig(
            url = BuildConfig.SUPABASE_URL,
            publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        )
    }
}

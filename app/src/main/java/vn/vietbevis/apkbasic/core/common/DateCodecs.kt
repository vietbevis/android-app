package vn.vietbevis.apkbasic.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateCodecs {
    private val isoFormatter: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    private val isoFormatterNoMillis: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    fun epochMillisToIso(epochMillis: Long): String = isoFormatter.format(Date(epochMillis))

    fun isoToEpochMillis(value: String): Long {
        if (value.isBlank()) return 0L
        return runCatching {
            // Normalize Supabase format: "2024-05-20T14:00:00.123456+00:00" -> "2024-05-20T14:00:00.123Z"
            val normalized = value
                .replace(Regex("(\\.\\d{3})\\d+"), "$1") // Truncate micros to millis
                .replace(Regex("\\+00:00$"), "Z")
                .replace(Regex("Z$"), ".000Z") // Ensure dots exist for easy regex
                .replace(Regex("\\.\\d{3}\\.000Z$"), ".000Z") // Clean up if we added too many
            
            isoFormatter.parse(normalized)?.time ?: 0L
        }.getOrElse {
            // Last resort: try parsing without milliseconds
            runCatching {
                val noMillis = value.split(".")[0].replace("Z", "")
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(noMillis)?.time ?: 0L
            }.getOrDefault(0L)
        }
    }
}

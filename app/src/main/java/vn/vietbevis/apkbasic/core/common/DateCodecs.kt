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
            // PostgreSQL/Supabase may return various ISO formats:
            // 1. 2024-05-20T14:00:00.123456+00:00
            // 2. 2024-05-20 14:00:00+00
            // 3. 2024-05-20T14:00:00Z
            var normalized = value
                .replace(" ", "T") // Replace space with T
                .replace(Regex("\\+00(:00)?$"), "Z") // Normalize timezone to Z
            
            if (normalized.contains(".")) {
                val parts = normalized.split(".")
                val base = parts[0]
                var fraction = parts[1]
                // Separate Z if present
                val hasZ = fraction.endsWith("Z")
                if (hasZ) fraction = fraction.removeSuffix("Z")
                
                // Truncate or pad fraction to exactly 3 digits (milliseconds)
                fraction = if (fraction.length >= 3) fraction.substring(0, 3) else fraction.padEnd(3, '0')
                normalized = "$base.${fraction}Z"
            } else if (normalized.endsWith("Z")) {
                // Ensure milliseconds exist: 2024-05-20T14:00:00Z -> 2024-05-20T14:00:00.000Z
                normalized = normalized.replace("Z", ".000Z")
            } else if (normalized.length == 10) {
                // Just a date: 2024-05-20 -> 2024-05-20T00:00:00.000Z
                normalized = "${normalized}T00:00:00.000Z"
            } else if (!normalized.contains(".") && !normalized.endsWith("Z")) {
                // No millis and no Z: 2024-05-20T14:00:00 -> 2024-05-20T14:00:00.000Z
                normalized = "${normalized}.000Z"
            }
            
            isoFormatter.parse(normalized)?.time ?: 0L
        }.getOrElse {
            // Fallback for non-ISO or unexpected formats
            runCatching {
                // Try parsing the first 19 chars (yyyy-MM-ddTHH:mm:ss)
                val simplePart = value.replace(" ", "T").take(19)
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(simplePart)?.time ?: 0L
            }.getOrDefault(0L)
        }
    }
}

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
            // Supabase/PostgreSQL often returns: 2024-05-20T14:00:00.123456+00
            // We need to normalize it to yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
            var normalized = value
                .replace(Regex("\\+00(:00)?$"), "Z") // Normalize timezone offset to Z
            
            if (normalized.contains(".")) {
                val parts = normalized.split(".")
                val base = parts[0]
                var fraction = parts[1].replace("Z", "")
                // Truncate or pad to exactly 3 digits for milliseconds
                fraction = if (fraction.length >= 3) fraction.substring(0, 3) else fraction.padEnd(3, '0')
                normalized = "$base.${fraction}Z"
            } else if (normalized.endsWith("Z")) {
                normalized = normalized.replace("Z", ".000Z")
            } else if (!normalized.contains("T")) {
                // If it's just a date, assume start of day UTC
                normalized = "${normalized}T00:00:00.000Z"
            }

            isoFormatter.parse(normalized)?.time ?: 0L
        }.getOrElse {
            // Fallback for other formats if any
            runCatching {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(value.substringBefore("."))?.time ?: 0L
            }.getOrDefault(0L)
        }
    }
}

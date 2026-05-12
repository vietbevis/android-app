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

    fun epochMillisToIso(epochMillis: Long): String = isoFormatter.format(Date(epochMillis))

    fun isoToEpochMillis(value: String): Long = runCatching {
        isoFormatter.parse(value)?.time ?: 0L
    }.getOrDefault(0L)
}

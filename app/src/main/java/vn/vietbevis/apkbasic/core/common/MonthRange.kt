package vn.vietbevis.apkbasic.core.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthRange(
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val label: String,
)

object MonthRanges {
    fun currentMonth(): MonthRange {
        val start = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = start.clone() as Calendar
        end.add(Calendar.MONTH, 1)
        val label = SimpleDateFormat("MM/yyyy", Locale.forLanguageTag("vi-VN")).format(start.time)
        return MonthRange(start.timeInMillis, end.timeInMillis, label)
    }
}

package vn.vietbevis.apkbasic.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import vn.vietbevis.apkbasic.domain.model.Money

class BudgetNotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "BudgetNotification"
        private const val CHANNEL_ID = "budget_alerts"
        private const val CHANNEL_NAME = "Cảnh báo ngân sách"
        private const val CHANNEL_DESCRIPTION = "Thông báo khi bạn chi tiêu vượt định mức"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetExceededNotification(budgetName: String, limit: Money, spent: Money) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Log.w(TAG, "Missing POST_NOTIFICATIONS permission")
                return
            }
        }

        Log.d(TAG, "Showing notification for budget: $budgetName")
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Vượt định mức chi tiêu!")
            .setContentText("Bạn đã chi ${spent.formatVnd()} cho '$budgetName', vượt ngưỡng ${limit.formatVnd()}.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}

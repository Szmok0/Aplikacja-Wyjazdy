package com.rodzina.wyjazdy.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rodzina.wyjazdy.R

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val city = inputData.getString(KEY_CITY) ?: return Result.success()
        val hours = inputData.getInt(KEY_HOURS, 24)
        val tripId = inputData.getString(KEY_TRIP_ID) ?: ""
        showNotification(city, hours, tripId)
        return Result.success()
    }

    private fun showNotification(city: String, hours: Int, tripId: String) {
        val context = applicationContext
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val whenText = if (hours >= 24) "za ${hours / 24} dzień/dni" else "za $hours h"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Wyjazd do $city $whenText")
            .setContentText("Sprawdź szczegóły w aplikacji Wyjazdy")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        androidx.core.app.NotificationManagerCompat.from(context)
            .notify(tripId.hashCode() + hours, notification)
    }

    companion object {
        const val CHANNEL_ID = "trip_reminders"
        const val KEY_TRIP_ID = "tripId"
        const val KEY_CITY = "city"
        const val KEY_HOURS = "hours"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
                manager?.createNotificationChannel(channel)
            }
        }
    }
}

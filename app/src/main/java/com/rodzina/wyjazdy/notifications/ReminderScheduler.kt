package com.rodzina.wyjazdy.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.rodzina.wyjazdy.data.model.Trip
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Przelicza od zera lokalne przypomnienia (WorkManager) dla własnych, przyszłych wyjazdów -
 * wywoływane przy każdej zmianie listy wyjazdów lub ustawień powiadomień (patrz WyjazdyApp).
 * Koszt ponownego planowania jest znikomy przy skali rodzinnej apki, więc nie ma potrzeby diffowania.
 */
object ReminderScheduler {
    private const val TAG = "trip_reminder"

    fun rescheduleAll(context: Context, ownUpcomingTrips: List<Trip>, reminderHoursBefore: List<Int>) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(TAG)
        if (reminderHoursBefore.isEmpty()) return

        val now = Instant.now()
        ownUpcomingTrips.forEach { trip ->
            val startInstant = trip.dateStart.toDate().toInstant()
            reminderHoursBefore.forEach { hours ->
                val delayMillis = Duration.between(now, startInstant.minus(Duration.ofHours(hours.toLong()))).toMillis()
                if (delayMillis > 0) {
                    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                        .setInputData(
                            workDataOf(
                                ReminderWorker.KEY_TRIP_ID to trip.id,
                                ReminderWorker.KEY_CITY to trip.city,
                                ReminderWorker.KEY_HOURS to hours,
                            ),
                        )
                        .addTag(TAG)
                        .build()
                    workManager.enqueueUniqueWork("trip_${trip.id}_$hours", ExistingWorkPolicy.REPLACE, request)
                }
            }
        }
    }
}

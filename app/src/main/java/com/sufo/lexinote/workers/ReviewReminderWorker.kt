package com.sufo.lexinote.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sufo.lexinote.MainActivity
import com.sufo.lexinote.R
import com.sufo.lexinote.data.preferences.SettingsDataStore
import com.sufo.lexinote.data.repo.ReviewRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Date

@HiltWorker
class ReviewReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reviewRepository: ReviewRepository,
    private val settingsDataStore: SettingsDataStore // Inject SettingsDataStore
): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // First, check if notifications are enabled by the user
            val userPreferences = settingsDataStore.userPreferencesFlow.first()
            if (!userPreferences.notificationsEnabled) {
                println("Notifications are disabled by the user. Skipping reminder.")
                return Result.success()
            }

            // If enabled, check for due words
            val dueWordsCount = reviewRepository.getDueWordsCount()

            // If there are words to review, show a notification.
            if (dueWordsCount > 0) {
                showNotification(dueWordsCount)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(dueWordsCount: Int) {
        val channelId = "review_reminder_channel"
        val notificationId = 1

        // Create an explicit intent for an Activity in your app
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a proper notification icon
            .setContentTitle("LexiNote "+appContext.resources.getString(R.string.review_reminder))
            .setContentText(appContext.resources.getString(R.string.remind_tip,dueWordsCount))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Notification disappears on tap

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = appContext.resources.getString(R.string.review_reminder)
            val descriptionText = appContext.resources.getString(R.string.review_reminder_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(appContext)) {
            if (ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
            }
        }
    }
}

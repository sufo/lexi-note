package com.sufo.lexinote

import android.app.Application
import android.util.Log
import com.sufo.lexinote.data.local.db.DictDatabaseHelper
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sufo.lexinote.workers.ReviewReminderWorker
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class LexiNoteApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize Timber for logging
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }else{
            Timber.plant(ReleaseTree())
        }

        // Schedule the periodic review reminder work
        scheduleReviewReminder()
    }

    private fun scheduleReviewReminder() {
        val workManager = WorkManager.getInstance(this)

        // Create a periodic request to run every 1 hours
        val reviewRequest = PeriodicWorkRequestBuilder<ReviewReminderWorker>(12, TimeUnit.HOURS)
            // Optional: Add constraints, e.g., require network or charging
            // .setConstraints(Constraints.Builder()...build())
            .build()

        // Enqueue the unique periodic work, keeping the existing one if it's already scheduled
        workManager.enqueueUniquePeriodicWork(
            "reviewReminderWork", // A unique name for the work
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
            reviewRequest
        )
    }

    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            // log your crash to your favourite
            // Sending crash report to Firebase CrashAnalytics

            // FirebaseCrash.report(message);
            // FirebaseCrash.report(new Exception(message));
        }
    }
}
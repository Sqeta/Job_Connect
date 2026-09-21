package com.example.job_connect.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.job_connect.R
import com.example.job_connect.SearchActivity
import com.example.job_connect.data.local.JobConnectDatabase
import com.google.firebase.auth.FirebaseAuth

/**
 * WorkManager task that creates JobConnect job-alert notifications.
 */
class JobAlertWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : Worker(
    appContext,
    workerParameters
) {

    companion object {
        private const val TAG = "JobAlertWorker"
        private const val CHANNEL_ID =
            "job_connect_alerts"

        private const val CHANNEL_NAME =
            "Job alerts"

        private const val NOTIFICATION_ID = 1001
    }

    override fun doWork(): Result {
        return try {
            val currentUser =
                FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Log.d(
                    TAG,
                    "No signed-in user. Notification skipped."
                )

                return Result.success()
            }

            val savedJobCount =
                JobConnectDatabase
                    .getDatabase(applicationContext)
                    .cachedJobDao()
                    .getSavedJobCount(currentUser.uid)

            createNotificationChannel()

            showJobAlertNotification(savedJobCount)

            Log.d(
                TAG,
                "Background job alert completed successfully"
            )

            Result.success()
        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Background job alert failed",
                exception
            )

            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Notifications about job opportunities"
                }

            val notificationManager =
                applicationContext.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }

    private fun showJobAlertNotification(
        savedJobCount: Int
    ) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            applicationContext.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(
                TAG,
                "Notification permission has not been granted"
            )

            return
        }

        val searchIntent =
            Intent(
                applicationContext,
                SearchActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                searchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val message =
            if (savedJobCount > 0) {
                "You have $savedJobCount saved jobs. Check for new opportunities today."
            } else {
                "New opportunities may be available. Start searching for your next role."
            }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "JobConnect opportunities"
                )
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                NOTIFICATION_ID,
                notification
            )
    }
}
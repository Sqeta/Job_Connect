package com.example.job_connect

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class JobConnectMessagingService :
    FirebaseMessagingService() {

    companion object {
        private const val TAG =
            "JobConnectMessaging"

        private const val CHANNEL_ID =
            "job_connect_push_notifications"

        private const val CHANNEL_NAME =
            "JobConnect Push Notifications"

        private const val CHANNEL_DESCRIPTION =
            "Real-time job alerts and JobConnect updates"
    }

    override fun onMessageReceived(
        remoteMessage: RemoteMessage
    ) {
        super.onMessageReceived(remoteMessage)

        Log.d(
            TAG,
            "Push notification received from: ${remoteMessage.from}"
        )

        val title =
            remoteMessage.notification?.title
                ?: remoteMessage.data["title"]
                ?: "JobConnect"

        val message =
            remoteMessage.notification?.body
                ?: remoteMessage.data["message"]
                ?: remoteMessage.data["body"]
                ?: "A new JobConnect update is available."

        showNotification(
            title = title,
            message = message
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(
            TAG,
            "New Firebase messaging token: $token"
        )

        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(
        token: String
    ) {
        val userId =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid

        if (userId == null) {
            Log.d(
                TAG,
                "User is not signed in. Token was not saved."
            )
            return
        }

        val tokenData =
            hashMapOf<String, Any>(
                "fcmToken" to token,
                "tokenUpdatedAt" to
                        com.google.firebase.firestore
                            .FieldValue
                            .serverTimestamp()
            )

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(userId)
            .set(
                tokenData,
                com.google.firebase.firestore
                    .SetOptions
                    .merge()
            )
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "Firebase messaging token saved"
                )
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Could not save Firebase messaging token",
                    exception
                )
            }
    }

    private fun showNotification(
        title: String,
        message: String
    ) {
        createNotificationChannel()

        val openAppIntent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                this,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(
                TAG,
                "Notification permission has not been granted"
            )
            return
        }

        NotificationManagerCompat
            .from(this)
            .notify(
                System.currentTimeMillis()
                    .toInt(),
                notification
            )
    }

    private fun createNotificationChannel() {
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description =
                        CHANNEL_DESCRIPTION
                }

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            notificationManager
                .createNotificationChannel(
                    channel
                )
        }
    }
}
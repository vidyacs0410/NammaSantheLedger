package com.namma.santhe.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.namma.santhe.MainActivity
import com.namma.santhe.R

class NotificationHelper(private val context: Context) {
    fun getContext() = context

    companion object {
        private const val CHANNEL_ID = "ledger_reminders"
        private const val CHANNEL_NAME = "Ledger Reminders"
        private const val AUTH_CHANNEL_ID = "auth_notifications"
        private const val AUTH_CHANNEL_NAME = "Authentication"
        private const val NOTIFICATION_ID = 101
        private const val OTP_NOTIFICATION_ID = 102
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Reminder Channel
            val reminderChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Reminders for pending customer dues"
            }
            notificationManager.createNotificationChannel(reminderChannel)

            // Auth Channel
            val authChannel = NotificationChannel(AUTH_CHANNEL_ID, AUTH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "One-time passwords and account security"
            }
            notificationManager.createNotificationChannel(authChannel)
        }
    }

    fun showReminder(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun showOtpNotification(otp: String) {
        val builder = NotificationCompat.Builder(context, AUTH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Namma Santhe OTP")
            .setContentText("Your verification code is: $otp. Valid for 10 minutes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(OTP_NOTIFICATION_ID, builder.build())
    }
}

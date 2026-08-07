package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.model.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object RenewalNotificationService {
    const val CHANNEL_ID = "subscription_renewal_channel"
    private const val CHANNEL_NAME = "Subscription Renewal Alerts"
    private const val CHANNEL_DESC = "Alerts triggered 3 days before subscription renewal dates"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun checkAndTriggerRenewalAlerts(
        context: Context,
        forceTrigger: Boolean = false
    ): List<NotificationEntity> = withContext(Dispatchers.IO) {
        createNotificationChannel(context)

        val database = AppDatabase.getDatabase(context)
        val userProfile = database.userProfileDao().getUserProfileOnce()

        // Honor user's notification preference unless forced for manual testing
        if (!forceTrigger && userProfile?.notificationsEnabled == false) {
            return@withContext emptyList()
        }

        val defaultLeadDays = userProfile?.defaultReminderDays ?: 3
        val todayEpochDay = LocalDate.now().toEpochDay()
        val activeSubscriptions = database.subscriptionDao().getActiveSubscriptionsOnce()
        val generatedAlerts = mutableListOf<NotificationEntity>()
        val startOfDayMs = System.currentTimeMillis() - 86_400_000L // last 24 hours

        for (sub in activeSubscriptions) {
            val reminderLead = if (sub.reminderDaysBefore > 0) sub.reminderDaysBefore else defaultLeadDays
            val daysUntilRenewal = (sub.nextRenewalEpochDays - todayEpochDay).toInt()

            // Check if renewal falls within the lead time window (e.g. 3 days before or today)
            if (daysUntilRenewal in 0..reminderLead) {
                val existingCount = database.notificationDao().getCountForSubscriptionSince(sub.id, startOfDayMs)

                if (existingCount == 0 || forceTrigger) {
                    val daysText = when (daysUntilRenewal) {
                        0 -> "today"
                        1 -> "tomorrow"
                        else -> "in $daysUntilRenewal days"
                    }

                    val title = "Renewal Alert: ${sub.name}"
                    val message = "${sub.name} auto-renews $daysText for ${sub.currency} ${String.format("%.2f", sub.price)}. Review or update your plan."

                    // Trigger System Local Status Bar Alert
                    showSystemNotification(
                        context = context,
                        notificationId = sub.id,
                        title = title,
                        message = message
                    )

                    // Store Notification Record in Room DB for in-app Alert Feed
                    val notificationEntity = NotificationEntity(
                        subscriptionId = sub.id,
                        subscriptionName = sub.name,
                        title = title,
                        message = message,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        type = "RENEWAL_ALERT"
                    )
                    database.notificationDao().insertNotification(notificationEntity)
                    generatedAlerts.add(notificationEntity)
                }
            }
        }

        generatedAlerts
    }

    fun showSystemNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleDailyRenewalCheck(context: Context) {
        try {
            val intent = Intent(context, RenewalAlarmReceiver::class.java).apply {
                action = RenewalAlarmReceiver.ACTION_CHECK_RENEWALS
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAtMs = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.example.data.repository

import com.example.data.local.NotificationDao
import com.example.data.local.SubscriptionDao
import com.example.data.local.UserProfileDao
import com.example.data.model.NotificationEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val notificationDao: NotificationDao,
    private val userProfileDao: UserProfileDao
) {
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = subscriptionDao.getCount()
        val todayEpochDay = LocalDate.now().toEpochDay()

        if (count == 0) {
            val sampleSubs = listOf(
                SubscriptionEntity(
                    name = "Netflix Premium",
                    price = 19.99,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "STREAMING",
                    nextRenewalEpochDays = todayEpochDay + 3,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Visa",
                    reminderDaysBefore = 3,
                    notes = "4K UHD Family plan",
                    colorHex = "#E50914"
                ),
                SubscriptionEntity(
                    name = "ChatGPT Plus",
                    price = 20.00,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "PRODUCTIVITY",
                    nextRenewalEpochDays = todayEpochDay + 1,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Mastercard",
                    reminderDaysBefore = 1,
                    notes = "GPT-4o & Advanced Voice Access",
                    colorHex = "#10A37F"
                ),
                SubscriptionEntity(
                    name = "Spotify Family",
                    price = 16.99,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "STREAMING",
                    nextRenewalEpochDays = todayEpochDay + 12,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "PayPal",
                    reminderDaysBefore = 3,
                    notes = "Ad-free music stream for 6 members",
                    colorHex = "#1DB954"
                ),
                SubscriptionEntity(
                    name = "AWS Cloud Hosting",
                    price = 45.50,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "CLOUD",
                    nextRenewalEpochDays = todayEpochDay + 18,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Visa",
                    reminderDaysBefore = 5,
                    notes = "EC2 & S3 bucket usage",
                    colorHex = "#FF9900"
                ),
                SubscriptionEntity(
                    name = "iCloud+ 2TB",
                    price = 9.99,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "CLOUD",
                    nextRenewalEpochDays = todayEpochDay + 5,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Apple Pay",
                    reminderDaysBefore = 3,
                    notes = "Full phone backup & Private Relay",
                    colorHex = "#007AFF"
                ),
                SubscriptionEntity(
                    name = "Adobe Creative Cloud",
                    price = 54.99,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "PRODUCTIVITY",
                    nextRenewalEpochDays = todayEpochDay + 22,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Visa",
                    reminderDaysBefore = 7,
                    notes = "Photoshop, Illustrator, Premiere Pro",
                    colorHex = "#FF0000"
                ),
                SubscriptionEntity(
                    name = "PlayStation Plus",
                    price = 79.99,
                    currency = "USD",
                    billingCycle = "YEARLY",
                    category = "GAMING",
                    nextRenewalEpochDays = todayEpochDay + 45,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Mastercard",
                    reminderDaysBefore = 7,
                    notes = "Online multiplayer & monthly games",
                    colorHex = "#00439C"
                ),
                SubscriptionEntity(
                    name = "Gym Membership",
                    price = 29.99,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "FITNESS",
                    nextRenewalEpochDays = todayEpochDay + 8,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Visa",
                    reminderDaysBefore = 3,
                    notes = "24/7 access to downtown location",
                    colorHex = "#10B981"
                ),
                SubscriptionEntity(
                    name = "GitHub Copilot",
                    price = 10.00,
                    currency = "USD",
                    billingCycle = "MONTHLY",
                    category = "PRODUCTIVITY",
                    nextRenewalEpochDays = todayEpochDay + 2,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = "Google Pay",
                    reminderDaysBefore = 1,
                    notes = "AI Pair programmer for Android",
                    colorHex = "#6E40C9"
                )
            )

            sampleSubs.forEach { sub ->
                val subId = subscriptionDao.insertSubscription(sub).toInt()
                // Seed initial notification alert for upcoming renewals
                if (sub.nextRenewalEpochDays - todayEpochDay <= 3) {
                    val daysLeft = (sub.nextRenewalEpochDays - todayEpochDay).toInt()
                    val daysText = if (daysLeft == 0) "today" else if (daysLeft == 1) "tomorrow" else "in $daysLeft days"
                    notificationDao.insertNotification(
                        NotificationEntity(
                            subscriptionId = subId,
                            subscriptionName = sub.name,
                            title = "Upcoming Renewal Alert",
                            message = "${sub.name} auto-renews $daysText for $${sub.price} ${sub.currency}.",
                            timestamp = System.currentTimeMillis() - (daysLeft * 3600000)
                        )
                    )
                }
            }
        }

        val existingProfile = userProfileDao.getUserProfileOnce()
        if (existingProfile == null) {
            userProfileDao.insertOrUpdateUserProfile(
                UserProfileEntity(
                    id = 1,
                    isLoggedIn = true,
                    name = "Sazzad Hossain",
                    email = "sazzadmbstu@gmail.com",
                    preferredCurrency = "USD",
                    isDarkMode = true,
                    notificationsEnabled = true,
                    defaultReminderDays = 3,
                    authProvider = "GOOGLE"
                )
            )
        }
    }

    suspend fun addSubscription(sub: SubscriptionEntity) = withContext(Dispatchers.IO) {
        val id = subscriptionDao.insertSubscription(sub).toInt()
        val todayEpoch = LocalDate.now().toEpochDay()
        val daysUntil = (sub.nextRenewalEpochDays - todayEpoch).toInt()
        if (daysUntil in 0..7) {
            notificationDao.insertNotification(
                NotificationEntity(
                    subscriptionId = id,
                    subscriptionName = sub.name,
                    title = "New Subscription Added",
                    message = "Added ${sub.name} ($${sub.price} ${sub.currency}). Next renewal in $daysUntil days.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateSubscription(sub: SubscriptionEntity) = withContext(Dispatchers.IO) {
        subscriptionDao.updateSubscription(sub)
    }

    suspend fun deleteSubscription(sub: SubscriptionEntity) = withContext(Dispatchers.IO) {
        subscriptionDao.deleteSubscription(sub)
    }

    suspend fun togglePauseSubscription(sub: SubscriptionEntity) = withContext(Dispatchers.IO) {
        val newStatus = if (sub.status == "PAUSED") "ACTIVE" else "PAUSED"
        subscriptionDao.updateStatus(sub.id, newStatus)
        notificationDao.insertNotification(
            NotificationEntity(
                subscriptionId = sub.id,
                subscriptionName = sub.name,
                title = "Subscription $newStatus",
                message = "${sub.name} status updated to $newStatus.",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCurrency(currencyCode: String) = withContext(Dispatchers.IO) {
        userProfileDao.updateCurrency(currencyCode)
    }

    suspend fun updateDarkMode(isDarkMode: Boolean) = withContext(Dispatchers.IO) {
        userProfileDao.updateDarkMode(isDarkMode)
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        userProfileDao.insertOrUpdateUserProfile(profile)
    }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearAllNotifications() = withContext(Dispatchers.IO) {
        notificationDao.clearAll()
    }

    suspend fun setLoginState(isLoggedIn: Boolean) = withContext(Dispatchers.IO) {
        userProfileDao.updateLoginState(isLoggedIn)
    }
}

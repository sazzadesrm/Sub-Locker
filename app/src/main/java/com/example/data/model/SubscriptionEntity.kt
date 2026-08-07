package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val currency: String = "USD",
    val billingCycle: String = "MONTHLY",
    val category: String = "STREAMING",
    val nextRenewalEpochDays: Long, // Epoch day for next renewal
    val status: String = "ACTIVE",
    val isAutoRenew: Boolean = true,
    val paymentMethod: String = "Visa",
    val reminderDaysBefore: Int = 3,
    val notes: String = "",
    val iconName: String = "",
    val colorHex: String = "#3B82F6"
)

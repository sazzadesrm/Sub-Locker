package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subscriptionId: Int,
    val subscriptionName: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "RENEWAL_ALERT" // RENEWAL_ALERT, PRICE_CHANGE, STATUS_CHANGE
)

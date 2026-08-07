package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val isLoggedIn: Boolean = true,
    val name: String = "Sazzad Hossain",
    val email: String = "sazzadmbstu@gmail.com",
    val phone: String = "+1 (555) 234-5678",
    val preferredCurrency: String = "TK",
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val defaultReminderDays: Int = 3,
    val authProvider: String = "GOOGLE",
    val avatarUrl: String = ""
)

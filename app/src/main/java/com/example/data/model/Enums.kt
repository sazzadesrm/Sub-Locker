package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class BillingCycle(val label: String, val cyclesPerYear: Double) {
    WEEKLY("Weekly", 52.0),
    MONTHLY("Monthly", 12.0),
    QUARTERLY("Quarterly", 4.0),
    YEARLY("Yearly", 1.0);

    fun toMonthlyAmount(price: Double): Double {
        return when (this) {
            WEEKLY -> price * 4.333
            MONTHLY -> price
            QUARTERLY -> price / 3.0
            YEARLY -> price / 12.0
        }
    }

    fun toYearlyAmount(price: Double): Double {
        return when (this) {
            WEEKLY -> price * 52.0
            MONTHLY -> price * 12.0
            QUARTERLY -> price * 4.0
            YEARLY -> price
        }
    }

    companion object {
        fun fromString(value: String): BillingCycle {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: MONTHLY
        }
    }
}

enum class SubscriptionCategory(
    val displayName: String,
    val hexColor: String,
    val defaultColor: Color
) {
    STREAMING("Streaming", "#3B82F6", Color(0xFF3B82F6)),
    CLOUD("Cloud & Storage", "#06B6D4", Color(0xFF06B6D4)),
    PRODUCTIVITY("Productivity & AI", "#8B5CF6", Color(0xFF8B5CF6)),
    GAMING("Gaming", "#EC4899", Color(0xFFEC4899)),
    FITNESS("Health & Fitness", "#10B981", Color(0xFF10B981)),
    FINANCE("Finance & News", "#F59E0B", Color(0xFFF59E0B)),
    UTILITIES("Utilities & Tools", "#6366F1", Color(0xFF6366F1)),
    OTHER("Others", "#64748B", Color(0xFF64748B));

    companion object {
        fun fromString(value: String): SubscriptionCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: OTHER
        }
    }
}

enum class SubscriptionStatus(val displayName: String) {
    ACTIVE("Active"),
    PAUSED("Paused"),
    TRIAL("Trial"),
    CANCELED("Canceled");

    companion object {
        fun fromString(value: String): SubscriptionStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: ACTIVE
        }
    }
}

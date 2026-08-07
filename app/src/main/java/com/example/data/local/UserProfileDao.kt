package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET notificationsEnabled = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)

    @Query("UPDATE user_profile SET defaultReminderDays = :days WHERE id = 1")
    suspend fun updateDefaultReminderDays(days: Int)

    @Query("UPDATE user_profile SET preferredCurrency = :currency WHERE id = 1")
    suspend fun updateCurrency(currency: String)

    @Query("UPDATE user_profile SET isDarkMode = :isDarkMode WHERE id = 1")
    suspend fun updateDarkMode(isDarkMode: Boolean)

    @Query("UPDATE user_profile SET isLoggedIn = :isLoggedIn WHERE id = 1")
    suspend fun updateLoginState(isLoggedIn: Boolean)
}

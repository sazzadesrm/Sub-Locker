package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BillingCycle
import com.example.data.model.Currency
import com.example.data.model.NotificationEntity
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SubscriptionUiState(
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val filteredSubscriptions: List<SubscriptionEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val userProfile: UserProfileEntity = UserProfileEntity(),
    val preferredCurrency: Currency = Currency.USD,
    val totalMonthlySpend: Double = 0.0,
    val totalYearlySpend: Double = 0.0,
    val categorySpendBreakdown: Map<SubscriptionCategory, Double> = emptyMap(),
    val activeCount: Int = 0,
    val upcomingRenewalsCount: Int = 0,
    val searchQuery: String = "",
    val selectedCategoryFilter: String? = null,
    val selectedStatusFilter: String? = null,
    val selectedSortOption: SortOption = SortOption.RENEWAL_DATE,
    val isLoading: Boolean = false
)

enum class SortOption(val label: String) {
    RENEWAL_DATE("Renewal Date"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    NAME("Name (A-Z)");
}

private data class DataState(
    val subs: List<SubscriptionEntity>,
    val notifications: List<NotificationEntity>,
    val profile: UserProfileEntity?
)

private data class FilterState(
    val query: String,
    val catFilter: String?,
    val statusFilter: String?,
    val sortOption: SortOption
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = SubscriptionRepository(
        database.subscriptionDao(),
        database.notificationDao(),
        database.userProfileDao()
    )

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    private val _selectedStatusFilter = MutableStateFlow<String?>(null)
    private val _selectedSortOption = MutableStateFlow(SortOption.RENEWAL_DATE)

    private val dataStateFlow = combine(
        repository.allSubscriptions,
        repository.allNotifications,
        repository.userProfile
    ) { subs, notifications, profile ->
        DataState(subs, notifications, profile)
    }

    private val filterStateFlow = combine(
        _searchQuery,
        _selectedCategoryFilter,
        _selectedStatusFilter,
        _selectedSortOption
    ) { query, catFilter, statusFilter, sortOption ->
        FilterState(query, catFilter, statusFilter, sortOption)
    }

    val uiState: StateFlow<SubscriptionUiState> = combine(
        dataStateFlow,
        filterStateFlow
    ) { data, filters ->
        val subs = data.subs
        val notifications = data.notifications
        val userProf = data.profile ?: UserProfileEntity()
        val prefCurrency = Currency.fromCode(userProf.preferredCurrency)
        val todayEpochDay = LocalDate.now().toEpochDay()

        var filtered = subs.filter { sub ->
            val matchesQuery = filters.query.isBlank() || sub.name.contains(filters.query, ignoreCase = true) || sub.notes.contains(filters.query, ignoreCase = true)
            val matchesCat = filters.catFilter == null || sub.category.equals(filters.catFilter, ignoreCase = true)
            val matchesStatus = filters.statusFilter == null || sub.status.equals(filters.statusFilter, ignoreCase = true)
            matchesQuery && matchesCat && matchesStatus
        }

        filtered = when (filters.sortOption) {
            SortOption.RENEWAL_DATE -> filtered.sortedBy { it.nextRenewalEpochDays }
            SortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending {
                val cycle = BillingCycle.fromString(it.billingCycle)
                val subCurr = Currency.fromCode(it.currency)
                Currency.convert(cycle.toMonthlyAmount(it.price), subCurr, prefCurrency)
            }
            SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy {
                val cycle = BillingCycle.fromString(it.billingCycle)
                val subCurr = Currency.fromCode(it.currency)
                Currency.convert(cycle.toMonthlyAmount(it.price), subCurr, prefCurrency)
            }
            SortOption.NAME -> filtered.sortedBy { it.name.lowercase() }
        }

        var monthlyTotal = 0.0
        val categoryBreakdown = mutableMapOf<SubscriptionCategory, Double>()

        val activeSubs = subs.filter { it.status == "ACTIVE" || it.status == "TRIAL" }
        activeSubs.forEach { sub ->
            val subCurr = Currency.fromCode(sub.currency)
            val cycle = BillingCycle.fromString(sub.billingCycle)
            val monthlyInSubCurr = cycle.toMonthlyAmount(sub.price)
            val monthlyInPrefCurr = Currency.convert(monthlyInSubCurr, subCurr, prefCurrency)

            monthlyTotal += monthlyInPrefCurr

            val cat = SubscriptionCategory.fromString(sub.category)
            val currentCatTotal = categoryBreakdown.getOrDefault(cat, 0.0)
            categoryBreakdown[cat] = currentCatTotal + monthlyInPrefCurr
        }

        val yearlyTotal = monthlyTotal * 12.0
        val upcomingCount = subs.count { sub ->
            sub.status != "CANCELED" && (sub.nextRenewalEpochDays - todayEpochDay) in 0..7
        }

        SubscriptionUiState(
            subscriptions = subs,
            filteredSubscriptions = filtered,
            notifications = notifications,
            userProfile = userProf,
            preferredCurrency = prefCurrency,
            totalMonthlySpend = monthlyTotal,
            totalYearlySpend = yearlyTotal,
            categorySpendBreakdown = categoryBreakdown,
            activeCount = activeSubs.size,
            upcomingRenewalsCount = upcomingCount,
            searchQuery = filters.query,
            selectedCategoryFilter = filters.catFilter,
            selectedStatusFilter = filters.statusFilter,
            selectedSortOption = filters.sortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
    }

    fun setStatusFilter(status: String?) {
        _selectedStatusFilter.value = if (_selectedStatusFilter.value == status) null else status
    }

    fun setSortOption(option: SortOption) {
        _selectedSortOption.value = option
    }

    fun addSubscription(
        name: String,
        price: Double,
        currency: String,
        billingCycle: String,
        category: String,
        renewalEpochDays: Long,
        paymentMethod: String,
        reminderDaysBefore: Int,
        notes: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            repository.addSubscription(
                SubscriptionEntity(
                    name = name,
                    price = price,
                    currency = currency,
                    billingCycle = billingCycle,
                    category = category,
                    nextRenewalEpochDays = renewalEpochDays,
                    status = "ACTIVE",
                    isAutoRenew = true,
                    paymentMethod = paymentMethod,
                    reminderDaysBefore = reminderDaysBefore,
                    notes = notes,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.updateSubscription(subscription)
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }

    fun togglePauseSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.togglePauseSubscription(subscription)
        }
    }

    fun changePreferredCurrency(currencyCode: String) {
        viewModelScope.launch {
            repository.updateCurrency(currencyCode)
        }
    }

    fun toggleDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            repository.updateDarkMode(isDarkMode)
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val current = uiState.value.userProfile
            repository.updateUserProfile(
                current.copy(
                    name = name,
                    email = email,
                    phone = phone
                )
            )
        }
    }

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun setLoginState(isLoggedIn: Boolean) {
        viewModelScope.launch {
            repository.setLoginState(isLoggedIn)
        }
    }
}

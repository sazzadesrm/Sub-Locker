package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.SubscriptionEntity
import com.example.ui.components.AddEditSubscriptionBottomSheet
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.SubifyTheme

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Filled.Home, Icons.Outlined.Home, "tab_dashboard"),
    ANALYTICS("Analytics", Icons.Filled.PieChart, Icons.Outlined.PieChart, "tab_analytics"),
    ALERTS("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications, "tab_alerts"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

@Composable
fun SubscriptionApp(
    viewModel: SubscriptionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionEntity?>(null) }

    val isDarkMode = uiState.userProfile.isDarkMode

    SubifyTheme(darkTheme = isDarkMode) {
        if (!uiState.userProfile.isLoggedIn) {
            LoginScreen(
                onLoginSuccess = { viewModel.setLoginState(true) }
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        val unreadCount = uiState.notifications.count { !it.isRead }

                        NavigationTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentTab = tab
                                },
                                modifier = Modifier.testTag(tab.testTag),
                                icon = {
                                    if (tab == NavigationTab.ALERTS && unreadCount > 0) {
                                        BadgedBox(
                                            badge = { Badge { Text(unreadCount.toString()) } }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    }
                                },
                                label = { Text(text = tab.title) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(
                        targetState = currentTab,
                        label = "MainNavigationAnimation"
                    ) { tab ->
                        when (tab) {
                            NavigationTab.DASHBOARD -> DashboardScreen(
                                state = uiState,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onCategoryFilterSelect = { viewModel.setCategoryFilter(it) },
                                onSortOptionSelect = { viewModel.setSortOption(it) },
                                onAddClick = {
                                    subscriptionToEdit = null
                                    showAddEditSheet = true
                                },
                                onEditSubscription = { sub ->
                                    subscriptionToEdit = sub
                                    showAddEditSheet = true
                                },
                                onDeleteSubscription = { sub -> viewModel.deleteSubscription(sub) },
                                onTogglePauseSubscription = { sub -> viewModel.togglePauseSubscription(sub) },
                                onMarkSubscriptionPaid = { sub -> viewModel.markAsPaid(sub) },
                                onCurrencyChange = { viewModel.changePreferredCurrency(it) },
                                onRefreshRates = { viewModel.refreshData() },
                                onNavigateToAlerts = { currentTab = NavigationTab.ALERTS }
                            )

                            NavigationTab.ANALYTICS -> AnalyticsScreen(
                                state = uiState,
                                onCurrencyChange = { viewModel.changePreferredCurrency(it) }
                            )

                            NavigationTab.ALERTS -> AlertsScreen(
                                state = uiState,
                                onMarkRead = { viewModel.markNotificationRead(it) },
                                onClearAll = { viewModel.clearAllNotifications() },
                                onToggleNotifications = { viewModel.toggleNotificationsEnabled(it) },
                                onUpdateReminderLeadTime = { viewModel.updateDefaultReminderDays(it) },
                                onTriggerScan = { viewModel.triggerManualRenewalScan() }
                            )

                            NavigationTab.PROFILE -> ProfileScreen(
                                state = uiState,
                                onUpdateProfile = { name, email, phone ->
                                    viewModel.updateUserProfile(name, email, phone)
                                },
                                onCurrencyChange = { viewModel.changePreferredCurrency(it) },
                                onDarkModeToggle = { viewModel.toggleDarkMode(it) },
                                onSignOut = { viewModel.setLoginState(false) }
                            )
                        }
                    }
                }

                if (showAddEditSheet) {
                    AddEditSubscriptionBottomSheet(
                        subscriptionToEdit = subscriptionToEdit,
                        onDismiss = { showAddEditSheet = false },
                        onSave = { name, price, currency, cycle, cat, renewalEpoch, paymentMethod, reminderDays, notes, colorHex ->
                            if (subscriptionToEdit == null) {
                                viewModel.addSubscription(
                                    name = name,
                                    price = price,
                                    currency = currency,
                                    billingCycle = cycle,
                                    category = cat,
                                    renewalEpochDays = renewalEpoch,
                                    paymentMethod = paymentMethod,
                                    reminderDaysBefore = reminderDays,
                                    notes = notes,
                                    colorHex = colorHex
                                )
                            } else {
                                viewModel.updateSubscription(
                                    subscriptionToEdit!!.copy(
                                        name = name,
                                        price = price,
                                        currency = currency,
                                        billingCycle = cycle,
                                        category = cat,
                                        nextRenewalEpochDays = renewalEpoch,
                                        paymentMethod = paymentMethod,
                                        reminderDaysBefore = reminderDays,
                                        notes = notes,
                                        colorHex = colorHex
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

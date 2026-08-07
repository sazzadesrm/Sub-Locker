package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillingCycle
import com.example.data.model.Currency
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryAnalyticsFilterCard(
    subscriptions: List<SubscriptionEntity>,
    preferredCurrency: Currency,
    onCategoryFilterSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Enabled category toggles state (default all enabled)
    val enabledCategories = remember {
        mutableStateListOf<SubscriptionCategory>().apply {
            addAll(SubscriptionCategory.entries)
        }
    }

    var isFilterPanelExpanded by remember { mutableStateOf(true) }

    val activeSubscriptions = remember(subscriptions) {
        subscriptions.filter { it.status == "ACTIVE" || it.status == "TRIAL" }
    }

    // Calculate category spending breakdown for visible categories
    val categorySpendMap = remember(activeSubscriptions, preferredCurrency) {
        val map = mutableMapOf<SubscriptionCategory, Double>()
        activeSubscriptions.forEach { sub ->
            val cat = SubscriptionCategory.fromString(sub.category)
            val subCurr = Currency.fromCode(sub.currency)
            val cycle = BillingCycle.fromString(sub.billingCycle)
            val monthlyInSubCurr = cycle.toMonthlyAmount(sub.price)
            val monthlyInPrefCurr = Currency.convert(monthlyInSubCurr, subCurr, preferredCurrency)
            map[cat] = (map[cat] ?: 0.0) + monthlyInPrefCurr
        }
        map
    }

    // Filtered total spend based on toggled category visibility
    val filteredTotalSpend = remember(categorySpendMap, enabledCategories.toList()) {
        categorySpendMap.entries
            .filter { enabledCategories.contains(it.key) }
            .sumOf { it.value }
    }

    val totalUnfilteredSpend = remember(categorySpendMap) {
        categorySpendMap.values.sum()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_analytics_filter_card")
            .animateContentSize(animationSpec = tween(300)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Title & Expand / Toggle Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Category Analytics & Filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toggle category visibility to update spending analytics",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isFilterPanelExpanded = !isFilterPanelExpanded
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("toggle_category_filter_panel_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isFilterPanelExpanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isFilterPanelExpanded) "Hide Toggles" else "Filter Categories",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Toggles: Enable All / Disable All
            AnimatedVisibility(
                visible = isFilterPanelExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORIES (${enabledCategories.size}/${SubscriptionCategory.entries.size} VISIBLE)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    enabledCategories.clear()
                                    enabledCategories.addAll(SubscriptionCategory.entries)
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("select_all_categories_button")
                            ) {
                                Text("Select All", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    enabledCategories.clear()
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("deselect_all_categories_button")
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Toggle Chips (Wrap layout via FlowRow)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubscriptionCategory.entries.forEach { category ->
                            val isEnabled = enabledCategories.contains(category)
                            val categorySpend = categorySpendMap[category] ?: 0.0

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isEnabled) {
                                        enabledCategories.remove(category)
                                    } else {
                                        enabledCategories.add(category)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isEnabled) category.defaultColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isEnabled) category.defaultColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.testTag("category_toggle_chip_${category.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isEnabled) category.defaultColor else Color.Gray)
                                    )

                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (isEnabled) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = category.defaultColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Filtered Analytics Summary Header
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selected Category Spend",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${Currency.format(filteredTotalSpend, preferredCurrency)} / mo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val percentage = if (totalUnfilteredSpend > 0) (filteredTotalSpend / totalUnfilteredSpend * 100) else 0.0
                        Text(
                            text = "${String.format("%.1f", percentage)}% of total spend",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${enabledCategories.size} categories active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Analytics Progress Bars Breakdown
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val visibleCategoriesWithSpend = SubscriptionCategory.entries.filter {
                    enabledCategories.contains(it) && (categorySpendMap[it] ?: 0.0) > 0
                }

                if (visibleCategoriesWithSpend.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active spending in selected categories.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    visibleCategoriesWithSpend.forEach { category ->
                        val spend = categorySpendMap[category] ?: 0.0
                        val fraction = if (filteredTotalSpend > 0) (spend / filteredTotalSpend).toFloat() else 0f
                        val subCount = activeSubscriptions.count { SubscriptionCategory.fromString(it.category) == category }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCategoryFilterSelect(category.name)
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(category.defaultColor)
                                    )
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "($subCount sub${if (subCount > 1) "s" else ""})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "${Currency.format(spend, preferredCurrency)} (${(fraction * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = category.defaultColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

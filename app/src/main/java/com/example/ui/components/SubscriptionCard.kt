package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillingCycle
import com.example.data.model.Currency
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionEntity
import java.time.LocalDate

@Composable
fun SubscriptionCard(
    subscription: SubscriptionEntity,
    displayCurrency: Currency,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subCurrency = Currency.fromCode(subscription.currency)
    val convertedAmount = Currency.convert(subscription.price, subCurrency, displayCurrency)
    val formattedPrice = Currency.format(convertedAmount, displayCurrency)
    val billingCycle = BillingCycle.fromString(subscription.billingCycle)
    val category = SubscriptionCategory.fromString(subscription.category)

    val todayEpoch = LocalDate.now().toEpochDay()
    val daysUntilRenewal = (subscription.nextRenewalEpochDays - todayEpoch).toInt()

    var showMenu by remember { mutableStateOf(false) }

    val renewalStatusText = when {
        subscription.status == "PAUSED" -> "Paused"
        subscription.status == "CANCELED" -> "Canceled"
        daysUntilRenewal < 0 -> "Overdue"
        daysUntilRenewal == 0 -> "Renews Today"
        daysUntilRenewal == 1 -> "Renews Tomorrow"
        daysUntilRenewal in 2..7 -> "Renews in $daysUntilRenewal days"
        else -> "Renews in ${daysUntilRenewal / 30}m ${daysUntilRenewal % 30}d"
    }

    val badgeColor = when {
        subscription.status == "PAUSED" -> MaterialTheme.colorScheme.outline
        daysUntilRenewal in 0..3 -> MaterialTheme.colorScheme.error
        daysUntilRenewal in 4..7 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val categoryIcon = getCategoryIcon(category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subscription_card_${subscription.id}")
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon + Name & Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(category.defaultColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = category.displayName,
                            tint = category.defaultColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (subscription.isAutoRenew && subscription.status == "ACTIVE") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = "Auto-renew active",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Price & Cycle
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/${billingCycle.label.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // More Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("subscription_menu_${subscription.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Subscription actions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Subscription") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (subscription.status == "PAUSED") "Resume Subscription" else "Pause Subscription") },
                            onClick = {
                                showMenu = false
                                onTogglePause()
                            },
                            leadingIcon = {
                                Icon(
                                    if (subscription.status == "PAUSED") Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Renewal status pill & Payment method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = renewalStatusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (subscription.paymentMethod.isNotEmpty()) {
                    Text(
                        text = "Paid via ${subscription.paymentMethod}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: SubscriptionCategory): ImageVector {
    return when (category) {
        SubscriptionCategory.STREAMING -> Icons.Default.Movie
        SubscriptionCategory.CLOUD -> Icons.Default.Cloud
        SubscriptionCategory.PRODUCTIVITY -> Icons.Default.Work
        SubscriptionCategory.GAMING -> Icons.Default.Gamepad
        SubscriptionCategory.FITNESS -> Icons.Default.FitnessCenter
        SubscriptionCategory.FINANCE -> Icons.Default.Star
        SubscriptionCategory.UTILITIES -> Icons.Default.Work
        SubscriptionCategory.OTHER -> Icons.Default.Star
    }
}

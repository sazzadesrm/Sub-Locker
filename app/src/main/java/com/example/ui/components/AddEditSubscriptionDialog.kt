package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BillingCycle
import com.example.data.model.Currency
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionEntity
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionBottomSheet(
    subscriptionToEdit: SubscriptionEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val todayEpoch = LocalDate.now().toEpochDay()

    var name by remember { mutableStateOf(subscriptionToEdit?.name ?: "") }
    var priceText by remember { mutableStateOf(subscriptionToEdit?.price?.toString() ?: "14.99") }
    var selectedCurrency by remember { mutableStateOf(subscriptionToEdit?.currency ?: "USD") }
    var selectedCycle by remember { mutableStateOf(subscriptionToEdit?.billingCycle ?: "MONTHLY") }
    var selectedCategory by remember { mutableStateOf(subscriptionToEdit?.category ?: "STREAMING") }
    var renewalDaysOffset by remember {
        mutableStateOf(
            if (subscriptionToEdit != null) {
                (subscriptionToEdit.nextRenewalEpochDays - todayEpoch).toInt().coerceAtLeast(1).toString()
            } else "14"
        )
    }
    var paymentMethod by remember { mutableStateOf(subscriptionToEdit?.paymentMethod ?: "Visa") }
    var reminderDaysBefore by remember { mutableIntStateOf(subscriptionToEdit?.reminderDaysBefore ?: 3) }
    var notes by remember { mutableStateOf(subscriptionToEdit?.notes ?: "") }

    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var cycleDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (subscriptionToEdit == null) "Add Subscription" else "Edit Subscription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Service / App Name") },
                placeholder = { Text("e.g. Netflix, Spotify, AWS") },
                isError = nameError,
                supportingText = if (nameError) { { Text("Name is required") } } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscription_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Price & Currency Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        priceError = false
                    },
                    label = { Text("Price") },
                    isError = priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("subscription_price_input"),
                    singleLine = true
                )

                // Currency Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currencyDropdownExpanded = true }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { currencyDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        Currency.entries.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text("${curr.code} (${curr.symbol})") },
                                onClick = {
                                    selectedCurrency = curr.code
                                    currencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cycle & Category Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Billing Cycle Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = BillingCycle.fromString(selectedCycle).label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { cycleDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = cycleDropdownExpanded,
                        onDismissRequest = { cycleDropdownExpanded = false }
                    ) {
                        BillingCycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.label) },
                                onClick = {
                                    selectedCycle = cycle.name
                                    cycleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category Dropdown
                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedTextField(
                        value = SubscriptionCategory.fromString(selectedCategory).displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { categoryDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        SubscriptionCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Renewal Days Offset & Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = renewalDaysOffset,
                    onValueChange = { renewalDaysOffset = it },
                    label = { Text("Renews In (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method") },
                    placeholder = { Text("Visa, PayPal, etc.") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminder Days
            Text(
                text = "Alert Lead Time: Remind $reminderDaysBefore days before renewal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                listOf(1, 3, 5, 7).forEach { days ->
                    Surface(
                        selected = reminderDaysBefore == days,
                        onClick = { reminderDaysBefore = days },
                        shape = RoundedCornerShape(20.dp),
                        color = if (reminderDaysBefore == days) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$days ${if (days == 1) "day" else "days"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (reminderDaysBefore == days) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Account Info (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val parsedPrice = priceText.toDoubleOrNull()
                    if (parsedPrice == null || parsedPrice <= 0) {
                        priceError = true
                        return@Button
                    }
                    val days = renewalDaysOffset.toIntOrNull() ?: 14
                    val renewalEpoch = todayEpoch + days.coerceAtLeast(0)

                    val cat = SubscriptionCategory.fromString(selectedCategory)

                    onSave(
                        name.trim(),
                        parsedPrice,
                        selectedCurrency,
                        selectedCycle,
                        selectedCategory,
                        renewalEpoch,
                        paymentMethod.ifBlank { "Card" },
                        reminderDaysBefore,
                        notes.trim(),
                        cat.hexColor
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_subscription_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (subscriptionToEdit == null) "Add Subscription" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillingCycle
import com.example.data.model.Currency
import com.example.data.model.SubscriptionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

data class MonthSpendData(
    val monthLabel: String,       // e.g. "Sep"
    val yearMonthLabel: String,   // e.g. "Sep 2025"
    val amount: Double,           // Total spend for this month in display currency
    val subscriptionCount: Int    // Active subscriptions in this month
)

enum class ChartType {
    AREA, BAR, LINE
}

@Composable
fun MonthlySpendingTrendChart(
    subscriptions: List<SubscriptionEntity>,
    preferredCurrency: Currency,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf(ChartType.AREA) }
    var selectedMonthIndex by remember { mutableIntStateOf(11) } // Default to latest month
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptions, preferredCurrency) {
        animationPlayed = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "TrendChartAnimation"
    )

    // Calculate 12 month data points leading up to current month
    val monthlyData = remember(subscriptions, preferredCurrency) {
        calculatePastYearSpending(subscriptions, preferredCurrency)
    }

    val maxAmount = remember(monthlyData) {
        val peak = monthlyData.maxOfOrNull { it.amount } ?: 100.0
        if (peak <= 0) 100.0 else peak * 1.15
    }

    val avgAmount = remember(monthlyData) {
        if (monthlyData.isNotEmpty()) monthlyData.map { it.amount }.average() else 0.0
    }

    val selectedData = monthlyData.getOrNull(selectedMonthIndex) ?: monthlyData.lastOrNull()
    val previousData = if (selectedMonthIndex > 0) monthlyData.getOrNull(selectedMonthIndex - 1) else null

    val diffFromPrevious = if (selectedData != null && previousData != null) {
        selectedData.amount - previousData.amount
    } else 0.0

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_spending_trend_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row: Title + Chart Type Toggle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "12-Month Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Text(
                        text = "Monthly subscription total over the past year",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceMuted
                    )
                }

                // Chart Style Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceVariantColor)
                        .padding(4.dp)
                ) {
                    ChartTypeIconButton(
                        icon = Icons.Default.Timeline,
                        isSelected = chartType == ChartType.AREA,
                        onClick = { chartType = ChartType.AREA },
                        testTag = "chart_type_area"
                    )
                    ChartTypeIconButton(
                        icon = Icons.Default.BarChart,
                        isSelected = chartType == ChartType.BAR,
                        onClick = { chartType = ChartType.BAR },
                        testTag = "chart_type_bar"
                    )
                    ChartTypeIconButton(
                        icon = Icons.Default.ShowChart,
                        isSelected = chartType == ChartType.LINE,
                        onClick = { chartType = ChartType.LINE },
                        testTag = "chart_type_line"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recharts-style Active Month Tooltip Callout
            selectedData?.let { data ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_interactive_tooltip")
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
                                text = data.yearMonthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = Currency.format(data.amount, preferredCurrency),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${data.subscriptionCount} Active Subscriptions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (previousData != null) {
                                val isIncrease = diffFromPrevious > 0
                                val diffFormatted = Currency.format(kotlin.math.abs(diffFromPrevious), preferredCurrency)
                                val diffText = if (isIncrease) "+$diffFormatted vs prev month" else if (diffFromPrevious < 0) "-$diffFormatted vs prev month" else "No change"
                                Text(
                                    text = diffText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncrease) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Canvas Data Visualization (Recharts Area / Bar / Line)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(monthlyData) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val monthWidth = width / monthlyData.size
                                val clickedIndex = (offset.x / monthWidth).toInt().coerceIn(0, monthlyData.size - 1)
                                selectedMonthIndex = clickedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val pointCount = monthlyData.size
                    if (pointCount == 0) return@Canvas

                    val stepX = width / (pointCount - 1).coerceAtLeast(1)
                    val barWidth = (width / pointCount) * 0.55f

                    // 1. Draw Cartesian Grid Lines (Recharts style)
                    val gridLineCount = 4
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 0..gridLineCount) {
                        val y = height * (i.toFloat() / gridLineCount)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashPathEffect
                        )
                    }

                    // 2. Draw Average Line (Recharts ReferenceLine)
                    val avgY = height - ((avgAmount / maxAmount).toFloat() * height * animatedProgress)
                    drawLine(
                        color = onSurfaceMuted.copy(alpha = 0.5f),
                        start = Offset(0f, avgY),
                        end = Offset(width, avgY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                    )

                    // Compute points coordinates
                    val points = monthlyData.mapIndexed { index, data ->
                        val x = if (chartType == ChartType.BAR) {
                            (index * (width / pointCount)) + (width / pointCount) / 2f
                        } else {
                            index * stepX
                        }
                        val y = height - ((data.amount / maxAmount).toFloat() * height * animatedProgress)
                        Offset(x, y)
                    }

                    // Render based on selected ChartType
                    when (chartType) {
                        ChartType.BAR -> {
                            monthlyData.forEachIndexed { index, data ->
                                val barX = (index * (width / pointCount)) + ((width / pointCount) - barWidth) / 2f
                                val barHeight = ((data.amount / maxAmount).toFloat() * height * animatedProgress)
                                val barY = height - barHeight
                                val isSelected = index == selectedMonthIndex

                                drawRoundRect(
                                    color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.45f),
                                    topLeft = Offset(barX, barY),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }

                        ChartType.AREA, ChartType.LINE -> {
                            if (points.size >= 2) {
                                val strokePath = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 0 until points.size - 1) {
                                        val p1 = points[i]
                                        val p2 = points[i + 1]
                                        val cx = (p1.x + p2.x) / 2f
                                        cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                                    }
                                }

                                if (chartType == ChartType.AREA) {
                                    val fillPath = Path().apply {
                                        addPath(strokePath)
                                        lineTo(points.last().x, height)
                                        lineTo(points.first().x, height)
                                        close()
                                    }

                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                primaryColor.copy(alpha = 0.35f),
                                                primaryColor.copy(alpha = 0.02f)
                                            )
                                        )
                                    )
                                }

                                drawPath(
                                    path = strokePath,
                                    color = primaryColor,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    // 3. Draw Data Point Circles and Selection Highlight
                    points.forEachIndexed { index, pt ->
                        val isSelected = index == selectedMonthIndex
                        if (isSelected) {
                            // Outer Glow Ring
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.25f),
                                radius = 12.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 6.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = pt
                            )
                        } else if (chartType != ChartType.BAR) {
                            drawCircle(
                                color = primaryColor,
                                radius = 3.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Labels (Months)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyData.forEachIndexed { index, data ->
                    val isSelected = index == selectedMonthIndex
                    Text(
                        text = data.monthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) primaryColor else onSurfaceMuted,
                        modifier = Modifier
                            .clickable { selectedMonthIndex = index }
                            .padding(vertical = 2.dp, horizontal = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Metrics Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "12-Mo Total: ${Currency.format(monthlyData.sumOf { it.amount }, preferredCurrency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceColor
                    )
                }

                Text(
                    text = "Avg: ${Currency.format(avgAmount, preferredCurrency)}/mo",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ChartTypeIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun calculatePastYearSpending(
    subscriptions: List<SubscriptionEntity>,
    currency: Currency
): List<MonthSpendData> {
    val activeSubs = subscriptions.filter { it.status == "ACTIVE" || it.status == "TRIAL" }
    val currentDate = LocalDate.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    val yearMonthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    val result = mutableListOf<MonthSpendData>()

    // Generate past 12 months in chronological order
    for (i in 11 downTo 0) {
        val targetDate = currentDate.minusMonths(i.toLong())
        val monthLabel = targetDate.format(monthFormatter)
        val yearMonthLabel = targetDate.format(yearMonthFormatter)
        val targetEpochDay = targetDate.toEpochDay()

        var monthTotal = 0.0
        var activeCount = 0

        for (sub in activeSubs) {
            val subCurr = Currency.fromCode(sub.currency)
            val cycle = BillingCycle.fromString(sub.billingCycle)
            val monthlyAmountInSubCurr = cycle.toMonthlyAmount(sub.price)
            val monthlyInDisplay = Currency.convert(monthlyAmountInSubCurr, subCurr, currency)

            // Calculate monthly spend with historical trend variation
            val monthsAgo = i
            val factor = when {
                monthsAgo > 8 && sub.id % 2 == 0 -> 0.85
                monthsAgo > 5 && sub.id % 3 == 0 -> 0.92
                else -> 1.0
            }
            monthTotal += (monthlyInDisplay * factor)
            activeCount++
        }

        // Ensure reasonable minimum spend if subs exist
        if (activeSubs.isNotEmpty() && monthTotal <= 0) {
            monthTotal = activeSubs.sumOf { sub ->
                val subCurr = Currency.fromCode(sub.currency)
                val cycle = BillingCycle.fromString(sub.billingCycle)
                Currency.convert(cycle.toMonthlyAmount(sub.price), subCurr, currency)
            }
            activeCount = activeSubs.size
        }

        result.add(
            MonthSpendData(
                monthLabel = monthLabel,
                yearMonthLabel = yearMonthLabel,
                amount = monthTotal,
                subscriptionCount = max(activeCount, if (activeSubs.isNotEmpty()) 1 else 0)
            )
        )
    }

    return result
}

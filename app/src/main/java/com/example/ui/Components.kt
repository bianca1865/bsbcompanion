package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetAllocation
import com.example.ui.theme.*
import com.example.viewmodel.WeeklyDataPoint

/**
 * 1. Weekly Trend Bar Chart
 */
@Composable
fun SimpleBarChart(
    data: List<WeeklyDataPoint>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { point ->
            val heightFactor = (point.amount / maxValue).toFloat().coerceIn(0.01f, 1f)
            val animatedHeight by animateFloatAsState(
                targetValue = heightFactor,
                animationSpec = tween(1000),
                label = "barHeight"
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "P${point.amount.toInt()}",
                    color = TextPrimary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((80 * animatedHeight).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if(point.amount > 0) CoralOrange else NavyPrimary.coerceOnLight(0.1f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = point.day,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Extension to adjust colors for light mode readability if needed
 */
@Composable
fun Color.coerceOnLight(alpha: Float): Color {
    return if (!isDarkThemeGlobal) this.copy(alpha = alpha) else this
}

/**
 * 2. Category Breakdown Donut Chart
 */
@Composable
fun SpendingDonutChart(
    categories: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalSum = categories.values.sum()
    val totalForCalc = totalSum.coerceAtLeast(1.0)
    val colorList = listOf(CoralOrange, GoldOrange, BlueAccent, Color(0xFF00B4D8), Color(0xFF9C27B0), Color(0xFF4CAF50), Color(0xFFFFEB3B), Color(0xFFE91E63))
    
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                if (categories.isEmpty()) {
                    drawArc(
                        color = if (isDarkThemeGlobal) NavyPrimary else Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 24f)
                    )
                } else {
                    categories.values.forEachIndexed { index, value ->
                        val sweepAngle = (value / totalForCalc * 360f).toFloat()
                        drawArc(
                            color = colorList.getOrElse(index) { Color.Gray },
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 24f, cap = StrokeCap.Round)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Spent", color = TextMuted, fontSize = 10.sp)
                Text("P${totalSum.toInt()}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (categories.isEmpty()) {
                Text("No data", color = TextMuted, fontSize = 12.sp)
            } else {
                categories.entries.take(5).forEachIndexed { index, entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(colorList.getOrElse(index) { Color.Gray }))
                        Spacer(Modifier.width(8.dp))
                        Text("${entry.key}: P${entry.value.toInt()}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/**
 * 3. Spending Utilisation Ring (Gauge)
 */
@Composable
fun AllowanceProgressRing(
    spent: Double,
    total: Double,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (total > 0) (spent / total).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(1500),
        label = "gauge"
    )
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = if (isDarkThemeGlobal) NavyPrimary else Color.LightGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 18f)
            )
            drawArc(
                color = if (targetProgress > 0.9f) Color.Red else CoralOrange,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = 18f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(targetProgress * 100).toInt()}%", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Used", color = TextMuted, fontSize = 10.sp)
        }
    }
}

/**
 * 4. Category Performance Bars
 */
@Composable
fun CategoryComparisonChart(
    allocations: List<BudgetAllocation>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (allocations.isEmpty()) {
            Text("No budgets defined yet.", color = TextMuted, fontSize = 13.sp)
        } else {
            allocations.take(4).forEach { alloc ->
                val progress = if (alloc.allocatedAmount > 0) (alloc.spentAmount / alloc.allocatedAmount).toFloat().coerceIn(0f, 1.2f) else 0f
                val animatedWidth by animateFloatAsState(targetValue = progress.coerceAtMost(1f), animationSpec = tween(1000), label = "bar")
                
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(alloc.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("P${alloc.spentAmount.toInt()} / P${alloc.allocatedAmount.toInt()}", color = TextMuted, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(if (isDarkThemeGlobal) NavyPrimary else Color.LightGray.coerceOnLight(0.2f))) {
                        Box(modifier = Modifier
                            .fillMaxWidth(animatedWidth)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (progress > 1f) Color.Red else GoldOrange))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * 1. Weekly Trend Bar Chart
 * Visualises spending across weeks with animations.
 */
@Composable
fun SimpleBarChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            val heightFactor = (value / maxValue).toFloat().coerceIn(0.05f, 1f)
            val animatedHeight by animateFloatAsState(
                targetValue = heightFactor,
                animationSpec = tween(1000),
                label = "barHeight"
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CoralOrange)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label.take(3),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 2. Category Breakdown Donut Chart
 * Segmented spending categories with a legend.
 */
@Composable
fun SpendingDonutChart(
    categories: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val total = categories.values.sum().coerceAtLeast(1.0)
    val colors = listOf(CoralOrange, GoldOrange, BlueAccent, Color.Cyan, Color(0xFF9C27B0), Color(0xFF4CAF50))
    
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                categories.values.forEachIndexed { index, value ->
                    val sweepAngle = (value / total * 360f).toFloat()
                    drawArc(
                        color = colors.getOrElse(index) { Color.Gray },
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 20f, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total", color = TextMuted, fontSize = 8.sp)
                Text("P${total.toInt()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            categories.keys.take(4).forEachIndexed { index, name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(colors.getOrElse(index) { Color.Gray }))
                    Spacer(Modifier.width(6.dp))
                    Text(name, color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * 3. Spending Utilisation Ring (Gauge)
 * Large visual for overall allowance consumption.
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
                color = NavyPrimary,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 16f)
            )
            drawArc(
                color = if (targetProgress > 0.9f) Color.Red else CoralOrange,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(targetProgress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text("Utilised", color = TextMuted, fontSize = 10.sp)
        }
    }
}

/**
 * 4. Category Performance Bars
 * Comparison of spent vs allocated for top categories.
 */
@Composable
fun CategoryComparisonChart(
    allocations: List<BudgetAllocation>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (allocations.isEmpty()) {
            Text("No budgets set.", color = TextMuted, fontSize = 12.sp)
        } else {
            allocations.take(4).forEach { alloc ->
                val progress = if (alloc.allocatedAmount > 0) (alloc.spentAmount / alloc.allocatedAmount).toFloat().coerceIn(0f, 1.2f) else 0f
                val animatedWidth by animateFloatAsState(targetValue = progress.coerceAtMost(1f), animationSpec = tween(1000), label = "bar")
                
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(alloc.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("P${alloc.spentAmount.toInt()} / P${alloc.allocatedAmount.toInt()}", color = TextMuted, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(NavyPrimary)) {
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
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SpendingDonutPlaceholder(categories: Map<String, Double>, modifier: Modifier = Modifier) {
    SpendingDonutChart(categories, modifier)
}

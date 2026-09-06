package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * PRIORITY 2: What-If Expense Impact Simulator Card
 * Enables students to test potential expenses before spending,
 * demonstrating how a single purchase changes their daily allowance pacing.
 */
@Composable
fun WhatIfExpenseCalculatorCard(
    summary: FinancialSummary,
    onLogWhatIfExpense: (title: String, amount: Double) -> Unit
) {
    var whatIfAmountText by remember { mutableStateOf("100") }
    val whatIfAmount = whatIfAmountText.toDoubleOrNull() ?: 0.0
    val daysLeft = summary.daysRemainingInCycle.coerceAtLeast(1)
    val projectedRemaining = (summary.remainingFreeToSpend - whatIfAmount).coerceAtLeast(0.0)
    val projectedDaily = projectedRemaining / daysLeft
    val dailyDrop = (summary.safeDailySpend - projectedDaily).coerceAtLeast(0.0)

    val projectedStatusColor = when {
        projectedDaily >= 65.0 -> BsbEmeraldGreen
        projectedDaily >= 40.0 -> BsbCyanSafe
        projectedDaily >= 20.0 -> BsbAmberGold
        else -> BsbCrimsonWarning
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        border = BorderStroke(1.dp, BsbCyanSafe.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = BsbCyanSafe,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "PRIORITY 2 • WHAT-IF PACING SIMULATOR",
                            color = BsbCyanSafe,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Test Spending Impact Before You Buy",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = BsbCyanSafe.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "LIVE SIMULATION",
                        color = BsbCyanSafe,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "See how a purchase today alters your safe daily allowance for the remaining $daysLeft days until DTEF payday.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Preset chips for typical student spending
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("Kombi + Snack", "25"),
                    Pair("Cafeteria Meal", "50"),
                    Pair("Weekend Outing", "100"),
                    Pair("Books / Printing", "180"),
                    Pair("Clothes / Shoes", "250")
                ).forEach { (label, amt) ->
                    val isSelected = whatIfAmountText == amt
                    Surface(
                        color = if (isSelected) BsbCyanSafe else BsbNavySurface,
                        border = BorderStroke(1.dp, if (isSelected) BsbCyanSafe else Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { whatIfAmountText = amt }
                    ) {
                        Text(
                            text = "$label (P$amt)",
                            color = if (isSelected) BsbNavyDeep else Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Custom amount input
            OutlinedTextField(
                value = whatIfAmountText,
                onValueChange = { whatIfAmountText = it },
                label = { Text("What if I spend Pula (P)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BsbCyanSafe,
                    unfocusedBorderColor = Color(0xFF2C436F),
                    focusedLabelColor = BsbCyanSafe
                ),
                modifier = Modifier.fillMaxWidth().testTag("input_what_if_amount")
            )

            // Simulation Result Box
            Surface(
                color = BsbNavySurface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, projectedStatusColor.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT SAFE PACING",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PROJECTED NEW PACING",
                            color = projectedStatusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "P ${String.format("%.2f", summary.safeDailySpend)}/day",
                            color = Color(0xFFCBD5E1),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "➔  ",
                                color = projectedStatusColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "P ${String.format("%.2f", projectedDaily)}/day",
                                color = projectedStatusColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (dailyDrop > 0) "-P${String.format("%.2f", dailyDrop)}/day reduction" else "No change",
                            color = if (dailyDrop > 10.0) BsbCrimsonWarning else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (projectedDaily >= 50.0) "✓ Pacing remains safe" else "⚠️ Caution: Tightens budget",
                            color = projectedStatusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action button to record if student actually proceeds
            if (whatIfAmount > 0) {
                OutlinedButton(
                    onClick = {
                        onLogWhatIfExpense("Planned Spend (P${String.format("%.2f", whatIfAmount)})", whatIfAmount)
                    },
                    border = BorderStroke(1.dp, BsbCyanSafe),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_log_what_if_expense")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = BsbCyanSafe, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Proceed & Log P${String.format("%.2f", whatIfAmount)} Expense",
                        color = BsbCyanSafe,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * PRIORITY 3: Add Recurring Committed Bill Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommittedBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, dueDay: Int, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDayText by remember { mutableStateOf("15") }
    var selectedCategory by remember { mutableStateOf("Rent & Accommodation") }

    val categories = listOf(
        "Rent & Accommodation",
        "Connectivity & Wifi",
        "Savings Reserve",
        "Mobile Subscription",
        "Campus Transport",
        "Education & Books"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BsbNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = BsbAmberGold)
                Text(
                    text = "Add Committed Obligation",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Registered bills are ring-fenced in your BSB account so money cannot be accidentally spent on debit card transactions.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )

                // Quick presets
                Text("Popular Tertiary Obligations:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("Room Rent (UB/BUAN)", "650.00", "Rent & Accommodation"),
                        Triple("Mascom Campus Wifi", "149.00", "Connectivity & Wifi"),
                        Triple("Orange Data Bundle", "99.00", "Mobile Subscription"),
                        Triple("Sesame Monthly Saver", "200.00", "Savings Reserve")
                    ).forEach { (presetTitle, presetAmt, presetCat) ->
                        Surface(
                            color = BsbNavySurface,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                title = presetTitle
                                amountText = presetAmt
                                selectedCategory = presetCat
                            }
                        ) {
                            Text(
                                text = presetTitle,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Obligation Name / Payee") },
                    placeholder = { Text("e.g. Landlord Rent (Block 6)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BsbAmberGold,
                        unfocusedBorderColor = Color(0xFF2C436F),
                        focusedLabelColor = BsbAmberGold
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_bill_title")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (P)") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BsbAmberGold,
                            unfocusedBorderColor = Color(0xFF2C436F),
                            focusedLabelColor = BsbAmberGold
                        ),
                        modifier = Modifier.weight(1f).testTag("input_bill_amount")
                    )

                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it },
                        label = { Text("Due Day") },
                        placeholder = { Text("1-31") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BsbAmberGold,
                            unfocusedBorderColor = Color(0xFF2C436F),
                            focusedLabelColor = BsbAmberGold
                        ),
                        modifier = Modifier.weight(0.8f).testTag("input_bill_due_day")
                    )
                }

                Text("Category:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            color = if (isSelected) BsbAmberGold else BsbNavySurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) BsbNavyDeep else Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val day = dueDayText.toIntOrNull() ?: 15
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title.trim(), amt, day, selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BsbAmberGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_add_bill")
            ) {
                Text("RING-FENCE & PROTECT", color = BsbNavyDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

/**
 * PRIORITY 2: Rebalance Category Envelope Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RebalanceEnvelopeDialog(
    category: CategorySpendItem,
    onDismiss: () -> Unit,
    onSaveBudget: (newBudget: Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(String.format("%.0f", category.allocatedBudget)) }
    val newBudget = budgetText.toDoubleOrNull() ?: category.allocatedBudget
    val delta = newBudget - category.allocatedBudget

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BsbNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = BsbOrangeBright)
                Text(
                    text = "Adjust Envelope Budget",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Category: ${category.categoryName}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = BsbNavySurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Budget", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("P ${String.format("%.2f", category.allocatedBudget)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Spent So Far", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("P ${String.format("%.2f", category.spentAmount)}", color = BsbOrangeBright, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Text("Quick Adjustment:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(-50, -20, 20, 50, 100).forEach { shift ->
                        Surface(
                            color = BsbNavySurface,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = budgetText.toDoubleOrNull() ?: category.allocatedBudget
                                    val updated = max(50.0, current + shift)
                                    budgetText = String.format("%.0f", updated)
                                }
                        ) {
                            Text(
                                text = if (shift > 0) "+$shift" else "$shift",
                                color = if (shift > 0) BsbEmeraldGreen else BsbCrimsonWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("New Monthly Allocation (P)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BsbOrangeBright,
                        unfocusedBorderColor = Color(0xFF2C436F),
                        focusedLabelColor = BsbOrangeBright
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (delta != 0.0) {
                    Text(
                        text = if (delta > 0) "Increases envelope allowance by P${String.format("%.2f", delta)}" else "Decreases envelope allowance by P${String.format("%.2f", -delta)}",
                        color = if (delta > 0) BsbEmeraldGreen else BsbAmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newBudget > 0) {
                        onSaveBudget(newBudget)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BsbOrangeBright),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("UPDATE ENVELOPE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

/**
 * PRIORITY 4: Transfer to BSB Sesame Smart Youth Savings Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferToSavingsDialog(
    onDismiss: () -> Unit,
    onConfirmTransfer: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BsbNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = BsbEmeraldGreen)
                Text(
                    text = "Transfer to Sesame Savings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Move surplus allowance into your interest-bearing BSB Sesame Smart Youth Savings account to build your emergency cushion.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("20", "50", "100", "200").forEach { amt ->
                        Surface(
                            color = if (amountText == amt) BsbEmeraldGreen else BsbNavySurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountText = amt }
                        ) {
                            Text(
                                text = "P$amt",
                                color = if (amountText == amt) BsbNavyDeep else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Transfer Amount in Pula (P)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BsbEmeraldGreen,
                        unfocusedBorderColor = Color(0xFF2C436F),
                        focusedLabelColor = BsbEmeraldGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_savings_amount")
                )

                Surface(
                    color = BsbEmeraldGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BsbEmeraldGreen, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Earns 3.5% p.a. compound interest backed by Botswana Savings Bank.",
                            color = BsbEmeraldGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirmTransfer(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BsbEmeraldGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_savings_transfer")
            ) {
                Text("TRANSFER TO SAVINGS", color = BsbNavyDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

/**
 * PRIORITY 4: Financial Health Scorecard Breakdown Dialog
 */
@Composable
fun HealthScorecardDialog(
    summary: FinancialSummary,
    onDismiss: () -> Unit,
    onConsultAdvisor: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BsbNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = BsbEmeraldGreen)
                Text(
                    text = "Financial Health Scorecard",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Score Header Pill
                Surface(
                    color = BsbEmeraldGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, BsbEmeraldGreen.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("HEALTH SCORE", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${summary.healthScore} / 100", color = BsbEmeraldGreen, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        Surface(
                            color = BsbEmeraldGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = summary.healthGrade.uppercase(),
                                color = BsbNavyDeep,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text("Score Factor Breakdown:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                ScoreFactorRow(
                    title = "Safe Daily Spending Pacing",
                    score = "36 / 40",
                    description = "Allowance is on track for remaining ${summary.daysRemainingInCycle} days."
                )

                ScoreFactorRow(
                    title = "Committed Bill Ring-Fencing",
                    score = "28 / 30",
                    description = "Essential rent and internet bills are ring-fenced."
                )

                ScoreFactorRow(
                    title = "Sesame Savings Cushion",
                    score = "20 / 30",
                    description = "Positive savings balance in your Sesame Youth account."
                )

                Surface(
                    color = BsbNavySurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("AI RECOMMENDATION", color = BsbAmberGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = summary.healthAdvice,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConsultAdvisor,
                colors = ButtonDefaults.buttonColors(containerColor = BsbAmberGold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("DISCUSS WITH AI ADVISOR", color = BsbNavyDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun ScoreFactorRow(title: String, score: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(score, color = BsbEmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(description, color = Color(0xFF94A3B8), fontSize = 10.sp)
    }
}

/**
 * PRIORITY 4: BSB Sesame Smart Youth Savings Card
 */
@Composable
fun SesameSmartSavingsCard(
    currentSavings: Double,
    roundUpEnabled: Boolean,
    onToggleRoundUp: () -> Unit,
    onQuickDeposit: (amount: Double) -> Unit,
    onCustomTransferClick: () -> Unit
) {
    val targetGoal = 500.0
    val progress = (currentSavings / targetGoal).toFloat().coerceIn(0f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        border = BorderStroke(1.dp, BsbEmeraldGreen.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BsbEmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = BsbEmeraldGreen, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "PRIORITY 4 • SESAME SMART SAVINGS",
                            color = BsbEmeraldGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Student Emergency Cushion",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = BsbEmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "3.5% P.A. YIELD",
                        color = BsbEmeraldGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Balance & Progress Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Sesame Savings Balance", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("P ${String.format("%.2f", currentSavings)}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target: P ${String.format("%.0f", targetGoal)}", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(progress * 100).roundToInt()}% achieved", color = BsbEmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = BsbEmeraldGreen,
                trackColor = BsbNavySurface
            )

            // Auto Round-Up Toggle
            Surface(
                color = BsbNavySurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Roundup Debit Purchases",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rounds student card purchases to nearest P5 and auto-saves the change.",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp
                        )
                    }

                    Switch(
                        checked = roundUpEnabled,
                        onCheckedChange = { onToggleRoundUp() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BsbEmeraldGreen,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = BsbNavyCard
                        ),
                        modifier = Modifier.testTag("switch_round_up_savings")
                    )
                }
            }

            // Quick Boost Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(20.0, 50.0, 100.0).forEach { boostAmt ->
                    Surface(
                        color = BsbNavySurface,
                        border = BorderStroke(1.dp, BsbEmeraldGreen.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onQuickDeposit(boostAmt) }
                    ) {
                        Text(
                            text = "+P${String.format("%.0f", boostAmt)} Boost",
                            color = BsbEmeraldGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }
                }

                Surface(
                    color = BsbEmeraldGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { onCustomTransferClick() }
                ) {
                    Text(
                        text = "Transfer Pula",
                        color = BsbNavyDeep,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
            }
        }
    }
}

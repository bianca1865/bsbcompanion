package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import kotlin.math.roundToInt

// Student360 Visual Accent Palette (BSB Navy & Vibrant Warm Highlights)
val BsbNavyDeep = Color(0xFF071228)
val BsbNavyCard = Color(0xFF0F2042)
val BsbNavySurface = Color(0xFF162B54)
val BsbOrangeBright = Color(0xFFFF6B35)
val BsbAmberGold = Color(0xFFFFB703)
val BsbCyanSafe = Color(0xFF00B4D8)
val BsbEmeraldGreen = Color(0xFF10B981)
val BsbCrimsonWarning = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Student360DashboardScreen(
    viewModel: CompanionViewModel,
    onNavigateTab: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val categories by viewModel.categorySpends.collectAsStateWithLifecycle()
    val committedBills by viewModel.committedBills.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()
    val simulatedDay by viewModel.simulatedDay.collectAsStateWithLifecycle()
    val freeDataMode by viewModel.freeDataMode.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showPaydayDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showSavingsDialog by remember { mutableStateOf(false) }
    var selectedCategoryForRebalance by remember { mutableStateOf<CategorySpendItem?>(null) }
    var showScorecardDialog by remember { mutableStateOf(false) }
    val roundUpSavingsEnabled by viewModel.roundUpSavingsEnabled.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()

    // Priorities 2 - 8 Dialog States
    var showStatementUploadDialog by remember { mutableStateOf(false) }
    var showOcrScannerDialog by remember { mutableStateOf(false) }
    var showAllocatorDialog by remember { mutableStateOf(false) }
    var showChatbotDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var showPaymentFlowDialog by remember { mutableStateOf(false) }
    var showSavingsGoalsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BsbNavyDeep)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // 1. Integrated BSB Context Bar & Student360 Header
        Student360IntegratedHeader(
            profile = profile,
            simulatedDay = simulatedDay,
            freeDataMode = freeDataMode,
            onBackToBsb = {
                Toast.makeText(context, "Returning to BSB Mobile Banking Overview...", Toast.LENGTH_SHORT).show()
            },
            onAdvanceDay = {
                viewModel.advanceSimulatedDay()
                Toast.makeText(context, "Advanced to Day ${simulatedDay + 1} • Pacing refreshed!", Toast.LENGTH_SHORT).show()
            },
            onToggleFreeData = { viewModel.toggleFreeDataMode() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Priority 1: AI Financial Overview Card
            FinancialOverviewCard(
                summary = summary,
                simulatedDay = simulatedDay,
                onLogExpenseClick = { showExpenseDialog = true },
                onSimulatePaydayClick = { showPaydayDialog = true }
            )

            // PRIORITIES 2 TO 8 CORE HUB STRIP
            Student360PrioritiesStrip(
                onSelectPriority = { priorityNum ->
                    when (priorityNum) {
                        2 -> showStatementUploadDialog = true
                        3 -> showOcrScannerDialog = true
                        4 -> showAllocatorDialog = true
                        5 -> showChatbotDialog = true
                        6 -> showRecurringDialog = true
                        7 -> showPaymentFlowDialog = true
                        8 -> showSavingsGoalsDialog = true
                    }
                }
            )

            // Priority 2: Safe-To-Spend Daily Pacing Meter & What-If Simulator
            SafeToSpendPacingBanner(summary = summary)

            WhatIfExpenseCalculatorCard(
                summary = summary,
                onLogWhatIfExpense = { title, amt ->
                    viewModel.logStudentExpense(title, amt, "Personal & Social")
                    Toast.makeText(context, "Logged $title (-P${String.format("%.2f", amt)})", Toast.LENGTH_SHORT).show()
                }
            )

            // Priority 2: Allowance Envelopes & Category Breakdown
            CategoryBreakdownSection(
                categories = categories,
                onAddExpenseClick = { showExpenseDialog = true },
                onSelectCategory = { cat -> selectedCategoryForRebalance = cat }
            )

            // Priority 3: Committed Obligations & Ring-Fenced Bills
            CommittedObligationsCard(
                bills = committedBills,
                simulatedDay = simulatedDay,
                onAddBillClick = { showAddBillDialog = true },
                onToggleRingFence = { billId, isRingFenced ->
                    viewModel.toggleBillRingFence(billId, isRingFenced)
                },
                onSettleBill = { billId ->
                    viewModel.settleBillNow(billId)
                    Toast.makeText(context, "Obligation settled from protected funds!", Toast.LENGTH_SHORT).show()
                }
            )

            // Priority 4: Personalised AI Financial Guidance & Health Scorecard
            AiFinancialInsightsSection(
                summary = summary,
                insights = aiInsights,
                onHealthScoreClick = { showScorecardDialog = true },
                onActionClick = { insight ->
                    when (insight.actionText) {
                        "Transfer to Savings", "Boost Savings" -> {
                            showSavingsDialog = true
                        }
                        "Review Outflows", "Log Expense" -> {
                            showExpenseDialog = true
                        }
                        "Transport Budget", "Adjust Budget" -> {
                            val targetCat = categories.find { it.categoryName.contains("Transport", true) } ?: categories.firstOrNull()
                            selectedCategoryForRebalance = targetCat
                        }
                        else -> {
                            onNavigateTab?.invoke("ADVISOR")
                        }
                    }
                }
            )

            // Priority 4: BSB Sesame Smart Youth Savings Hub
            SesameSmartSavingsCard(
                currentSavings = summary.totalSavings,
                roundUpEnabled = roundUpSavingsEnabled,
                onToggleRoundUp = { viewModel.toggleRoundUpSavings() },
                onQuickDeposit = { amt ->
                    viewModel.transferToSesameSavings(amt)
                    Toast.makeText(context, "Deposited P${String.format("%.2f", amt)} into Sesame Savings!", Toast.LENGTH_SHORT).show()
                },
                onCustomTransferClick = { showSavingsDialog = true }
            )

            // Priority 5 / Outflow Tracking: Recent Student Outflows Log
            RecentStudentOutflowsSection(
                expenses = expenses.take(6),
                onLogMore = { showExpenseDialog = true },
                onDeleteExpense = { exp ->
                    viewModel.deleteExpenseItem(exp)
                    Toast.makeText(context, "Removed expense: ${exp.title}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Modal 1: Quick Log Student Expense Dialog
    if (showExpenseDialog) {
        QuickLogExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onConfirm = { title, amount, category ->
                viewModel.logStudentExpense(title, amount, category)
                showExpenseDialog = false
                Toast.makeText(context, "Recorded: $title (P${String.format("%.2f", amount)})", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal 2: Simulate Allowance Payday
    if (showPaydayDialog) {
        AlertDialog(
            onDismissRequest = { showPaydayDialog = false },
            containerColor = BsbNavyCard,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = BsbOrangeBright)
                    Text("Simulate DTEF Payday", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Simulate the arrival of your monthly tertiary allowance (P2,200.00) from DTEF into your BSB Student Account.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Text(
                        "This replenishes your available balance, resets month burn calculations, and unlocks ring-fenced bill execution.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.simulatePaydayDeposit(2200.0)
                        showPaydayDialog = false
                        Toast.makeText(context, "DTEF P2,200.00 allowance successfully credited!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BsbOrangeBright),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CREDIT P2,200 NOW", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaydayDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Modal 3: Add Committed Recurring Bill (Priority 3)
    if (showAddBillDialog) {
        AddCommittedBillDialog(
            onDismiss = { showAddBillDialog = false },
            onConfirm = { title, amount, dueDay, category ->
                viewModel.addCommittedBill(title, amount, dueDay, category)
                showAddBillDialog = false
                Toast.makeText(context, "Obligation $title registered & ring-fenced!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal 4: Transfer to Sesame Savings (Priority 4)
    if (showSavingsDialog) {
        TransferToSavingsDialog(
            onDismiss = { showSavingsDialog = false },
            onConfirmTransfer = { amount ->
                viewModel.transferToSesameSavings(amount)
                showSavingsDialog = false
                Toast.makeText(context, "Transferred P${String.format("%.2f", amount)} into Sesame Youth Savings!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal 5: Rebalance Envelope Budget (Priority 2)
    selectedCategoryForRebalance?.let { cat ->
        RebalanceEnvelopeDialog(
            category = cat,
            onDismiss = { selectedCategoryForRebalance = null },
            onSaveBudget = { newBudget ->
                viewModel.rebalanceCategoryBudget(cat.categoryName, newBudget)
                selectedCategoryForRebalance = null
                Toast.makeText(context, "${cat.categoryName} budget updated to P${String.format("%.2f", newBudget)}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal 6: Health Scorecard Breakdown (Priority 4)
    if (showScorecardDialog) {
        HealthScorecardDialog(
            summary = summary,
            onDismiss = { showScorecardDialog = false },
            onConsultAdvisor = {
                showScorecardDialog = false
                onNavigateTab?.invoke("ADVISOR")
            }
        )
    }

    // PRIORITY 2: Bank Statement / E-Receipt Upload
    if (showStatementUploadDialog) {
        BankStatementUploadDialog(
            onDismiss = { showStatementUploadDialog = false },
            onImportTransactions = { list ->
                viewModel.batchImportStatementTransactions(list)
                Toast.makeText(context, "Imported ${list.filter { it.isSelected }.size} statement transactions!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // PRIORITY 3: Physical Receipt Camera Scanner + OCR
    if (showOcrScannerDialog) {
        PhysicalReceiptCameraScannerDialog(
            onDismiss = { showOcrScannerDialog = false },
            onReceiptScanned = { receipt ->
                viewModel.importOcrReceipt(receipt)
                Toast.makeText(context, "Saved OCR receipt: ${receipt.merchant} (P${String.format("%.2f", receipt.totalAmount)})", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // PRIORITY 4: P2,200 AI Allowance Allocator
    if (showAllocatorDialog) {
        P2200AllowanceAllocatorDialog(
            onDismiss = { showAllocatorDialog = false },
            onApplyAllocation = { preset ->
                viewModel.applyFullAllowanceAllocation(preset)
                Toast.makeText(context, "Locked P2,200 '${preset.name}' allowance allocation!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // PRIORITY 5: Student360 AI Chatbot
    if (showChatbotDialog) {
        Student360ChatbotDialog(
            summary = summary,
            onDismiss = { showChatbotDialog = false },
            onNavigateFullAdvisor = {
                showChatbotDialog = false
                onNavigateTab?.invoke("ADVISOR")
            }
        )
    }

    // PRIORITY 6: Recurring Expenses + Local Notifications
    if (showRecurringDialog) {
        RecurringExpensesNotificationsDialog(
            committedBills = committedBills,
            onDismiss = { showRecurringDialog = false },
            onAddRecurringBill = { title, amount, dueDay, category ->
                viewModel.addCommittedBill(title, amount, dueDay, category)
            },
            onToggleProtection = { billId, isRingFenced ->
                viewModel.toggleBillRingFence(billId, isRingFenced)
            },
            onSimulateNotification = { title, msg ->
                viewModel.simulateNotificationAlert(title, msg)
                Toast.makeText(context, "Triggered Alert: $title", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // PRIORITY 7: BSB Payment Flow Integration
    if (showPaymentFlowDialog) {
        BsbPaymentFlowDialog(
            onDismiss = { showPaymentFlowDialog = false },
            onExecutePayment = { payee, amount, ref, category, token, callback ->
                viewModel.executeBsbPayment(payee, amount, ref, category, token) { receipt ->
                    callback(receipt)
                }
            }
        )
    }

    // PRIORITY 8: Savings Goals + Monthly Reports
    if (showSavingsGoalsDialog) {
        SavingsGoalsAndReportsDialog(
            goals = savingsGoals,
            summary = summary,
            onDismiss = { showSavingsGoalsDialog = false },
            onDepositToGoal = { goalId, amount ->
                viewModel.depositToSavingsGoal(goalId, amount)
            },
            onAddGoal = { title, target, emoji, month ->
                viewModel.addSavingsGoal(title, target, emoji, month)
            }
        )
    }
}

/**
 * Integrated BSB Top Context Bar:
 * Clearly communicates that Student360 is accessed directly from within the BSB Mobile Banking app.
 */
@Composable
fun Student360IntegratedHeader(
    profile: StudentProfile,
    simulatedDay: Int,
    freeDataMode: Boolean,
    onBackToBsb: () -> Unit,
    onAdvanceDay: () -> Unit,
    onToggleFreeData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BsbNavyCard, BsbNavyDeep)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Parent BSB Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onBackToBsb() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to BSB",
                        tint = BsbOrangeBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "BSB Banking",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Zero-rated free data badge
            Surface(
                color = if (freeDataMode) BsbEmeraldGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, if (freeDataMode) BsbEmeraldGreen else Color.Gray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onToggleFreeData() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (freeDataMode) BsbEmeraldGreen else Color.Gray)
                    )
                    Text(
                        text = if (freeDataMode) "Zero-Rated Free Data" else "Standard Data",
                        color = if (freeDataMode) BsbEmeraldGreen else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student360 Hero Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Student360",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Surface(
                        color = BsbOrangeBright,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "PORTAL",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Your money. Your habits. Your future.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Day Simulator Trigger
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clickable { onAdvanceDay() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Simulate Day",
                        tint = BsbAmberGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Day $simulatedDay of 30",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+1 Day ⏩",
                            color = BsbAmberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Authenticated Student Identity Chip
        Surface(
            color = BsbNavySurface.copy(alpha = 0.6f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(BsbOrangeBright.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BsbOrangeBright,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${profile.fullName} • ${profile.institution}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${profile.sponsorName} • BSB Account •••• ${profile.primaryAccountNumber.takeLast(4)}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
                Surface(
                    color = BsbEmeraldGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "AUTHENTICATED",
                        color = BsbEmeraldGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Priority 1 Centerpiece:
 * AI Financial Analysis Overview Card
 * Displays Allowance, Spent, Committed (Ring-fenced), Remaining, and Savings.
 */
@Composable
fun FinancialOverviewCard(
    summary: FinancialSummary,
    simulatedDay: Int,
    onLogExpenseClick: () -> Unit,
    onSimulatePaydayClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(BsbOrangeBright.copy(alpha = 0.5f), Color.Transparent))),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student360_financial_overview_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row of Card
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BsbOrangeBright.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = BsbOrangeBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "MONTHLY FINANCIAL OVERVIEW",
                            color = BsbOrangeBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Allowance & Cashflow Matrix",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Payday Simulation Pill
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onSimulatePaydayClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BsbAmberGold, modifier = Modifier.size(12.dp))
                        Text("Payday", color = BsbAmberGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Big Hero Numbers: Remaining Free-to-Spend
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BsbNavySurface.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "REMAINING SAFE-TO-SPEND",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "P ${String.format("%,.2f", summary.remainingFreeToSpend)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "free to spend",
                        color = BsbCyanSafe,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = "Calculated after protecting P${String.format("%.0f", summary.totalCommitted)} for upcoming bills & P${String.format("%.0f", summary.totalSavings)} savings reserve.",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            // 4-Pillar Financial Breakdown Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Allowance
                PillarStatBox(
                    label = "Allowance",
                    amount = "P ${String.format("%.0f", summary.monthlyAllowance)}",
                    subtext = "DTEF Monthly",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Spent
                PillarStatBox(
                    label = "Spent So Far",
                    amount = "P ${String.format("%.0f", summary.totalSpent)}",
                    subtext = "${((summary.totalSpent / summary.monthlyAllowance) * 100).roundToInt()}% used",
                    color = BsbOrangeBright,
                    modifier = Modifier.weight(1f)
                )

                // Committed (Ring-Fenced)
                PillarStatBox(
                    label = "Committed",
                    amount = "P ${String.format("%.0f", summary.totalCommitted)}",
                    subtext = "🔒 Ring-fenced",
                    color = BsbAmberGold,
                    modifier = Modifier.weight(1f)
                )

                // Savings Pot
                PillarStatBox(
                    label = "BSB Savings",
                    amount = "P ${String.format("%.0f", summary.totalSavings)}",
                    subtext = "Sesame Youth",
                    color = BsbEmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Multi-Segment Visual Allocation Progress Bar
            MultiSegmentAllowanceBar(
                allowance = summary.monthlyAllowance,
                spent = summary.totalSpent,
                committed = summary.totalCommitted,
                savings = summary.totalSavings,
                remaining = summary.remainingFreeToSpend
            )

            // Primary Action Button: Log Expense
            Button(
                onClick = { onLogExpenseClick() },
                colors = ButtonDefaults.buttonColors(containerColor = BsbOrangeBright),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_log_student_expense")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECORD STUDENT EXPENSE",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PillarStatBox(
    label: String,
    amount: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BsbNavySurface.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtext,
                color = Color(0xFF64748B),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/**
 * Visual Multi-Segment Bar showing distribution of the P2,200 Allowance
 */
@Composable
fun MultiSegmentAllowanceBar(
    allowance: Double,
    spent: Double,
    committed: Double,
    savings: Double,
    remaining: Double
) {
    val total = allowance.coerceAtLeast(1.0)
    val spentFraction = (spent / total).toFloat().coerceIn(0f, 1f)
    val committedFraction = (committed / total).toFloat().coerceIn(0f, 1f)
    val savingsFraction = (savings / total).toFloat().coerceIn(0f, 1f)
    val remainingFraction = (remaining / total).toFloat().coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Allowance Utilization Breakdown", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("${((spent / total) * 100).roundToInt()}% consumed", color = BsbOrangeBright, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        // Custom drawn multi-segment bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            val w = size.width
            val h = size.height

            var currentX = 0f

            // Spent segment
            val spentWidth = w * spentFraction
            drawRect(color = BsbOrangeBright, topLeft = Offset(currentX, 0f), size = Size(spentWidth, h))
            currentX += spentWidth

            // Committed segment
            val committedWidth = w * committedFraction
            drawRect(color = BsbAmberGold, topLeft = Offset(currentX, 0f), size = Size(committedWidth, h))
            currentX += committedWidth

            // Savings segment
            val savingsWidth = w * savingsFraction
            drawRect(color = BsbEmeraldGreen, topLeft = Offset(currentX, 0f), size = Size(savingsWidth, h))
            currentX += savingsWidth

            // Remaining segment
            val remainingWidth = (w - currentX).coerceAtLeast(0f)
            drawRect(color = BsbCyanSafe, topLeft = Offset(currentX, 0f), size = Size(remainingWidth, h))
        }

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(color = BsbOrangeBright, label = "Spent")
            LegendItem(color = BsbAmberGold, label = "Ring-Fenced")
            LegendItem(color = BsbEmeraldGreen, label = "Savings")
            LegendItem(color = BsbCyanSafe, label = "Safe-to-Spend")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.sp)
    }
}

/**
 * Safe-To-Spend Daily Pacing Banner
 * Answers the student's most critical daily question:
 * "How much can I spend today without running out of money before the 25th?"
 */
@Composable
fun SafeToSpendPacingBanner(summary: FinancialSummary) {
    val statusColor = when (summary.pacingStatus) {
        PacingStatus.AHEAD_OF_PACE -> BsbEmeraldGreen
        PacingStatus.ON_PACE -> BsbCyanSafe
        PacingStatus.CAUTION -> BsbAmberGold
        PacingStatus.OVERSPENT -> BsbCrimsonWarning
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Speedometer Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SAFE DAILY PACING",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = summary.pacingStatus.label.uppercase(),
                            color = statusColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "P ${String.format("%.2f", summary.safeDailySpend)}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = " / day for next ${summary.daysRemainingInCycle} days",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                    )
                }
                Text(
                    text = summary.pacingStatus.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * AI Financial Insights & Health Score Section (Priority 4)
 */
@Composable
fun AiFinancialInsightsSection(
    summary: FinancialSummary,
    insights: List<FinancialInsight>,
    onHealthScoreClick: () -> Unit,
    onActionClick: (FinancialInsight) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BsbAmberGold,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "PRIORITY 4 • FINANCIAL INTELLIGENCE",
                        color = BsbAmberGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "AI Advice & Health Score",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Health Score Badge (clickable to open full scorecard modal)
            Surface(
                color = BsbEmeraldGreen.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, BsbEmeraldGreen.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clickable { onHealthScoreClick() }
                    .testTag("badge_health_score")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Score: ${summary.healthScore}/100 ℹ️",
                        color = BsbEmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // AI Insight Cards List
        insights.forEach { insight ->
            AiInsightCard(
                insight = insight,
                onActionClick = onActionClick
            )
        }
    }
}

@Composable
fun AiInsightCard(
    insight: FinancialInsight,
    onActionClick: (FinancialInsight) -> Unit
) {
    val (borderColor, accentColor, icon) = when (insight.type) {
        InsightType.POSITIVE -> Triple(BsbEmeraldGreen, BsbEmeraldGreen, Icons.Default.CheckCircle)
        InsightType.WARNING -> Triple(BsbAmberGold, BsbAmberGold, Icons.Default.Warning)
        InsightType.RING_FENCE -> Triple(BsbCyanSafe, BsbCyanSafe, Icons.Default.Lock)
        InsightType.SAVINGS -> Triple(BsbEmeraldGreen, BsbEmeraldGreen, Icons.Default.Star)
        InsightType.TIP -> Triple(BsbOrangeBright, BsbOrangeBright, Icons.Default.Info)
    }

    Surface(
        color = BsbNavyCard,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = insight.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = insight.tag,
                            color = accentColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.message,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Interactive Action Button
                insight.actionText?.let { action ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = accentColor.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onActionClick(insight) }
                    ) {
                        Text(
                            text = "$action ➔",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Category Breakdown & Student Envelopes (Priority 2)
 */
@Composable
fun CategoryBreakdownSection(
    categories: List<CategorySpendItem>,
    onAddExpenseClick: () -> Unit,
    onSelectCategory: (CategorySpendItem) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRIORITY 2 • ALLOWANCE ENVELOPES",
                        color = BsbOrangeBright,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Category Budget Envelopes",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onAddExpenseClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("+ Quick Log", color = BsbOrangeBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Tap any envelope below to rebalance monthly allocations or adjust limits.",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )

            categories.forEach { cat ->
                CategoryProgressRow(
                    item = cat,
                    onClick = { onSelectCategory(cat) }
                )
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    item: CategorySpendItem,
    onClick: () -> Unit
) {
    val fraction = (item.spentAmount / item.allocatedBudget.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val isOverbudget = item.spentAmount > item.allocatedBudget
    val barColor = if (isOverbudget) BsbCrimsonWarning else if (fraction > 0.8f) BsbAmberGold else BsbCyanSafe

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = item.categoryName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "⚙️ Rebalance",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }
            Text(
                text = "P ${String.format("%.0f", item.spentAmount)} / P ${String.format("%.0f", item.allocatedBudget)}",
                color = if (isOverbudget) BsbCrimsonWarning else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Progress line
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = BsbNavySurface
        )
    }
}

/**
 * Committed Obligations & Ring-Fenced Bills Card (Priority 3)
 */
@Composable
fun CommittedObligationsCard(
    bills: List<CommittedBillItem>,
    simulatedDay: Int,
    onAddBillClick: () -> Unit,
    onToggleRingFence: (billId: Int, isCurrentlyRingFenced: Boolean) -> Unit,
    onSettleBill: (billId: Int) -> Unit
) {
    val totalProtected = bills.filter { it.isRingFenced }.sumOf { it.amount }

    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        border = BorderStroke(1.dp, BsbAmberGold.copy(alpha = 0.35f)),
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
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BsbAmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "PRIORITY 3 • RING-FENCED OBLIGATIONS",
                            color = BsbAmberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Committed Recurring Bills",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(
                    onClick = onAddBillClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("btn_add_bill_card")
                ) {
                    Text("+ Add Bill", color = BsbAmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Summary protection pill
            Surface(
                color = BsbAmberGold.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, BsbAmberGold.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔒 P ${String.format("%.2f", totalProtected)} Ring-Fenced in BSB",
                        color = BsbAmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${bills.count { it.isRingFenced }} Protected",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Protected bills cannot be accidentally spent on debit card swipes. Tap the shield to toggle ring-fencing or pay bills directly.",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )

            bills.forEach { bill ->
                Surface(
                    color = BsbNavySurface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, if (bill.isRingFenced) BsbAmberGold.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (bill.isRingFenced) BsbAmberGold.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (bill.isRingFenced) Icons.Default.Lock else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (bill.isRingFenced) BsbAmberGold else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = bill.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Due Day ${bill.dueDay} (in ${bill.daysUntilDue} days) • ${bill.category}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "P ${String.format("%.2f", bill.amount)}",
                                    color = if (bill.isRingFenced) BsbAmberGold else Color(0xFFCBD5E1),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (bill.isRingFenced) "Ring-Fenced" else "Spendable",
                                    color = if (bill.isRingFenced) BsbEmeraldGreen else Color(0xFF94A3B8),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Interactive Row: Ring-fence toggle button & Settle Now button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (bill.isRingFenced) BsbAmberGold.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, if (bill.isRingFenced) BsbAmberGold else Color.Gray),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onToggleRingFence(bill.id, bill.isRingFenced) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (bill.isRingFenced) Icons.Default.Lock else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (bill.isRingFenced) BsbAmberGold else Color.LightGray,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (bill.isRingFenced) "Protection ON" else "Protection OFF",
                                        color = if (bill.isRingFenced) BsbAmberGold else Color.LightGray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = BsbEmeraldGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BsbEmeraldGreen.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { onSettleBill(bill.id) }
                            ) {
                                Text(
                                    text = "Pay Early ➔",
                                    color = BsbEmeraldGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recent Outflows Section (Outflow Tracking)
 */
@Composable
fun RecentStudentOutflowsSection(
    expenses: List<ExpenseItem>,
    onLogMore: () -> Unit,
    onDeleteExpense: (ExpenseItem) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Student Outflows",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+ Record",
                    color = BsbOrangeBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLogMore() }
                )
            }

            if (expenses.isEmpty()) {
                Text(
                    text = "No recent expenses logged. Tap '+ Record' above.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            } else {
                expenses.forEach { exp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { onDeleteExpense(exp) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Expense",
                                    tint = Color.Gray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = exp.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = exp.category,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = "- P ${String.format("%.2f", exp.amount)}",
                            color = BsbOrangeBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal Dialog: Quick Log Student Expense
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Meals") }

    val categories = listOf("Food & Meals", "Transport & Kombi", "Study Materials", "Data & Wifi", "Personal & Social")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BsbNavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Record Student Expense",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick preset chips for student life in Gaborone/Botswana
                Text("Quick Presets:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickChip("Kombi Fare P7.50") {
                        title = "Kombi (Campus to Mall)"
                        amountText = "7.50"
                        selectedCategory = "Transport & Kombi"
                    }
                    QuickChip("Cafeteria Lunch P35") {
                        title = "Campus Cafeteria Meal"
                        amountText = "35.00"
                        selectedCategory = "Food & Meals"
                    }
                    QuickChip("Orange Data P20") {
                        title = "1GB Student Data"
                        amountText = "20.00"
                        selectedCategory = "Data & Wifi"
                    }
                    QuickChip("Photocopying P10") {
                        title = "Lecture Notes Printing"
                        amountText = "10.00"
                        selectedCategory = "Study Materials"
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title / Merchant") },
                    placeholder = { Text("e.g. Choppies Campus") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BsbOrangeBright,
                        unfocusedBorderColor = Color(0xFF2C436F),
                        focusedLabelColor = BsbOrangeBright
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_title")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount in Pula (P)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BsbOrangeBright,
                        unfocusedBorderColor = Color(0xFF2C436F),
                        focusedLabelColor = BsbOrangeBright
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_amount")
                )

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
                            color = if (isSelected) BsbOrangeBright else BsbNavySurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
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
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title.trim(), amt, selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BsbOrangeBright),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_log_expense")
            ) {
                Text("SAVE EXPENSE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun QuickChip(text: String, onClick: () -> Unit) {
    Surface(
        color = BsbNavySurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

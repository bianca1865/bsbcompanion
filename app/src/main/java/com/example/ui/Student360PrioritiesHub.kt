package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.viewmodel.CompanionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Visual Quick-Launch Priorities Strip on Student360 Dashboard
 */
@Composable
fun Student360PrioritiesStrip(
    onSelectPriority: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BsbNavyCard),
        border = BorderStroke(1.dp, BsbAmberGold.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student360_priorities_strip")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(BsbAmberGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚡", fontSize = 13.sp)
                    }
                    Column {
                        Text(
                            text = "STUDENT360 CORE PRIORITIES",
                            color = BsbAmberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Financial Tools & Integrations",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    color = BsbOrangeBright.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "7 MODULES",
                        color = BsbOrangeBright,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Tap any priority below to launch statement imports, camera OCR, allowance allocation, AI bot, or BSB payments.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Horizontal scrolling priority chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriorityQuickChip(priorityNum = 2, title = "Statement Upload", icon = "📄", color = BsbCyanSafe) { onSelectPriority(2) }
                PriorityQuickChip(priorityNum = 3, title = "Receipt Scanner", icon = "📷", color = BsbOrangeBright) { onSelectPriority(3) }
                PriorityQuickChip(priorityNum = 4, title = "P2,200 Allocator", icon = "⚖️", color = BsbAmberGold) { onSelectPriority(4) }
                PriorityQuickChip(priorityNum = 5, title = "AI Chatbot", icon = "🤖", color = BsbEmeraldGreen) { onSelectPriority(5) }
                PriorityQuickChip(priorityNum = 6, title = "Recurring & Alerts", icon = "🔔", color = Color(0xFF818CF8)) { onSelectPriority(6) }
                PriorityQuickChip(priorityNum = 7, title = "BSB Payment Flow", icon = "💳", color = Color(0xFFF43F5E)) { onSelectPriority(7) }
                PriorityQuickChip(priorityNum = 8, title = "Goals & Reports", icon = "📊", color = Color(0xFF34D399)) { onSelectPriority(8) }
            }
        }
    }
}

@Composable
fun PriorityQuickChip(
    priorityNum: Int,
    title: String,
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = BsbNavySurface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("chip_priority_$priorityNum")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Column {
                Text(
                    text = "PRIORITY $priorityNum",
                    color = color,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* ========================================================================= */
/* PRIORITY 2: Bank Statement / E-Receipt Upload Dialog                      */
/* ========================================================================= */

@Composable
fun BankStatementUploadDialog(
    onDismiss: () -> Unit,
    onImportTransactions: (List<StatementTransaction>) -> Unit
) {
    val context = LocalContext.current
    var uploadMode by remember { mutableStateOf("statement") } // "statement" or "sms_receipt"
    var isProcessingFile by remember { mutableStateOf(false) }

    // Sample statement transactions pre-parsed
    var transactions by remember {
        mutableStateOf(
            listOf(
                StatementTransaction("t1", "01/09", "DTEF Tertiary Allowance Deposit", 2200.0, true, "Allowance", false),
                StatementTransaction("t2", "02/09", "Choppies Railpark Groceries", 185.50, false, "Food & Meals", true),
                StatementTransaction("t3", "03/09", "Mascom Campus 20GB Bundle", 99.00, false, "Data & Wifi", true),
                StatementTransaction("t4", "04/09", "UB Main Bookstore - Stationeries", 145.00, false, "Study Materials", true),
                StatementTransaction("t5", "05/09", "Shell Gabs Kombi Fuel Share", 40.00, false, "Transport & Kombi", true),
                StatementTransaction("t6", "06/09", "Campus Refectory Lunch Pack", 35.00, false, "Food & Meals", true)
            )
        )
    }

    var smsReceiptText by remember {
        mutableStateOf("BSB Alert: Debit P78.50 at Spar Riverwalk Mall on 05/09/2026. Avail Bal: P1,820.00.")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp)),
            color = BsbNavyCard,
            border = BorderStroke(1.5.dp, BsbCyanSafe.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "📄", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "PRIORITY 2 • BSB STATEMENT & E-RECEIPT",
                                color = BsbCyanSafe,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Bank Statement Upload",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Sub-tabs: Bank Statement vs SMS E-Receipt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BsbNavySurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uploadMode == "statement") BsbCyanSafe else Color.Transparent)
                            .clickable { uploadMode = "statement" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PDF / CSV Statement",
                            color = if (uploadMode == "statement") BsbNavyDeep else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uploadMode == "sms_receipt") BsbCyanSafe else Color.Transparent)
                            .clickable { uploadMode = "sms_receipt" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SMS E-Receipt Paste",
                            color = if (uploadMode == "sms_receipt") BsbNavyDeep else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uploadMode == "statement") {
                    // Statement File Upload Area
                    Surface(
                        color = BsbNavySurface.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, BsbCyanSafe.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isProcessingFile = true
                                Toast.makeText(context, "Parsing BSB_Student_Statement_Sept2026.pdf...", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = BsbCyanSafe, modifier = Modifier.size(24.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BSB_Account_Statement_Sept2026.pdf",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Detected: 6 Transactions • DTEF Student Account",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                            Surface(color = BsbCyanSafe.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "RE-SCAN",
                                    color = BsbCyanSafe,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Select transactions to import directly into your student expense envelopes:",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    // Transactions Table
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(transactions) { tx ->
                            Surface(
                                color = BsbNavySurface.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, if (tx.isSelected) BsbCyanSafe.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        transactions = transactions.map {
                                            if (it.id == tx.id) it.copy(isSelected = !it.isSelected) else it
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = tx.isSelected,
                                            onCheckedChange = { checked ->
                                                transactions = transactions.map {
                                                    if (it.id == tx.id) it.copy(isSelected = checked) else it
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = BsbCyanSafe,
                                                checkmarkColor = BsbNavyDeep
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = tx.description,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${tx.date} • ${tx.category}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (tx.isCredit) "+ P ${String.format("%.2f", tx.amount)}" else "- P ${String.format("%.2f", tx.amount)}",
                                        color = if (tx.isCredit) BsbEmeraldGreen else BsbOrangeBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SMS E-Receipt Paste Section
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Paste BSB debit SMS alert or Mascom/Orange Money receipt text:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        OutlinedTextField(
                            value = smsReceiptText,
                            onValueChange = { smsReceiptText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BsbCyanSafe,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = BsbNavySurface,
                                unfocusedContainerColor = BsbNavySurface
                            )
                        )

                        Surface(
                            color = BsbCyanSafe.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, BsbCyanSafe.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "PARSED E-RECEIPT PREVIEW", color = BsbCyanSafe, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Merchant: Spar Riverwalk Mall", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Amount: P 78.50  •  Category: Food & Meals", color = BsbAmberGold, fontSize = 11.sp)
                                Text(text = "Date: 05 Sept 2026  •  Method: BSB Student Debit Card", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(text = "Cancel", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (uploadMode == "statement") {
                                onImportTransactions(transactions)
                            } else {
                                val parsed = listOf(
                                    StatementTransaction("sms1", "05/09", "Spar Riverwalk Mall (SMS)", 78.50, false, "Food & Meals", true)
                                )
                                onImportTransactions(parsed)
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_import_statement"),
                        colors = ButtonDefaults.buttonColors(containerColor = BsbCyanSafe)
                    ) {
                        Text(
                            text = if (uploadMode == "statement") "Import Selected" else "Import E-Receipt",
                            color = BsbNavyDeep,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* PRIORITY 3: Physical Receipt Camera Scanner + OCR Dialog                 */
/* ========================================================================= */

@Composable
fun PhysicalReceiptCameraScannerDialog(
    onDismiss: () -> Unit,
    onReceiptScanned: (ScannedOcrReceipt) -> Unit
) {
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }

    // Mock OCR result
    var detectedMerchant by remember { mutableStateOf("Choppies Supermarket - Railpark Mall") }
    var detectedAmount by remember { mutableStateOf("142.80") }
    var detectedVat by remember { mutableStateOf("17.50") }
    var detectedCategory by remember { mutableStateOf("Food & Meals") }
    val detectedItems = listOf("Bread (Albany 700g) - P14.95", "Milk (Clover 2L) - P28.50", "Eggs (18pk) - P36.00", "Apples (1.5kg) - P24.50", "Potatoes (2kg) - P38.85")

    // Scanning laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    val coroutineScope = rememberCoroutineScope()

    fun triggerScan() {
        isScanning = true
        scanCompleted = false
        coroutineScope.launch {
            delay(1600)
            isScanning = false
            scanCompleted = true
            Toast.makeText(context, "OCR Complete: P142.80 Choppies receipt captured!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = BsbNavyCard,
            border = BorderStroke(1.5.dp, BsbOrangeBright.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "📷", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "PRIORITY 3 • PHYSICAL RECEIPT OCR",
                                color = BsbOrangeBright,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Camera Scanner & OCR",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Camera Viewfinder Screen Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0A1424))
                        .border(1.dp, BsbOrangeBright.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Viewfinder corner brackets & laser line
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val bracketLen = 30.dp.toPx()
                        val color = Color(0xFFFF6B35)

                        // Top-left
                        drawLine(color, Offset(20f, 20f), Offset(20f + bracketLen, 20f), strokeWidth = 5f)
                        drawLine(color, Offset(20f, 20f), Offset(20f, 20f + bracketLen), strokeWidth = 5f)

                        // Top-right
                        drawLine(color, Offset(w - 20f, 20f), Offset(w - 20f - bracketLen, 20f), strokeWidth = 5f)
                        drawLine(color, Offset(w - 20f, 20f), Offset(w - 20f, 20f + bracketLen), strokeWidth = 5f)

                        // Bottom-left
                        drawLine(color, Offset(20f, h - 20f), Offset(20f + bracketLen, h - 20f), strokeWidth = 5f)
                        drawLine(color, Offset(20f, h - 20f), Offset(20f, h - 20f - bracketLen), strokeWidth = 5f)

                        // Bottom-right
                        drawLine(color, Offset(w - 20f, h - 20f), Offset(w - 20f - bracketLen, h - 20f), strokeWidth = 5f)
                        drawLine(color, Offset(w - 20f, h - 20f), Offset(w - 20f, h - 20f - bracketLen), strokeWidth = 5f)

                        // Laser line if scanning
                        val laserY = h * laserOffsetY
                        drawLine(
                            brush = Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFFF6B35), Color.Transparent)),
                            start = Offset(20f, laserY),
                            end = Offset(w - 20f, laserY),
                            strokeWidth = 6f
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(color = BsbOrangeBright, modifier = Modifier.size(32.dp))
                            Text(text = "Scanning & Optical Character Recognition...", color = Color.White, fontSize = 11.sp)
                        } else if (!scanCompleted) {
                            Text(text = "🧾", fontSize = 36.sp)
                            Text(text = "Align physical student receipt within frame", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Text(text = "Auto-detects merchant, total BWP & line items", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        } else {
                            Surface(color = BsbEmeraldGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BsbEmeraldGreen, modifier = Modifier.size(16.dp))
                                    Text(text = "OCR Verified • 96% Match", color = BsbEmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Flash toggle button
                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.Star else Icons.Default.Refresh,
                            contentDescription = "Flash",
                            tint = if (flashEnabled) BsbAmberGold else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Capture / Retake Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { triggerScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = BsbOrangeBright),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_capture_receipt_ocr")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = BsbNavyDeep, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (scanCompleted) "Rescan Receipt" else "Capture & Run OCR", color = BsbNavyDeep, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                // Extracted OCR Data Preview
                if (scanCompleted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BsbNavySurface),
                        border = BorderStroke(1.dp, BsbEmeraldGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "EXTRACTED RECEIPT DETAILS", color = BsbEmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                Surface(color = BsbEmeraldGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                    Text(text = "VAT INCLUDED", color = BsbEmeraldGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }

                            Text(text = detectedMerchant, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Amount:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                Text(text = "BWP $detectedAmount", color = BsbAmberGold, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "VAT (14%): P $detectedVat", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(text = "Category: $detectedCategory", color = BsbCyanSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                            Text(text = "Detected Line Items (${detectedItems.size}):", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            detectedItems.forEach { item ->
                                Text(text = "• $item", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Final Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(text = "Cancel", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val receipt = ScannedOcrReceipt(
                                id = "ocr_${System.currentTimeMillis()}",
                                merchant = detectedMerchant,
                                date = "Today",
                                totalAmount = detectedAmount.toDoubleOrNull() ?: 142.80,
                                vatAmount = detectedVat.toDoubleOrNull() ?: 17.50,
                                items = detectedItems,
                                category = detectedCategory
                            )
                            onReceiptScanned(receipt)
                            onDismiss()
                        },
                        enabled = scanCompleted,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_ocr_receipt"),
                        colors = ButtonDefaults.buttonColors(containerColor = BsbEmeraldGreen)
                    ) {
                        Text(text = "Save to Expenses", color = BsbNavyDeep, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* PRIORITY 4: P2,200 AI Allowance Allocator Dialog                         */
/* ========================================================================= */

@Composable
fun P2200AllowanceAllocatorDialog(
    onDismiss: () -> Unit,
    onApplyAllocation: (AllowanceAllocationPreset) -> Unit
) {
    val presets = listOf(
        AllowanceAllocationPreset("Smart Balanced", 700.0, 750.0, 300.0, 250.0, 200.0, "Optimized for standard DTEF student life with steady kombi & meals."),
        AllowanceAllocationPreset("Frugal Saver", 600.0, 600.0, 200.0, 200.0, 600.0, "Maximizes Sesame Youth Savings to build an emergency buffer."),
        AllowanceAllocationPreset("Off-Campus Lodging", 1000.0, 650.0, 350.0, 150.0, 50.0, "Higher rent allocation for students living off-campus in Gaborone."),
        AllowanceAllocationPreset("Exam Month", 700.0, 650.0, 250.0, 450.0, 150.0, "Allocates extra for mobile data bundles, printing, and textbooks.")
    )

    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    val currentPreset = presets[selectedPresetIndex]

    var rentAmt by remember(selectedPresetIndex) { mutableDoubleStateOf(currentPreset.rent) }
    var foodAmt by remember(selectedPresetIndex) { mutableDoubleStateOf(currentPreset.food) }
    var transportAmt by remember(selectedPresetIndex) { mutableDoubleStateOf(currentPreset.transport) }
    var studyAmt by remember(selectedPresetIndex) { mutableDoubleStateOf(currentPreset.study) }
    var savingsAmt by remember(selectedPresetIndex) { mutableDoubleStateOf(currentPreset.savings) }

    val totalSum = rentAmt + foodAmt + transportAmt + studyAmt + savingsAmt
    val remaining = 2200.0 - totalSum

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = BsbNavyCard,
            border = BorderStroke(1.5.dp, BsbAmberGold.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "⚖️", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "PRIORITY 4 • DTEF P2,200 ALLOWANCE",
                                color = BsbAmberGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "AI Allowance Allocator",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Total Plan Guard Banner
                Surface(
                    color = if (totalSum <= 2200.0) BsbEmeraldGreen.copy(alpha = 0.15f) else BsbCrimsonWarning.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (totalSum <= 2200.0) BsbEmeraldGreen else BsbCrimsonWarning),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "DTEF DISBURSEMENT: P 2,200.00", color = Color(0xFFCBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Allocated: P ${String.format("%.0f", totalSum)} / P 2,200",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = if (remaining >= 0) "Surplus: P ${String.format("%.0f", remaining)}" else "Over by P ${String.format("%.0f", -remaining)}!",
                            color = if (remaining >= 0) BsbEmeraldGreen else BsbCrimsonWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // AI Strategy Chips
                Text(text = "Select AI Allocation Strategy:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEachIndexed { index, preset ->
                        val isSel = selectedPresetIndex == index
                        Surface(
                            color = if (isSel) BsbAmberGold else BsbNavySurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { selectedPresetIndex = index }
                        ) {
                            Text(
                                text = preset.name,
                                color = if (isSel) BsbNavyDeep else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = currentPreset.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )

                // Sliders List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AllocationSliderRow("Rent & Lodging", rentAmt, 1200.0, BsbAmberGold) { rentAmt = it }
                    AllocationSliderRow("Food & Groceries", foodAmt, 1200.0, BsbOrangeBright) { foodAmt = it }
                    AllocationSliderRow("Transport & Kombi", transportAmt, 600.0, BsbCyanSafe) { transportAmt = it }
                    AllocationSliderRow("Study & Data Bundles", studyAmt, 600.0, Color(0xFF818CF8)) { studyAmt = it }
                    AllocationSliderRow("Sesame Youth Savings", savingsAmt, 800.0, BsbEmeraldGreen) { savingsAmt = it }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(text = "Cancel", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val custom = AllowanceAllocationPreset(
                                name = currentPreset.name,
                                rent = rentAmt,
                                food = foodAmt,
                                transport = transportAmt,
                                study = studyAmt,
                                savings = savingsAmt,
                                description = currentPreset.description
                            )
                            onApplyAllocation(custom)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_apply_allowance_allocator"),
                        colors = ButtonDefaults.buttonColors(containerColor = BsbAmberGold)
                    ) {
                        Text(text = "Lock Envelopes", color = BsbNavyDeep, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AllocationSliderRow(
    title: String,
    value: Double,
    maxVal: Double,
    accentColor: Color,
    onValueChange: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "P ${value.toInt()}", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = 0f..maxVal.toFloat(),
            steps = (maxVal / 25).toInt() - 1,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

/* ========================================================================= */
/* PRIORITY 5: Student360 AI Chatbot Dialog                                 */
/* ========================================================================= */

data class StudentChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "Now"
)

@Composable
fun Student360ChatbotDialog(
    summary: FinancialSummary,
    onDismiss: () -> Unit,
    onNavigateFullAdvisor: () -> Unit
) {
    var chatList by remember {
        mutableStateOf(
            listOf(
                StudentChatMessage(
                    sender = "Student360 Bot",
                    text = "Dumela Kagiso! I'm your BSB Student AI Copilot.\n\n📊 **Live Status:** You have **P ${String.format("%.2f", summary.remainingFreeToSpend)}** free to spend (${summary.daysRemainingInCycle} days to DTEF payday). Safe daily pace is **P ${String.format("%.2f", summary.safeDailySpend)}/day**.\n\nHow can I help you stretch your P2,200 today?",
                    isUser = false
                )
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val quickPrompts = listOf(
        "Can I afford lunch at Nandos today?",
        "How do I save P300 before payday?",
        "When is my next committed bill due?",
        "How much can I safely spend today?"
    )

    fun sendBotMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = StudentChatMessage(sender = "Student", text = prompt, isUser = true)
        chatList = chatList + userMsg
        inputText = ""

        coroutineScope.launch {
            delay(500)
            val reply = when {
                prompt.contains("nandos", ignoreCase = true) || prompt.contains("lunch", ignoreCase = true) -> {
                    "A Nandos meal is approx P65. With your daily safe allowance of P${summary.safeDailySpend.toInt()}, spending P65 leaves you with P${(summary.safeDailySpend - 65).coerceAtLeast(0.0).toInt()} for tomorrow. Better option: Campus Refectory (P35) saves you P30 towards your wifi bill!"
                }
                prompt.contains("save", ignoreCase = true) -> {
                    "To save P300: Turn on BSB Smart Round-Ups on debit swipes (+P50/mo), cook 3 dinners at hostel (+P150/mo), and swap 2 taxi rides for Kombi (+P100). That deposits P300 directly into your Sesame Savings account earning 3.5% p.a.!"
                }
                prompt.contains("bill", ignoreCase = true) || prompt.contains("due", ignoreCase = true) -> {
                    "Your protected ring-fenced bills: Off-Campus Rent (P650 due Day 15) and Mascom Wifi (P149 due Day 20). Total P799 is quarantined in your BSB account so it cannot be spent accidentally."
                }
                else -> {
                    "Today's recommended maximum spend is **P ${String.format("%.2f", summary.safeDailySpend)}**. Staying under this keeps your financial health score at ${summary.healthScore}/100 and ensures you reach payday comfortably!"
                }
            }
            val botMsg = StudentChatMessage(sender = "Student360 Bot", text = reply, isUser = false)
            chatList = chatList + botMsg
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp)),
            color = BsbNavyCard,
            border = BorderStroke(1.5.dp, BsbEmeraldGreen.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🤖", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "PRIORITY 5 • BOTSWANA STUDENT COPILOT",
                                color = BsbEmeraldGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Student360 AI Chatbot",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Quick Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPrompts.forEach { prompt ->
                        Surface(
                            color = BsbNavySurface,
                            border = BorderStroke(1.dp, BsbEmeraldGreen.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.clickable { sendBotMessage(prompt) }
                        ) {
                            Text(
                                text = prompt,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Chat Messages LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatList) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (msg.isUser) BsbEmeraldGreen.copy(alpha = 0.25f) else BsbNavySurface,
                                border = BorderStroke(1.dp, if (msg.isUser) BsbEmeraldGreen else Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = msg.sender,
                                        color = if (msg.isUser) BsbEmeraldGreen else BsbAmberGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about kombi fares, meals, or savings...", fontSize = 11.sp, color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BsbEmeraldGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = BsbNavySurface,
                            unfocusedContainerColor = BsbNavySurface
                        )
                    )

                    IconButton(
                        onClick = { sendBotMessage(inputText) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BsbEmeraldGreen)
                            .testTag("btn_send_bot_msg")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = BsbNavyDeep, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* PRIORITY 6: Recurring Expenses + Local Notifications Dialog              */
/* ========================================================================= */

@Composable
fun RecurringExpensesNotificationsDialog(
    committedBills: List<CommittedBillItem>,
    onDismiss: () -> Unit,
    onAddRecurringBill: (title: String, amount: Double, dueDay: Int, category: String) -> Unit,
    onToggleProtection: (billId: Int, isRingFenced: Boolean) -> Unit,
    onSimulateNotification: (title: String, message: String) -> Unit
) {
    val context = LocalContext.current
    var showAddForm by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newDueDay by remember { mutableStateOf("15") }
    var newCategory by remember { mutableStateOf("Rent & Accommodation") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp)),
            color = BsbNavyCard,
            border = BorderStroke(1.5.dp, Color(0xFF818CF8).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🔔", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "PRIORITY 6 • RECURRING & LOCAL ALERTS",
                                color = Color(0xFF818CF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Recurring Bills & Reminders",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Notification Simulation Buttons Strip
                Text(text = "Simulate Local Student Push Notifications:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AlertSimulationChip("🏠 Rent Due (3 Days)") {
                        onSimulateNotification("Committed Bill Alert", "Off-Campus Rent of P650.00 is due in 3 days. P650 is ring-fenced safely.")
                    }
                    AlertSimulationChip("⚠️ Daily Pacing Caution") {
                        onSimulateNotification("Pacing Caution Alert", "Daily spend exceeded P60.00 today. Discretionary allowance dropped to P28.50/day.")
                    }
                    AlertSimulationChip("💰 DTEF Allowance Inflow") {
                        onSimulateNotification("DTEF Allowance Credited", "MoESD/DTEF has credited P2,200.00 to your BSB Student Account No. 10243950621.")
                    }
                    AlertSimulationChip("🔒 Ring-Fence Protection") {
                        onSimulateNotification("BSB Security Shield", "Debit swipe blocked from spending P799.00 reserved for committed bills.")
                    }
                }

                // Recurring Bills List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Registered Recurring Student Bills", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showAddForm = !showAddForm }) {
                        Text(text = if (showAddForm) "Hide Form" else "+ Add Bill", color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showAddForm) {
                    Surface(
                        color = BsbNavySurface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                placeholder = { Text("e.g. Mascom Home Wifi", fontSize = 11.sp, color = Color.Gray) },
                                label = { Text("Bill Title", fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newAmount,
                                    onValueChange = { newAmount = it },
                                    placeholder = { Text("Pula", fontSize = 11.sp, color = Color.Gray) },
                                    label = { Text("Amount (BWP)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                                )
                                OutlinedTextField(
                                    value = newDueDay,
                                    onValueChange = { newDueDay = it },
                                    label = { Text("Due Day (1-28)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                                )
                            }
                            Button(
                                onClick = {
                                    val amt = newAmount.toDoubleOrNull() ?: 0.0
                                    val day = newDueDay.toIntOrNull() ?: 15
                                    if (newTitle.isNotBlank() && amt > 0) {
                                        onAddRecurringBill(newTitle, amt, day, newCategory)
                                        newTitle = ""
                                        newAmount = ""
                                        showAddForm = false
                                        Toast.makeText(context, "Added recurring bill!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8))
                            ) {
                                Text("Register Recurring Bill", color = BsbNavyDeep, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(committedBills) { bill ->
                        Surface(
                            color = BsbNavySurface.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, if (bill.isRingFenced) BsbAmberGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(text = bill.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Due Day ${bill.dueDay} (in ${bill.daysUntilDue} days) • ${bill.category}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    Text(
                                        text = if (bill.isRingFenced) "🔒 Ring-Fenced from swipe" else "🔓 Spendable",
                                        color = if (bill.isRingFenced) BsbAmberGold else Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "P ${String.format("%.2f", bill.amount)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    Switch(
                                        checked = bill.isRingFenced,
                                        onCheckedChange = { onToggleProtection(bill.id, bill.isRingFenced) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8))
                ) {
                    Text(text = "Done", color = BsbNavyDeep, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AlertSimulationChip(text: String, onClick: () -> Unit) {
    Surface(
        color = Color(0xFF818CF8).copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color(0xFF818CF8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

/* ========================================================================= */
/* PRIORITY 7: BSB Payment Flow Integration Dialog                          */
/* ========================================================================= */

@Composable
fun BsbPaymentFlowDialog(
    onDismiss: () -> Unit,
    onExecutePayment: (payee: String, amount: Double, ref: String, category: String, token: String?, (BsbPaymentReceipt) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) } // 1: Payee & Amount, 2: 2FA / PIN, 3: Receipt

    var selectedBsbService by remember { mutableStateOf("BPC Prepaid Electricity") }
    val bsbServices = listOf(
        "BPC Prepaid Electricity",
        "Water Utilities (WUC)",
        "Mascom Campus Data",
        "Orange Airtime / Data",
        "UB Tuition & Exam Fees",
        "Student Landlord Transfer"
    )

    var recipientRef by remember { mutableStateOf("Meter: 041-9283-4819") }
    var paymentAmountText by remember { mutableStateOf("100.00") }
    var pinCode by remember { mutableStateOf("") }
    var generatedReceipt by remember { mutableStateOf<BsbPaymentReceipt?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp)),
            color = BsbNavyCard,
            border = BorderStroke(1.5.dp, Color(0xFFF43F5E).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "💳", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "PRIORITY 7 • BSB DIGITAL BANKING",
                                color = Color(0xFFF43F5E),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "BSB Bill Payment & Transfers",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                if (step == 1) {
                    // Step 1: Select Payee & Amount
                    Text(text = "Select Payment Destination:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bsbServices.forEach { service ->
                            val isSel = selectedBsbService == service
                            Surface(
                                color = if (isSel) Color(0xFFF43F5E) else BsbNavySurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    selectedBsbService = service
                                    recipientRef = when (service) {
                                        "BPC Prepaid Electricity" -> "Meter No: 041-9823-1102"
                                        "Water Utilities (WUC)" -> "Account No: WUC-UB-8492"
                                        "Mascom Campus Data" -> "Cellphone: 71 649 231"
                                        "Orange Airtime / Data" -> "Cellphone: 72 341 890"
                                        "UB Tuition & Exam Fees" -> "Student ID: UB-2022-04829"
                                        else -> "Account: 1098472910 (Landlord)"
                                    }
                                }
                            ) {
                                Text(
                                    text = service,
                                    color = if (isSel) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = recipientRef,
                        onValueChange = { recipientRef = it },
                        label = { Text("Account / Meter / Reference", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Amount (BWP)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )

                    Surface(
                        color = Color(0xFFF43F5E).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                            Text(text = "Paid from: BSB Student Allowance (Free Data Zero-Rated)", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_proceed_bsb_payment"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                    ) {
                        Text("Proceed to Authorization", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                } else if (step == 2) {
                    // Step 2: 2FA & PIN Authentication
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Authorize BSB Payment", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Paying P $paymentAmountText to $selectedBsbService", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 4) pinCode = it },
                            label = { Text("Enter 4-Digit BSB PIN", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                            modifier = Modifier.width(180.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "🔒 Secured by BSB 3D-Secure Architecture", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { step = 1 }, modifier = Modifier.weight(1f)) {
                            Text("Back", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val amt = paymentAmountText.toDoubleOrNull() ?: 100.0
                                val token = if (selectedBsbService.contains("Electricity")) "8492-1924-0012-9842" else null
                                onExecutePayment(selectedBsbService, amt, recipientRef, "Utilities", token) { r ->
                                    generatedReceipt = r
                                    step = 3
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_confirm_pin_bsb_payment"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                        ) {
                            Text("Confirm & Pay", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    // Step 3: Digital Payment Receipt
                    generatedReceipt?.let { receipt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BsbNavySurface),
                            border = BorderStroke(1.5.dp, BsbEmeraldGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = CircleShape, color = BsbEmeraldGreen.copy(alpha = 0.2f), modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BsbEmeraldGreen, modifier = Modifier.size(24.dp))
                                    }
                                }

                                Text(text = "BSB OFFICIAL PAYMENT RECEIPT", color = BsbEmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Text(text = "P ${String.format("%.2f", receipt.amount)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text(text = "Paid to ${receipt.payeeName}", color = Color(0xFFCBD5E1), fontSize = 12.sp)

                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Ref Number:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    Text(text = receipt.referenceNumber, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "From Account:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    Text(text = "BSB No. ${receipt.fromAccount}", color = Color.White, fontSize = 10.sp)
                                }

                                receipt.tokenCode?.let { token ->
                                    Surface(color = BsbAmberGold.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "⚡ PREPAID ELECTRICITY TOKEN", color = BsbAmberGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(text = token, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                Text(text = "Bank Stamp: Botswana Savings Bank Certified", color = Color(0xFF94A3B8), fontSize = 9.sp)
                            }
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BsbEmeraldGreen)
                    ) {
                        Text("Done", color = BsbNavyDeep, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/* ========================================================================= */
/* PRIORITY 8: Savings Goals + Monthly Reports Dialog                       */
/* ========================================================================= */

@Composable
fun SavingsGoalsAndReportsDialog(
    goals: List<StudentSavingsGoal>,
    summary: FinancialSummary,
    onDismiss: () -> Unit,
    onDepositToGoal: (goalId: String, amount: Double) -> Unit,
    onAddGoal: (title: String, target: Double, emoji: String, targetMonth: String) -> Unit
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Savings Goals, 1 = Monthly Health Report
    var showCreateGoalDialog by remember { mutableStateOf(false) }

    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }
    var newGoalEmoji by remember { mutableStateOf("🎯") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = BsbNavyCard,
            border = BorderStroke(1.5.dp, Color(0xFF34D399).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "📊", fontSize = 20.sp)
                        Column {
                            Text(
                                text = "PRIORITY 8 • WEALTH MILESTONES",
                                color = Color(0xFF34D399),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Savings Goals & Monthly Report",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Sub-tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BsbNavySurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeSubTab == 0) Color(0xFF34D399) else Color.Transparent)
                            .clickable { activeSubTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎯 Student Goals (${goals.size})",
                            color = if (activeSubTab == 0) BsbNavyDeep else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeSubTab == 1) Color(0xFF34D399) else Color.Transparent)
                            .clickable { activeSubTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📑 Monthly Report Card",
                            color = if (activeSubTab == 1) BsbNavyDeep else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (activeSubTab == 0) {
                    // Savings Goals Tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Active Student Targets", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showCreateGoalDialog = !showCreateGoalDialog }) {
                            Text(text = "+ Create Goal", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showCreateGoalDialog) {
                        Surface(
                            color = BsbNavySurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newGoalTitle,
                                    onValueChange = { newGoalTitle = it },
                                    label = { Text("Goal Title (e.g. Laptop)", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                                )
                                OutlinedTextField(
                                    value = newGoalTarget,
                                    onValueChange = { newGoalTarget = it },
                                    label = { Text("Target Amount (BWP)", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                                )
                                Button(
                                    onClick = {
                                        val target = newGoalTarget.toDoubleOrNull() ?: 0.0
                                        if (newGoalTitle.isNotBlank() && target > 0) {
                                            onAddGoal(newGoalTitle, target, newGoalEmoji, "Dec 2026")
                                            newGoalTitle = ""
                                            newGoalTarget = ""
                                            showCreateGoalDialog = false
                                            Toast.makeText(context, "Goal added to Sesame Savings!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399))
                                ) {
                                    Text("Add Goal", color = BsbNavyDeep, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(goals) { goal ->
                            val progress = (goal.currentAmount / goal.targetAmount.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                            Surface(
                                color = BsbNavySurface.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(text = goal.iconEmoji, fontSize = 18.sp)
                                            Column {
                                                Text(text = goal.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(text = "Target Date: ${goal.targetMonth}", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                            }
                                        }
                                        Text(
                                            text = "P ${String.format("%.0f", goal.currentAmount)} / P ${String.format("%.0f", goal.targetAmount)}",
                                            color = Color(0xFF34D399),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = Color(0xFF34D399),
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "${(progress * 100).toInt()}% completed", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                color = Color(0xFF34D399).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.clickable { onDepositToGoal(goal.id, 50.0) }
                                            ) {
                                                Text("+ P50 Boost", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                            }
                                            Surface(
                                                color = Color(0xFF34D399).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.clickable { onDepositToGoal(goal.id, 100.0) }
                                            ) {
                                                Text("+ P100 Boost", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Monthly Report Card Tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = BsbNavySurface,
                            border = BorderStroke(1.dp, BsbAmberGold.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "MONTHLY DTEF CYCLE AUDIT", color = BsbAmberGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    Text(text = "SEPTEMBER 2026", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Allowance Inflow:", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text(text = "+ P ${String.format("%.2f", summary.monthlyAllowance)}", color = BsbEmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Total Spent to Date:", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text(text = "- P ${String.format("%.2f", summary.totalSpent)}", color = BsbOrangeBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Committed Protected Bills:", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text(text = "P ${String.format("%.2f", summary.totalCommitted)}", color = BsbAmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Sesame Youth Savings Reserve:", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text(text = "P ${String.format("%.2f", summary.totalSavings)}", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // AI Coach Performance Assessment
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BsbNavySurface.copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, BsbCyanSafe.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "AI FINANCIAL COACH ASSESSMENT", color = BsbCyanSafe, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                Text(text = "Health Score: ${summary.healthScore}/100 • ${summary.healthGrade}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = summary.healthAdvice, color = Color(0xFF94A3B8), fontSize = 10.sp, lineHeight = 14.sp)
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Exporting BSB_Student_Audit_Sept2026.pdf...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399))
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = BsbNavyDeep, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Export Statement (PDF)", color = BsbNavyDeep, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399))
                ) {
                    Text("Close", color = BsbNavyDeep, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

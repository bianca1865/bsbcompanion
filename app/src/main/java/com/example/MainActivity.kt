package com.example

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.ScanningCameraScreen
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import com.example.viewmodel.CompanionViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = Repository(database)
        val viewModel: CompanionViewModel by viewModels {
            CompanionViewModelFactory(repository)
        }

        setContent {
            val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
            val isDark = loggedInUser?.isDarkMode ?: true
            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(viewModel)
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    EXPENSES("Expenses", Icons.Default.ReceiptLong),
    BUDGET("Budget", Icons.Default.AccountBalanceWallet),
    AI_ASSISTANT("AI Assistant", Icons.Default.AutoAwesome),
    MORE("More", Icons.Default.Menu)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainAppScreen(viewModel: CompanionViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showCamera by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (showCamera) {
        if (cameraPermissionState.status.isGranted) {
            ScanningCameraScreen(
                onDismiss = { showCamera = false },
                onReceiptDetected = { merchant, amount, date, category ->
                    viewModel.processScannedReceipt(merchant, amount, date, category)
                    showCamera = false
                }
            )
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera permission required", color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showCamera = false }) {
                        Text("Back")
                    }
                }
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = NavySurface) {
                    NavigationTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NavyPrimary,
                                selectedIconColor = CoralOrange,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = CoralOrange,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyBackground)
                    .padding(padding)
            ) {
                when (selectedTab) {
                    NavigationTab.HOME -> HomeScreen(viewModel)
                    NavigationTab.EXPENSES -> ExpensesScreen(viewModel, onScanClick = { showCamera = true })
                    NavigationTab.BUDGET -> BudgetScreen(viewModel)
                    NavigationTab.AI_ASSISTANT -> AIAssistantScreen(viewModel)
                    NavigationTab.MORE -> MoreScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: CompanionViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val allocations by viewModel.budgetAllocations.collectAsStateWithLifecycle()
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val allowance = user?.monthlyAllowance ?: 2200.0
    val totalSpent = expenses.sumOf { it.amount }
    val committedExpenses = 800.0
    val remaining = allowance - totalSpent - committedExpenses
    val savings = allocations.find { it.category == "Savings" }?.allocatedAmount ?: 300.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(CoralOrange, CircleShape), contentAlignment = Alignment.Center) {
                Text("BSB", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Student360",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Your money. Your habits. Your future.",
                    color = CoralOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Monthly Summary", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem("Allowance", "P${allowance.toInt()}", TextPrimary)
                    SummaryItem("Spent", "P${totalSpent.toInt()}", Color.Red)
                }
                
                HorizontalDivider(color = NavyPrimary)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem("Committed", "P${committedExpenses.toInt()}", TextMuted)
                    SummaryItem("Available", "P${remaining.coerceAtLeast(0.0).toInt()}", Color.Green)
                }

                HorizontalDivider(color = NavyPrimary)

                SummaryItem("Savings", "P${savings.toInt()}", GoldOrange)
            }
        }

        Text("Spending Breakdown", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(NavySurface, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DonutSmall, contentDescription = null, modifier = Modifier.size(48.dp), tint = CoralOrange)
                Text("Spending Categories Chart", color = TextMuted, fontSize = 12.sp)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CoralOrange)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "\"You've spent more on food this month than last month. Consider reviewing your Takeaway budget.\"",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Text("Upcoming Payments", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        UpcomingPaymentRow("Rent", "P800", "Due in 3 days")
        UpcomingPaymentRow("Data/WiFi", "P100", "Due in 7 days")

        Spacer(modifier = Modifier.height(24.dp))
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
fun UpcomingPaymentRow(name: String, amount: String, due: String) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(due, color = TextMuted, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(amount, color = GoldOrange, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { 
                        Toast.makeText(context, "Redirecting to secure BSB payment flow...", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Pay Now", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ExpensesScreen(viewModel: CompanionViewModel, onScanClick: () -> Unit) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Processing file...", Toast.LENGTH_SHORT).show()
            viewModel.processUploadedFile(uri)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Expenses", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpenseActionButton(
                icon = Icons.Default.CameraAlt,
                label = "Scan Receipt",
                modifier = Modifier.weight(1f),
                onClick = onScanClick
            )
            ExpenseActionButton(
                icon = Icons.Default.FileUpload,
                label = "Upload Statement",
                modifier = Modifier.weight(1f),
                onClick = { launcher.launch("*/*") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(expenses) { expense ->
                ExpenseCard(expense)
            }
        }
    }
}

@Composable
fun ExpenseActionButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(60.dp),
        contentPadding = PaddingValues(8.dp),
        border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.4f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(20.dp))
            Text(label, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(NavyPrimary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(expense.category) {
                            "Groceries" -> Icons.Default.ShoppingCart
                            "Data/WiFi" -> Icons.Default.Wifi
                            "Transport" -> Icons.Default.DirectionsBus
                            else -> Icons.Default.Receipt
                        },
                        contentDescription = null,
                        tint = CoralOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(expense.merchant, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("${expense.date} • ${expense.category}", color = TextMuted, fontSize = 12.sp)
                }
            }
            Text("P${String.format("%.2f", expense.amount)}", color = GoldOrange, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun BudgetScreen(viewModel: CompanionViewModel) {
    val allocations by viewModel.budgetAllocations.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Allowance Allocator", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = BlueAccent.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BlueAccent.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BlueAccent)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "AI Recommendation: Based on your previous spending, consider increasing your transport allocation from P250 to P350.",
                    color = BlueAccent,
                    fontSize = 13.sp
                )
            }
        }

        allocations.forEach { allocation ->
            BudgetCategoryIndicator(allocation, onAdjust = { newVal ->
                viewModel.updateAllocation(allocation.category, newVal, allocation.isEssential)
            })
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* AI Optimization */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Optimize with AI", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Button(
            onClick = { /* Reset */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
        ) {
            Text("Reset Budget", color = TextPrimary)
        }
    }
}

@Composable
fun BudgetCategoryIndicator(allocation: BudgetAllocation, onAdjust: (Double) -> Unit) {
    val progress = (allocation.spentAmount / allocation.allocatedAmount).toFloat().coerceIn(0f, 1f)
    val remaining = allocation.allocatedAmount - allocation.spentAmount
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (allocation.isEssential) {
                    Icon(Icons.Default.Lock, contentDescription = "Essential", modifier = Modifier.size(14.dp), tint = GoldOrange)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(allocation.category, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onAdjust(allocation.allocatedAmount - 10) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = CoralOrange)
                }
                Text("P${allocation.allocatedAmount.toInt()}", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { onAdjust(allocation.allocatedAmount + 10) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CoralOrange)
                }
            }
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
            color = if (progress > 0.9f) Color.Red else CoralOrange,
            trackColor = NavyPrimary
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Spent: P${allocation.spentAmount.toInt()}", color = TextMuted, fontSize = 11.sp)
            Text(
                text = if (remaining >= 0) "P${remaining.toInt()} remaining" else "P${(-remaining).toInt()} over budget",
                color = if (remaining >= 0) Color.Green else Color.Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AIAssistantScreen(viewModel: CompanionViewModel) {
    var message by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<Pair<String, String>>("AI" to "Hello! I'm Student360 AI, your official BSB financial companion. How can I help you manage your P2,200 allowance today?") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Student360 AI", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        
        LazyColumn(
            modifier = Modifier.weight(1f).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { (sender, text) ->
                val isAI = sender == "AI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isAI) NavySurface else CoralOrange),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isAI) 0.dp else 16.dp, bottomEnd = if (isAI) 16.dp else 0.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(text, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about your spending...", color = TextMuted) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralOrange,
                    unfocusedBorderColor = NavySurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedContainerColor = NavySurface,
                    focusedContainerColor = NavySurface
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (message.isNotBlank()) {
                        chatMessages.add("User" to message)
                        val response = when {
                            message.contains("spending", true) -> "You've spent P1,340 so far this month. Your biggest category is Groceries at P420."
                            message.contains("money", true) || message.contains("left", true) -> "You have P60 available for flexible spending after committed expenses."
                            message.contains("save", true) -> "I suggest saving 15% of your allowance (P330). You currently have P300 allocated."
                            else -> "I'm analyzing your spending patterns. You're doing a great job staying within your transport budget!"
                        }
                        chatMessages.add("AI" to response)
                        message = ""
                    }
                },
                containerColor = CoralOrange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
            }
        }
    }
}

@Composable
fun MoreScreen(viewModel: CompanionViewModel) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("More", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        
        MoreItem("Payment Calendar", Icons.Default.DateRange)
        MoreItem("Recurring Expenses", Icons.Default.Autorenew)
        MoreItem("Notification Settings", Icons.Default.Notifications)
        MoreItem("Savings Goals", Icons.Default.Savings)
        MoreItem("Monthly Financial Report", Icons.Default.Assessment)
        MoreItem("Notification Preferences", Icons.Default.Settings)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("BSB Integration", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Securely linked to your Botswana Savings Bank account.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun MoreItem(title: String, icon: ImageVector) {
    Surface(
        onClick = { },
        color = NavySurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
        }
    }
}

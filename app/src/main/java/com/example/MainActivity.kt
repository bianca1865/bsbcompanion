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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import com.example.viewmodel.CompanionViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * STUDENT360 - Main Application Controller
 * Handles navigation and core screen orchestration.
 * Dashboard features 4 interactive visuals and a dedicated Profile/Logout menu.
 */
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
            val user by viewModel.userProfile.collectAsStateWithLifecycle()
            val isDark = user?.isDarkMode ?: true
            
            MyApplicationTheme(darkTheme = isDark) {
                Surface(color = NavyBackground) {
                    when {
                        user == null || !user!!.isLoggedIn -> AuthNavigation(viewModel)
                        !user!!.hasCompletedOnboarding -> OnboardingScreen(viewModel)
                        else -> MainAppScreen(viewModel)
                    }
                }
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    EXPENSES("Expenses", Icons.Default.ReceiptLong),
    BUDGET("Budget", Icons.Default.AccountBalanceWallet),
    ORBIT("Orbit", Icons.Default.AutoAwesome),
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
                Crossfade(targetState = selectedTab, label = "TabSwitch") { tab ->
                    when (tab) {
                        NavigationTab.HOME -> HomeScreen(viewModel, onAction = { selectedTab = it })
                        NavigationTab.EXPENSES -> ExpensesScreen(viewModel, onScanClick = { showCamera = true })
                        NavigationTab.BUDGET -> BudgetScreen(viewModel)
                        NavigationTab.ORBIT -> OrbitScreen(viewModel)
                        NavigationTab.MORE -> MoreScreen(viewModel)
                    }
                }
            }
        }
    }
}

/**
 * HOME SCREEN - Dashboard with 4 Visuals
 * 1. Progress Gauge (Remaining Balance)
 * 2. Donut Chart (Category Breakdown)
 * 3. Bar Chart (Weekly Trend)
 * 4. Performance Bars (Budget Comparison)
 */
@Composable
fun HomeScreen(viewModel: CompanionViewModel, onAction: (NavigationTab) -> Unit) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val allocations by viewModel.budgetAllocations.collectAsStateWithLifecycle()
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val weeklySpending by viewModel.weeklySpending.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    
    val scrollState = rememberScrollState(initial = viewModel.homeScrollValue)
    LaunchedEffect(scrollState.value) { viewModel.homeScrollValue = scrollState.value }
    
    val context = LocalContext.current
    val allowance = user?.monthlyAllowance ?: 0.0
    val totalSpent = expenses.sumOf { it.amount }
    val remaining = allowance - totalSpent
    
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Processing statement...", Toast.LENGTH_SHORT).show()
            viewModel.processUploadedFile(uri)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Profile Icon
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Student360Branding.Logo(size = 40.dp, showText = false)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("$greeting, ${user?.firstName} !", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Your financial overview.", color = CoralOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            var showProfileMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showProfileMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account",
                        tint = CoralOrange,
                        modifier = Modifier.size(36.dp)
                    )
                }
                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false },
                    offset = androidx.compose.ui.unit.DpOffset(0.dp, 8.dp),
                    containerColor = NavySurface
                ) {
                    DropdownMenuItem(
                        text = { Text("Profile", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = CoralOrange) },
                        onClick = { 
                            showProfileMenu = false
                            onAction(NavigationTab.MORE) 
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout", color = Color.Red) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red) },
                        onClick = { 
                            showProfileMenu = false
                            viewModel.logout()
                        }
                    )
                }
            }
        }

        // 1. Allowance Gauge
        Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                AllowanceProgressRing(spent = totalSpent, total = allowance, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text("Remaining Balance", color = TextMuted, fontSize = 12.sp)
                    Text("P${remaining.toInt()}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("Goal: P${allowance.toInt()}", color = CoralOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2 & 3. Breakdown Donut & Weekly Trend Bars
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(1.1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Categories", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    SpendingDonutChart(categories = categoryBreakdown.ifEmpty { mapOf("Other" to 1.0) }, modifier = Modifier.fillMaxWidth())
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(0.9f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    SimpleBarChart(data = weeklySpending, modifier = Modifier.height(100.dp))
                }
            }
        }

        // 4. Budget Comparison Bars
        Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Budget Performance", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CategoryComparisonChart(allocations = allocations, modifier = Modifier.fillMaxWidth())
            }
        }

        // AI Advice
        val insight by viewModel.dashboardInsight.collectAsStateWithLifecycle()
        Card(colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.5f)), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.3f))) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = CoralOrange)
                Spacer(modifier = Modifier.width(16.dp))
                Text(insight, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAction(NavigationTab.BUDGET) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = CoralOrange)) {
                Text("Manage Budget", fontSize = 11.sp)
            }
            OutlinedButton(onClick = { launcher.launch("*/*") }, modifier = Modifier.weight(1f)) {
                Text("Add Statement", fontSize = 11.sp, color = Color.White)
            }
        }

        Text("Upcoming Tasks", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        val recurring by viewModel.recurringExpenses.collectAsStateWithLifecycle()
        if (recurring.none { !it.isPaid }) {
            Text("No pending payments today.", color = TextMuted, fontSize = 13.sp)
        } else {
            recurring.filter { !it.isPaid }.take(2).forEach { exp -> UpcomingPaymentRow(exp, viewModel) }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * SHARED COMPONENTS
 */
@Composable
fun UpcomingPaymentRow(expense: RecurringExpense, viewModel: CompanionViewModel) {
    val context = LocalContext.current
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val status = when {
        expense.isPaid -> "Paid"
        expense.dueDate < today -> "Overdue"
        expense.dueDate == today -> "Due Today"
        else -> "Upcoming"
    }
    Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(expense.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Due: ${expense.dueDate} • $status", color = if(status == "Overdue") Color.Red else TextMuted, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("P${expense.amount.toInt()}", color = GoldOrange, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { MockPaymentService.initiatePayment(context, expense.name, expense.amount) { viewModel.markRecurringAsPaid(expense) } },
                    enabled = !expense.isPaid,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (expense.isPaid) "Done" else "Pay", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/**
 * EXPENSES SCREEN
 */
@Composable
fun ExpensesScreen(viewModel: CompanionViewModel, onScanClick: () -> Unit) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = viewModel.expensesScrollIndex, initialFirstVisibleItemScrollOffset = viewModel.expensesScrollOffset)
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.expensesScrollIndex = listState.firstVisibleItemIndex
        viewModel.expensesScrollOffset = listState.firstVisibleItemScrollOffset
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.processUploadedFile(uri)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Transactions", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseActionButton(icon = Icons.Default.CameraAlt, label = "Capture Receipt", modifier = Modifier.weight(1f), onClick = onScanClick)
            ExpenseActionButton(icon = Icons.Default.FileUpload, label = "Import Log", modifier = Modifier.weight(1f), onClick = { launcher.launch("*/*") })
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (expenses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No history found.", color = TextMuted) }
        } else {
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(expenses) { expense -> ExpenseCard(expense, onDelete = { viewModel.deleteExpense(expense) }) }
            }
        }
    }
}

@Composable
fun ExpenseActionButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = NavySurface), shape = RoundedCornerShape(12.dp), modifier = modifier.height(60.dp), border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.4f))) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = CoralOrange, modifier = Modifier.size(20.dp))
            Text(label, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense, onDelete: () -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(16.dp), onClick = { showOptions = true }) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(NavyPrimary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = when(expense.category) { "Groceries" -> Icons.Default.ShoppingCart; "Data/WiFi" -> Icons.Default.Wifi; "Transport" -> Icons.Default.DirectionsBus; "Rent" -> Icons.Default.Home; else -> Icons.Default.Receipt }, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(20.dp))
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
    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Clean up?") },
            text = { Text("Would you like to remove this entry?") },
            confirmButton = { TextButton(onClick = { onDelete(); showOptions = false }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showOptions = false }) { Text("Cancel") } },
            containerColor = NavySurface, titleContentColor = Color.White, textContentColor = TextMuted
        )
    }
}

/**
 * BUDGET SCREEN
 */
@Composable
fun BudgetScreen(viewModel: CompanionViewModel) {
    val allocations by viewModel.budgetAllocations.collectAsStateWithLifecycle()
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState(initial = viewModel.budgetScrollValue)
    LaunchedEffect(scrollState.value) { viewModel.budgetScrollValue = scrollState.value }
    
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAllocation by remember { mutableStateOf<BudgetAllocation?>(null) }
    val allowance = user?.monthlyAllowance ?: 0.0
    val totalAllocated = allocations.sumOf { it.allocatedAmount }
    val remainingToAllocate = allowance - totalAllocated
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Budget Planner", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Monthly Allowance", color = TextMuted, fontSize = 12.sp)
                if (viewModel.isEditingAllowance) {
                    OutlinedTextField(value = viewModel.allowanceInput, onValueChange = { viewModel.allowanceInput = it }, modifier = Modifier.fillMaxWidth(), colors = authFieldColors())
                    Button(onClick = { viewModel.setAllowance(viewModel.allowanceInput.toDoubleOrNull() ?: 0.0); viewModel.isEditingAllowance = false }, colors = ButtonDefaults.buttonColors(CoralOrange)) { Text("Set Goal") }
                } else {
                    Text("P${allowance.toInt()}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    TextButton(onClick = { viewModel.isEditingAllowance = true }) { Text(if(allowance <= 0) "Define Allowance" else "Modify Limit", color = CoralOrange) }
                }
                Spacer(Modifier.height(16.dp))
                Text("Setup: P${totalAllocated.toInt()} / Left: P${remainingToAllocate.toInt()}", color = if(remainingToAllocate < 0) Color.Red else Color.Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Budget Bricks", color = TextPrimary, fontWeight = FontWeight.Bold)
            Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(CoralOrange)) { Icon(Icons.Default.Add, null); Text("Create") }
        }
        if (allocations.isEmpty()) { Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { Text("Empty budget.", color = TextMuted) } }
        else { allocations.forEach { allocation -> BudgetCategoryIndicator(allocation = allocation, onDelete = { viewModel.deleteAllocation(allocation) }, onEdit = { editingAllocation = allocation }, onAdjust = { viewModel.updateAllocation(allocation.copy(allocatedAmount = it)) }) } }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { scope.launch { val advice = viewModel.optimizeBudget(); Toast.makeText(context, advice, Toast.LENGTH_LONG).show() } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("AI Optimize", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    if (showAddDialog) { AllocationDialog(onDismiss = { showAddDialog = false }, onSave = { n, a, c, r, d -> viewModel.addAllocation(n, a, c, r, d); showAddDialog = false }) }
    if (editingAllocation != null) { AllocationDialog(allocation = editingAllocation, onDismiss = { editingAllocation = null }, onSave = { n, a, c, r, d -> viewModel.updateAllocation(editingAllocation!!.copy(name = n, allocatedAmount = a, category = c, isRecurring = r, dueDate = d)); editingAllocation = null }) }
}

@Composable
fun BudgetCategoryIndicator(allocation: BudgetAllocation, onDelete: () -> Unit, onEdit: () -> Unit, onAdjust: (Double) -> Unit) {
    val progress = if (allocation.allocatedAmount > 0) (allocation.spentAmount / allocation.allocatedAmount).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = allocation.allocatedAmount - allocation.spentAmount
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.background(NavyPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f).clickable { onEdit() }) { Text(allocation.name, color = TextPrimary, fontWeight = FontWeight.Bold); Text(allocation.category, color = TextMuted, fontSize = 10.sp) }
            Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = { onAdjust(allocation.allocatedAmount - 10) }) { Icon(Icons.Default.Remove, null, tint = CoralOrange) }; Text("P${allocation.allocatedAmount.toInt()}", color = TextPrimary, fontWeight = FontWeight.Bold); IconButton(onClick = { onAdjust(allocation.allocatedAmount + 10) }) { Icon(Icons.Default.Add, null, tint = CoralOrange) }; IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) } }
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = if (progress > 0.9f) Color.Red else CoralOrange, trackColor = NavyPrimary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Used: P${allocation.spentAmount.toInt()}", color = TextMuted, fontSize = 11.sp); Text(text = if (remaining >= 0) "P${remaining.toInt()} left" else "P${(-remaining).toInt()} excess", color = if (remaining >= 0) Color.Green else Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun AllocationDialog(allocation: BudgetAllocation? = null, onDismiss: () -> Unit, onSave: (String, Double, String, Boolean, Int?) -> Unit) {
    var name by remember { mutableStateOf(allocation?.name ?: "") }
    var amount by remember { mutableStateOf(allocation?.allocatedAmount?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(allocation?.category ?: "Groceries") }
    var isRecurring by remember { mutableStateOf(allocation?.isRecurring ?: false) }
    var day by remember { mutableStateOf(allocation?.dueDate?.toString() ?: "") }
    val categories = listOf("Rent", "Transport", "Groceries", "Connectivity", "Food", "Study", "Fun", "Saving", "Urgent", "Misc")
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(NavySurface), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if(allocation == null) "New Allocation" else "Update Item", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Label") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Limit (P)") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
                Text("Type", color = TextMuted, fontSize = 12.sp)
                var expanded by remember { mutableStateOf(false) }
                Box { TextButton(onClick = { expanded = true }) { Text(category, color = CoralOrange) }; DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { categories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false }) } } }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it }, colors = CheckboxDefaults.colors(checkedColor = CoralOrange)); Text("Monthly Repeat?", color = Color.White) }
                if (isRecurring) { OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day of Month") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth()) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = onDismiss, Modifier.weight(1f)) { Text("Back", color = TextMuted) }; Button(onClick = { onSave(name, amount.toDoubleOrNull() ?: 0.0, category, isRecurring, day.toIntOrNull()) }, colors = ButtonDefaults.buttonColors(CoralOrange), modifier = Modifier.weight(1f)) { Text("Lock In") } }
            }
        }
    }
}

/**
 * ORBIT SCREEN - AI Chat
 */
@Composable
fun OrbitScreen(viewModel: CompanionViewModel) {
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val chatMessages = viewModel.chatMessages
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = viewModel.orbitScrollIndex, initialFirstVisibleItemScrollOffset = viewModel.orbitScrollOffset)
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }.collect { viewModel.orbitScrollIndex = it.first; viewModel.orbitScrollOffset = it.second }
    }
    val quickPrompts = listOf("Scan my lifestyle", "Burn rate?", "Savings tip", "Budget check", "Am I safe?", "Rebalance")
    LaunchedEffect(chatMessages.size, isThinking) { if (chatMessages.isNotEmpty() || isThinking) { listState.animateScrollToItem((chatMessages.size - 1 + if(isThinking) 1 else 0).coerceAtLeast(0)) } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Orbit Intelligence", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chatMessages) { (sender, text) ->
                val isAI = sender == "AI"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End) {
                    Card(colors = CardDefaults.cardColors(containerColor = if (isAI) NavySurface else CoralOrange), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isAI) 0.dp else 16.dp, bottomEnd = if (isAI) 16.dp else 0.dp), modifier = Modifier.widthIn(max = 280.dp)) { Text(text, modifier = Modifier.padding(12.dp), color = Color.White, fontSize = 14.sp) }
                }
            }
            if (isThinking) { item { AIThinkingIndicator() } }
        }
        LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(quickPrompts) { prompt -> SuggestionChip(onClick = { viewModel.sendMessage(prompt) }, label = { Text(prompt, fontSize = 10.sp, color = Color.White) }, colors = SuggestionChipDefaults.suggestionChipColors(containerColor = NavySurface), border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.3f))) } }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = viewModel.orbitMessage, onValueChange = { viewModel.orbitMessage = it }, modifier = Modifier.weight(1f), placeholder = { Text("Consult Orbit...", color = TextMuted) }, shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralOrange, unfocusedBorderColor = NavySurface, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, unfocusedContainerColor = NavySurface, focusedContainerColor = NavySurface))
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(onClick = { viewModel.sendMessage(viewModel.orbitMessage) }, containerColor = CoralOrange, contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Send, null) }
        }
    }
}

@Composable
fun AIThinkingIndicator() {
    var dotIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(500); dotIndex = (dotIndex + 1) % 3 } }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(colors = CardDefaults.cardColors(containerColor = NavySurface), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp), modifier = Modifier.widthIn(max = 80.dp)) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (dotIndex == 0) "●" else "○", color = Color.White, fontSize = 12.sp)
                Text(if (dotIndex == 1) "●" else "○", color = Color.White, fontSize = 12.sp)
                Text(if (dotIndex == 2) "●" else "○", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

/**
 * MORE SCREEN
 */
@Composable
fun MoreScreen(viewModel: CompanionViewModel) {
    val scrollState = rememberScrollState(initial = viewModel.moreScrollValue)
    LaunchedEffect(scrollState) { snapshotFlow { scrollState.value }.collect { viewModel.moreScrollValue = it } }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Account", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
        MoreItem("Profile Hub", Icons.Default.Person) { viewModel.activeMoreModal = "Profile" }
        MoreItem("Vault Goals", Icons.Default.Savings) { viewModel.activeMoreModal = "Savings" }
        MoreItem("Ledger Calendar", Icons.Default.DateRange) { viewModel.activeMoreModal = "Calendar" }
        MoreItem("Pending Dues", Icons.Default.Schedule) { viewModel.activeMoreModal = "Upcoming" }
        MoreItem("Visual Reports", Icons.Default.Assessment) { viewModel.activeMoreModal = "Reports" }
        MoreItem("Alert Controls", Icons.Default.Notifications) { viewModel.activeMoreModal = "Notifications" }
        MoreItem("App Settings", Icons.Default.Settings) { viewModel.activeMoreModal = "Settings" }
        MoreItem("Get Support", Icons.AutoMirrored.Filled.Help) { viewModel.activeMoreModal = "Help" }
        MoreItem("Legal & Version", Icons.Default.Info) { viewModel.activeMoreModal = "About" }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.logout() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) { Text("Sign Out") }
    }
    viewModel.activeMoreModal?.let { modal ->
        Dialog(onDismissRequest = { viewModel.activeMoreModal = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(NavyBackground).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.activeMoreModal = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                        Text(modal, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    when(modal) {
                        "Profile" -> ProfileView(viewModel)
                        "Savings" -> SavingsView(viewModel)
                        "Calendar" -> CalendarView(viewModel)
                        "Upcoming" -> UpcomingPaymentsListView(viewModel)
                        "Notifications" -> NotificationsView(viewModel)
                        "Reports" -> ReportsView(viewModel)
                        else -> ComingSoonView(modal)
                    }
                }
            }
        }
    }
}

@Composable
fun MoreItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, color = NavySurface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = CoralOrange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextMuted)
        }
    }
}

@Composable
fun ProfileView(viewModel: CompanionViewModel) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileField("User", user?.firstName ?: "")
        ProfileField("Surname", user?.lastName ?: "")
        ProfileField("Contact", user?.email ?: "")
        ProfileField("Campus", user?.institution ?: "")
        ProfileField("Monthly Fund", "P${user?.monthlyAllowance?.toInt()}")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { /* Edit logic */ }, colors = ButtonDefaults.buttonColors(CoralOrange), modifier = Modifier.fillMaxWidth()) { Text("Update Profile") }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        HorizontalDivider(color = NavyPrimary, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SavingsView(viewModel: CompanionViewModel) {
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { showAdd = true }, colors = ButtonDefaults.buttonColors(CoralOrange), modifier = Modifier.fillMaxWidth()) { Text("Target New Goal") }
        if (goals.isEmpty()) { Text("No active targets.", color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) }
        else { goals.forEach { goal ->
            Card(colors = CardDefaults.cardColors(NavySurface), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(goal.name, color = Color.White, fontWeight = FontWeight.Bold); Text("P${goal.targetAmount.toInt()}", color = GoldOrange) }
                    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = GoldOrange, trackColor = NavyPrimary)
                    Text("Funded: P${goal.currentAmount.toInt()} / P${goal.targetAmount.toInt()}", color = TextMuted, fontSize = 11.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = { viewModel.updateSavingsGoal(goal.copy(currentAmount = goal.currentAmount + 50)) }) { Text("+ P50", color = CoralOrange) } }
                }
            }
        } }
    }
    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            Card(colors = CardDefaults.cardColors(NavySurface), shape = RoundedCornerShape(16.dp)) {
                var name by remember { mutableStateOf("") }; var target by remember { mutableStateOf("") }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("New Target", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal Name") }, colors = authFieldColors())
                    OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target P") }, colors = authFieldColors())
                    Button(onClick = { viewModel.addSavingsGoal(name, target.toDoubleOrNull() ?: 0.0); showAdd = false }, colors = ButtonDefaults.buttonColors(CoralOrange), modifier = Modifier.fillMaxWidth()) { Text("Create") }
                }
            }
        }
    }
}

@Composable
fun CalendarView(viewModel: CompanionViewModel) {
    val recurring by viewModel.recurringExpenses.collectAsStateWithLifecycle()
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Payment Timeline", color = TextMuted, fontSize = 12.sp)
        if (recurring.isEmpty()) { Text("Quiet calendar.", color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) }
        else { recurring.forEach { exp ->
            val status = when { exp.isPaid -> "Paid"; exp.dueDate < today -> "Past Due"; exp.dueDate == today -> "Today"; else -> "Coming Up" }
            val statusColor = when(status) { "Paid" -> Color.Green; "Past Due" -> Color.Red; "Today" -> GoldOrange; else -> CoralOrange }
            Card(colors = CardDefaults.cardColors(NavySurface), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(exp.name, color = Color.White, fontWeight = FontWeight.Bold); Text("Day ${exp.dueDate}", color = TextMuted, fontSize = 12.sp); Text(status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                    Text("P${exp.amount.toInt()}", color = GoldOrange, fontWeight = FontWeight.Black)
                }
            }
        } }
    }
}

@Composable
fun UpcomingPaymentsListView(viewModel: CompanionViewModel) {
    val recurring by viewModel.recurringExpenses.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        recurring.filter { !it.isPaid }.forEach { UpcomingPaymentRow(it, viewModel) }
        if (recurring.none { !it.isPaid }) { Text("All clear!", color = Color.Green, modifier = Modifier.padding(16.dp)) }
    }
}

@Composable
fun NotificationsView(viewModel: CompanionViewModel) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    Column {
        Text("Smart alerts.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        NotificationToggle("Recurring Reminders", user?.rentReminder ?: true) { viewModel.updateProfile(user!!.copy(rentReminder = it)) }
        NotificationToggle("Limit Alerts", user?.budgetAlerts ?: true) { viewModel.updateProfile(user!!.copy(budgetAlerts = it)) }
        NotificationToggle("Vault Nudges", user?.savingsReminders ?: true) { viewModel.updateProfile(user!!.copy(savingsReminders = it)) }
    }
}

@Composable
fun NotificationToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White); Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = CoralOrange))
    }
}

@Composable
fun ReportsView(viewModel: CompanionViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val categories = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (expenses.isEmpty()) { Icon(Icons.Default.Assessment, null, tint = GoldOrange, modifier = Modifier.size(64.dp)); Text("Insufficient data.", color = TextMuted) }
        else {
            Text("Category Density", color = Color.White, fontWeight = FontWeight.Bold)
            SpendingDonutChart(categories, modifier = Modifier.fillMaxWidth())
            HorizontalDivider(color = NavyPrimary)
            Text("Activity Trend", color = Color.White, fontWeight = FontWeight.Bold)
            SimpleBarChart(data = mapOf("W1" to 400.0, "W2" to 320.0, "W3" to 580.0, "W4" to 200.0), modifier = Modifier.height(150.dp).padding(16.dp))
            Card(colors = CardDefaults.cardColors(NavySurface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) { Text("Efficiency", color = Color.White, fontWeight = FontWeight.Bold); Text("Budget adherence is at 85%. Good job!", color = TextMuted, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun ComingSoonView(feature: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Construction, null, tint = CoralOrange, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("$feature: Development in progress", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

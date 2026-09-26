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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
 * Polished student financial management experience with accurate data.
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
            val isDark = user?.isDarkMode ?: isSystemInDarkTheme()
            
            MyApplicationTheme(darkTheme = isDark) {
                Surface(color = MaterialTheme.colorScheme.background) {
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
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = CoralOrange.copy(alpha = 0.2f),
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
                    .background(MaterialTheme.colorScheme.background)
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

@Composable
fun HomeScreen(viewModel: CompanionViewModel, onAction: (NavigationTab) -> Unit) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val allocations by viewModel.budgetAllocations.collectAsStateWithLifecycle()
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val weeklySpending by viewModel.weeklySpending.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    
    val scrollState = rememberScrollState(initial = viewModel.homeScrollValue)
    LaunchedEffect(scrollState.value) { viewModel.homeScrollValue = scrollState.value }
    
    val context = LocalContext.current
    
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
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Student360Branding.Logo(size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("$greeting, ${user?.firstName ?: "Student"}!", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Your Student360 Dashboard", color = CoralOrange, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            IconButton(onClick = { viewModel.activeMoreModal = "Profile" }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = CoralOrange,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 1. Allowance Gauge
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                AllowanceProgressRing(spent = stats.totalSpent, total = stats.allowance, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text("Remaining Available", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("P${stats.remaining.toInt()}", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("Allowance: ${if(stats.allowance > 0) "P${stats.allowance.toInt()}" else "Not set"}", color = CoralOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Stats Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Allocated", value = "P${stats.allocated.toInt()}", icon = Icons.Default.PieChart, modifier = Modifier.weight(1f))
            StatCard(label = "Committed", value = "P${stats.committed.toInt()}", icon = Icons.Default.Lock, modifier = Modifier.weight(1f))
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Spending Category Analysis", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                SpendingDonutChart(categories = categoryBreakdown, modifier = Modifier.fillMaxWidth())
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Weekly Spending", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                SimpleBarChart(data = weeklySpending, modifier = Modifier.height(150.dp))
            }
        }

        // AI Insight
        val insight by viewModel.dashboardInsight.collectAsStateWithLifecycle()
        Card(colors = CardDefaults.cardColors(containerColor = CoralOrange.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.2f))) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = CoralOrange)
                Spacer(modifier = Modifier.width(16.dp))
                Text(insight, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Quick Actions
        if (stats.transactionCount == 0 && stats.allowance == 0.0) {
            EmptyDashboardActions(onAction = onAction, onUpload = { launcher.launch("*/*") })
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(NavigationTab.BUDGET) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = CoralOrange, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                    Text("Budget Planner", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { onAction(NavigationTab.EXPENSES) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CoralOrange)) {
                    Text("View History", color = CoralOrange, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = CoralOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyDashboardActions(onAction: (NavigationTab) -> Unit, onUpload: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Get started with your finances:", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Button(onClick = { onAction(NavigationTab.BUDGET) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CoralOrange, contentColor = Color.White)) {
            Text("Set Allowance")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction(NavigationTab.EXPENSES) }, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, CoralOrange)) {
                Text("Add Expense", color = CoralOrange)
            }
            OutlinedButton(onClick = onUpload, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, CoralOrange)) {
                Text("Upload Statement", color = CoralOrange)
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

    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Transaction History", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, "Add Expense", tint = CoralOrange) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseActionButton(icon = Icons.Default.CameraAlt, label = "Scan Receipt", modifier = Modifier.weight(1f), onClick = onScanClick)
            ExpenseActionButton(icon = Icons.Default.FileUpload, label = "Import Bank", modifier = Modifier.weight(1f), onClick = { launcher.launch("*/*") })
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (expenses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No transactions recorded yet.", color = TextMuted, fontWeight = FontWeight.Medium) }
        } else {
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(expenses) { expense -> ExpenseCard(expense, onDelete = { viewModel.deleteExpense(expense) }) }
            }
        }
    }

    if (showAddDialog) {
        ExpenseDialog(onDismiss = { showAddDialog = false }, onSave = { m, a, c -> viewModel.addManualExpense(m, a, c); showAddDialog = false })
    }
}

@Composable
fun ExpenseActionButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = modifier.height(64.dp), border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.3f))) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = CoralOrange, modifier = Modifier.size(24.dp))
            Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense, onDelete: () -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), onClick = { showOptions = true }) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(CoralOrange.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = when(expense.category) { "Groceries" -> Icons.Default.ShoppingCart; "Data/WiFi" -> Icons.Default.Wifi; "Transport" -> Icons.Default.DirectionsBus; "Rent" -> Icons.Default.Home; "Food" -> Icons.Default.Restaurant; else -> Icons.Default.Receipt }, contentDescription = null, tint = CoralOrange, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(expense.merchant, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("${expense.date} • ${expense.category}", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Text("P${String.format("%.2f", expense.amount)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to remove this record?") },
            confirmButton = { TextButton(onClick = { onDelete(); showOptions = false }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showOptions = false }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface, titleContentColor = TextPrimary, textContentColor = TextMuted
        )
    }
}

/**
 * BUDGET SCREEN
 */
@Composable
fun BudgetScreen(viewModel: CompanionViewModel) {
    val allocations by viewModel.budgetAllocations.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState(initial = viewModel.budgetScrollValue)
    LaunchedEffect(scrollState.value) { viewModel.budgetScrollValue = scrollState.value }
    
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAllocation by remember { mutableStateOf<BudgetAllocation?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Allowance Allocator", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Monthly Allowance", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                if (viewModel.isEditingAllowance) {
                    OutlinedTextField(value = viewModel.allowanceInput, onValueChange = { viewModel.allowanceInput = it }, modifier = Modifier.fillMaxWidth(), colors = authFieldColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.setAllowance(viewModel.allowanceInput.toDoubleOrNull() ?: 0.0); viewModel.isEditingAllowance = false }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), modifier = Modifier.fillMaxWidth()) { Text("Save Allowance", fontWeight = FontWeight.Bold) }
                } else {
                    Text("P${stats.allowance.toInt()}", color = TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.isEditingAllowance = true }) { Text(if(stats.allowance <= 0) "Set Allowance" else "Edit Allowance", color = CoralOrange, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(16.dp))
                val remainingToAllocate = stats.allowance - stats.allocated
                Text("Allocated: P${stats.allocated.toInt()} | Left: P${remainingToAllocate.toInt()}", color = if(remainingToAllocate < 0) Color.Red else Color.Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (remainingToAllocate < 0) {
                    Text("Warning: Allocations exceed allowance!", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Budget Bricks", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), shape = RoundedCornerShape(8.dp)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Add", fontWeight = FontWeight.Bold) }
        }
        
        if (allocations.isEmpty()) { Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { Text("No budget allocations yet.", color = TextMuted, fontWeight = FontWeight.Medium) } }
        else { allocations.forEach { allocation -> BudgetCategoryIndicator(allocation = allocation, onDelete = { viewModel.deleteAllocation(allocation) }, onEdit = { editingAllocation = allocation }, onAdjust = { viewModel.updateAllocation(allocation.copy(allocatedAmount = it)) }) } }
        
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { scope.launch { val advice = viewModel.optimizeBudget(); Toast.makeText(context, advice, Toast.LENGTH_LONG).show() } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("AI Optimize Budget", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    if (showAddDialog) { AllocationDialog(onDismiss = { showAddDialog = false }, onSave = { n, a, c, r, d -> viewModel.addAllocation(n, a, c, r, d); showAddDialog = false }) }
    if (editingAllocation != null) { AllocationDialog(allocation = editingAllocation, onDismiss = { editingAllocation = null }, onSave = { n, a, c, r, d -> viewModel.updateAllocation(editingAllocation!!.copy(name = n, allocatedAmount = a, category = c, isRecurring = r, dueDate = d)); editingAllocation = null }) }
}

@Composable
fun BudgetCategoryIndicator(allocation: BudgetAllocation, onDelete: () -> Unit, onEdit: () -> Unit, onAdjust: (Double) -> Unit) {
    val progress = if (allocation.allocatedAmount > 0) (allocation.spentAmount / allocation.allocatedAmount).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = allocation.allocatedAmount - allocation.spentAmount
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f).clickable { onEdit() }) { Text(allocation.name, color = TextPrimary, fontWeight = FontWeight.Bold); Text(allocation.category, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    IconButton(onClick = { onAdjust((allocation.allocatedAmount - 50).coerceAtLeast(0.0)) }) { Icon(Icons.Default.Remove, null, tint = CoralOrange) }
                    Text("P${allocation.allocatedAmount.toInt()}", color = TextPrimary, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onAdjust(allocation.allocatedAmount + 50) }) { Icon(Icons.Default.Add, null, tint = CoralOrange) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = if (progress > 0.9f) Color.Red else CoralOrange, trackColor = NavyPrimary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { 
                Text("Used: P${allocation.spentAmount.toInt()}", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = if (remaining >= 0) "P${remaining.toInt()} left" else "P${(-remaining).toInt()} excess", color = if (remaining >= 0) Color.Green else Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold) 
            }
        }
    }
}

@Composable
fun CategoryDropdown(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("Rent", "Transport", "Groceries", "Data/WiFi", "Airtime", "Food", "Education", "Entertainment", "Shopping", "Savings", "Emergency", "Other", "Custom Category")
    var expanded by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }
    var customCategory by remember { mutableStateOf("") }

    Column {
        Text("Category", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Box(modifier = Modifier.fillMaxWidth().clickable { expanded = true }.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)).padding(12.dp)) {
            Text(if (selectedCategory.isEmpty()) "Select Category" else selectedCategory, color = TextPrimary, fontWeight = FontWeight.Medium)
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = MaterialTheme.colorScheme.surface) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat, color = TextPrimary) },
                        onClick = {
                            if (cat == "Custom Category") {
                                showCustomInput = true
                            } else {
                                onCategorySelected(cat)
                                showCustomInput = false
                            }
                            expanded = false
                        }
                    )
                }
            }
        }
        if (showCustomInput) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = customCategory,
                onValueChange = { 
                    customCategory = it
                    onCategorySelected(it)
                },
                label = { Text("Enter Custom Category") },
                colors = authFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ExpenseDialog(onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Groceries") }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Add Expense", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Merchant") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (P)") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                CategoryDropdown(selectedCategory = category, onCategorySelected = { category = it })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, Modifier.weight(1f)) { Text("Cancel", color = TextMuted) }
                    Button(onClick = { onSave(merchant, amount.toDoubleOrNull() ?: 0.0, category) }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun AllocationDialog(allocation: BudgetAllocation? = null, onDismiss: () -> Unit, onSave: (String, Double, String, Boolean, Int?) -> Unit) {
    var name by remember { mutableStateOf(allocation?.name ?: "") }
    var amount by remember { mutableStateOf(allocation?.allocatedAmount?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(allocation?.category ?: "Groceries") }
    var isRecurring by remember { mutableStateOf(allocation?.isRecurring ?: false) }
    var day by remember { mutableStateOf(allocation?.dueDate?.toString() ?: "") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if(allocation == null) "New Allocation" else "Edit Allocation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Expense Name") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (P)") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                
                CategoryDropdown(selectedCategory = category, onCategorySelected = { category = it })
                
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    Checkbox(checked = isRecurring, onCheckedChange = { isRecurring = it }, colors = CheckboxDefaults.colors(checkedColor = CoralOrange))
                    Text("Recurring Monthly Expense", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium) 
                }
                
                if (isRecurring) { 
                    OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Due Day (1-31)") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) ) 
                }
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                    TextButton(onClick = onDismiss, Modifier.weight(1f)) { Text("Cancel", color = TextMuted, fontWeight = FontWeight.Bold) }
                    Button(onClick = { onSave(name, amount.toDoubleOrNull() ?: 0.0, category, isRecurring, day.toIntOrNull()) }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Save", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

/**
 * ORBIT SCREEN
 */
@Composable
fun OrbitScreen(viewModel: CompanionViewModel) {
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val geminiStatus by viewModel.geminiStatus.collectAsStateWithLifecycle()
    val isOrbitConfigured by viewModel.isOrbitConfigured.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    val quickPrompts = listOf("How am I spending?", "Can I afford P200?", "Savings advice", "Budget check", "Need vs Want")
    
    LaunchedEffect(chatMessages.size, isThinking) { 
        if (chatMessages.isNotEmpty() || isThinking) { 
            listState.animateScrollToItem((chatMessages.size - 1 + if(isThinking) 1 else 0).coerceAtLeast(0)) 
        } 
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Orbit AI Advisor", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { viewModel.runGeminiDiagnostic() }) { Icon(Icons.Default.BugReport, "Diagnostic", tint = if(isOrbitConfigured) GoldOrange else Color.Red) }
                IconButton(onClick = { viewModel.clearChat() }) { Icon(Icons.Default.DeleteSweep, "Clear Chat", tint = TextMuted) }
            }
        }

        // Configuration Error / Diagnostic result
        if (!isOrbitConfigured) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Orbit is not configured", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Add GEMINI_API_KEY to local.properties and rebuild the project.", color = TextPrimary, fontSize = 12.sp)
                }
            }
        } else if (geminiStatus != null && geminiStatus != Student360AIService.GeminiStatus.SUCCESS) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldOrange.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text("Diagnostic: $geminiStatus", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = GoldOrange, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(chatMessages) { message ->
                val isAI = message.sender == "AI"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isAI) MaterialTheme.colorScheme.surface else CoralOrange), 
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isAI) 2.dp else 16.dp, bottomEnd = if (isAI) 16.dp else 2.dp), 
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) { 
                        Text(message.text, modifier = Modifier.padding(14.dp), color = if (isAI) TextPrimary else Color.White, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (isThinking) { item { AIThinkingIndicator() } }
        }
        
        LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
            items(quickPrompts) { prompt -> SuggestionChip(onClick = { viewModel.sendMessage(prompt) }, label = { Text(prompt, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium) }, colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, CoralOrange.copy(alpha = 0.3f))) } 
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            OutlinedTextField(
                value = viewModel.orbitMessage, 
                onValueChange = { viewModel.orbitMessage = it }, 
                modifier = Modifier.weight(1f), 
                placeholder = { Text("Ask Orbit anything...", color = TextMuted) }, 
                shape = RoundedCornerShape(28.dp), 
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CoralOrange, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(onClick = { viewModel.sendMessage(viewModel.orbitMessage) }, containerColor = CoralOrange, contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(52.dp)) { Icon(Icons.Default.Send, null) }
        }
    }
}

@Composable
fun AIThinkingIndicator() {
    var dotIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(400); dotIndex = (dotIndex + 1) % 3 } }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp), modifier = Modifier.widthIn(max = 80.dp)) {
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (dotIndex == i) CoralOrange else TextMuted.copy(alpha = 0.5f)))
                }
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
        Text("More Features", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        MoreSectionHeader("Financial Tools")
        MoreItem("Upcoming Payments", Icons.Default.Schedule) { viewModel.activeMoreModal = "Upcoming" }
        MoreItem("Payment Calendar", Icons.Default.DateRange) { viewModel.activeMoreModal = "Calendar" }
        MoreItem("Savings Goals", Icons.Default.Savings) { viewModel.activeMoreModal = "Savings" }
        MoreItem("Financial Reports", Icons.Default.Assessment) { viewModel.activeMoreModal = "Reports" }
        
        MoreSectionHeader("Application")
        MoreItem("Settings", Icons.Default.Settings) { viewModel.activeMoreModal = "Settings" }
        MoreItem("Help & Support", Icons.AutoMirrored.Filled.Help) { viewModel.activeMoreModal = "Help" }
        MoreItem("Terms & Privacy", Icons.Default.Info) { viewModel.activeMoreModal = "Privacy" }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.logout() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BSBDeepNavy, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) { 
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold) 
        }
        Spacer(Modifier.height(40.dp))
    }
    
    viewModel.activeMoreModal?.let { modal ->
        Dialog(onDismissRequest = { viewModel.activeMoreModal = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.activeMoreModal = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary) }
                        Text(modal, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    when(modal) {
                        "Profile" -> ProfileView(viewModel)
                        "Settings" -> SettingsView(viewModel)
                        "Savings" -> SavingsView(viewModel)
                        "Calendar" -> CalendarView(viewModel)
                        "Upcoming" -> UpcomingPaymentsListView(viewModel)
                        "Reports" -> ReportsView(viewModel)
                        "Privacy" -> PrivacyPolicyView()
                        "Security" -> ChangePasswordView(viewModel)
                        else -> ComingSoonView(modal)
                    }
                }
            }
        }
    }
}

@Composable
fun MoreSectionHeader(title: String) {
    Text(title, color = CoralOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
}

@Composable
fun MoreItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = CoralOrange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextMuted)
        }
    }
}

@Composable
fun ProfileView(viewModel: CompanionViewModel) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var institution by remember { mutableStateOf(user?.institution ?: "") }
    var allowance by remember { mutableStateOf(user?.monthlyAllowance?.toInt()?.toString() ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountCircle, null, tint = CoralOrange, modifier = Modifier.size(100.dp))
                Spacer(Modifier.height(12.dp))
                Text("${user?.firstName ?: ""} ${user?.lastName ?: ""}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(user?.email ?: "", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(Modifier.height(12.dp))

        if (isEditing) {
            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = institution, onValueChange = { institution = it }, label = { Text("Institution") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = allowance, onValueChange = { allowance = it }, label = { Text("Monthly Allowance") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            
            Button(onClick = { 
                viewModel.updateProfileDetails(firstName, lastName, email, institution, allowance.toDoubleOrNull() ?: 0.0)
                isEditing = false 
            }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { isEditing = false }, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = TextMuted) }
        } else {
            ProfileField("First Name", user?.firstName ?: "")
            ProfileField("Last Name", user?.lastName ?: "")
            ProfileField("Email Address", user?.email ?: "")
            ProfileField("Institution", user?.institution ?: "")
            ProfileField("Monthly Allowance", "P${user?.monthlyAllowance?.toInt()}")
            
            Button(onClick = { isEditing = true }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { 
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.Bold) 
            }
        }
    }
}

@Composable
fun SettingsView(viewModel: CompanionViewModel) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column {
            Text("Appearance", color = CoralOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Dark Mode", color = TextPrimary, fontWeight = FontWeight.Medium)
                Switch(checked = user?.isDarkMode ?: true, onCheckedChange = { viewModel.updateTheme(it) }, colors = SwitchDefaults.colors(checkedThumbColor = CoralOrange))
            }
        }
        
        Column {
            Text("Notifications", color = CoralOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            NotificationToggle("Payment Reminders", user?.rentReminder ?: true) { viewModel.updateNotificationSetting("rent", it) }
            NotificationToggle("Budget Alerts", user?.budgetAlerts ?: true) { viewModel.updateNotificationSetting("budget", it) }
            NotificationToggle("Savings Reminders", user?.savingsReminders ?: true) { viewModel.updateNotificationSetting("savings", it) }
        }
        
        Column {
            Text("Privacy & Security", color = CoralOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            MoreItemSmall("Change Password", Icons.Default.Lock) { viewModel.activeMoreModal = "Security" }
            MoreItemSmall("Privacy Policy", Icons.Default.Security) { viewModel.activeMoreModal = "Privacy" }
        }
    }
}

@Composable
fun PrivacyPolicyView() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Privacy Policy", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        PrivacySection("Information We Collect", "Student 360 collects account information (name, email), financial information (allowance, expenses), uploaded statements, and application usage data to provide our services.")
        
        PrivacySection("How Information Is Used", "We use your data to display financial summaries, categorise expenses, generate AI insights via Orbit, and provide personalised reminders.")
        
        PrivacySection("Financial Credentials", "Student 360 will NEVER request or store your banking PINs, card PINs, online banking passwords, or card security codes. All bank statement processing is done locally or through secure encrypted channels.")
        
        PrivacySection("Data Security", "We implement secure storage using encrypted local databases and secure communication protocols. Your financial data is protected and never sold to third parties.")
        
        PrivacySection("User Control", "You can manage, edit, or delete your transaction history and profile information at any time through the Settings and Profile sections.")
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column {
        Text(title, color = CoralOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(content, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun ChangePasswordView(viewModel: CompanionViewModel) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = currentPassword, 
            onValueChange = { currentPassword = it }, 
            label = { Text("Current Password") },
            visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { showCurrent = !showCurrent }) { Icon(if(showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
            modifier = Modifier.fillMaxWidth(),
            colors = authFieldColors()
        )
        OutlinedTextField(
            value = newPassword, 
            onValueChange = { newPassword = it }, 
            label = { Text("New Password") },
            visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { showNew = !showNew }) { Icon(if(showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
            modifier = Modifier.fillMaxWidth(),
            colors = authFieldColors()
        )
        OutlinedTextField(
            value = confirmPassword, 
            onValueChange = { confirmPassword = it }, 
            label = { Text("Confirm New Password") },
            visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = authFieldColors()
        )
        
        Button(
            onClick = {
                if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                } else if (newPassword != confirmPassword) {
                    Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                } else if (newPassword.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else if (newPassword == currentPassword) {
                    Toast.makeText(context, "New password cannot be the same as old", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.changePassword(currentPassword, newPassword) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        if (success) viewModel.activeMoreModal = null
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Update Password", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MoreItemSmall(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        HorizontalDivider(color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
fun NotificationToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium); Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = CoralOrange))
    }
}

@Composable
fun SavingsView(viewModel: CompanionViewModel) {
    val goals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { showAdd = true }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { 
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Create Savings Goal", fontWeight = FontWeight.Bold) 
        }
        
        if (goals.isEmpty()) { 
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("You haven't created a savings goal yet.", color = TextMuted, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium) 
            }
        } else { 
            goals.forEach { goal ->
                Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                            Text(goal.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("P${goal.targetAmount.toInt()}", color = GoldOrange, fontWeight = FontWeight.Bold) 
                        }
                        val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = GoldOrange, trackColor = NavyPrimary)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("P${goal.currentAmount.toInt()} saved", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("${(progress * 100).toInt()}%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = { viewModel.updateSavingsGoal(goal.copy(currentAmount = goal.currentAmount + 100)) }, colors = ButtonDefaults.buttonColors(BSBDeepNavy, contentColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Text("Add P100 to Goal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } 
        }
    }
    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
                var name by remember { mutableStateOf("") }; var target by remember { mutableStateOf("") }
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("New Savings Goal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal Name") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target Amount (P)") }, colors = authFieldColors(), modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Button(onClick = { viewModel.addSavingsGoal(name, target.toDoubleOrNull() ?: 0.0); showAdd = false }, colors = ButtonDefaults.buttonColors(CoralOrange, contentColor = Color.White), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Create Goal", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun CalendarView(viewModel: CompanionViewModel) {
    val recurring by viewModel.recurringExpenses.collectAsStateWithLifecycle()
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (recurring.isEmpty()) { 
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No upcoming payments in your calendar.", color = TextMuted, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium) 
            }
        } else { 
            recurring.forEach { exp ->
                val status = when { exp.isPaid -> "Paid"; exp.dueDate < today -> "Overdue"; exp.dueDate == today -> "Due Today"; else -> "Upcoming" }
                val statusColor = when(status) { "Paid" -> Color.Green; "Overdue" -> Color.Red; "Due Today" -> GoldOrange; else -> CoralOrange }
                Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) { 
                            Text(exp.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Due: Day ${exp.dueDate} of month", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Surface(color = statusColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        Text("P${exp.amount.toInt()}", color = GoldOrange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            } 
        }
    }
}

@Composable
fun UpcomingPaymentsListView(viewModel: CompanionViewModel) {
    val recurring by viewModel.recurringExpenses.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
        val pending = recurring.filter { !it.isPaid }
        if (pending.isEmpty()) { 
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("All clear! No upcoming payments.", color = Color.Green, fontWeight = FontWeight.Bold) 
            }
        } else {
            pending.forEach { UpcomingPaymentRow(it, viewModel) }
        }
    }
}

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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("P${expense.amount.toInt()} • Due Day ${expense.dueDate}", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                if (status == "Overdue") {
                    Text("Overdue", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = { MockPaymentService.initiatePayment(context, expense.name, expense.amount) { viewModel.markRecurringAsPaid(expense) } },
                enabled = !expense.isPaid,
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrange, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pay Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReportsView(viewModel: CompanionViewModel) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (stats.transactionCount == 0) { 
            Icon(Icons.Default.Assessment, null, tint = TextMuted, modifier = Modifier.size(80.dp))
            Text("No financial data available for reports yet.", color = TextMuted, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium) 
        } else {
            Text("Monthly Financial Summary", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReportRow("Allowance", "P${stats.allowance.toInt()}")
                    ReportRow("Total Spent", "P${stats.totalSpent.toInt()}")
                    ReportRow("Budget Allocated", "P${stats.allocated.toInt()}")
                    ReportRow("Committed Bills", "P${stats.committed.toInt()}")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ReportRow("Net Remaining", "P${stats.remaining.toInt()}", isBold = true, valueColor = Color.Green)
                }
            }
            
            Text("Spending Distribution", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            SpendingDonutChart(categoryBreakdown, modifier = Modifier.fillMaxWidth())
            
            Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("AI Analysis", color = CoralOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Based on your current spending, you are tracking well against your allowance. Consider moving P${(stats.remaining * 0.2).toInt()} into your savings goal to accelerate your progress.", color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ReportRow(label: String, value: String, isBold: Boolean = false, valueColor: Color = TextPrimary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextMuted, fontSize = 14.sp, fontWeight = if(isBold) FontWeight.Bold else FontWeight.Medium)
        Text(value, color = if(valueColor == TextPrimary) TextPrimary else valueColor, fontSize = 16.sp, fontWeight = if(isBold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
fun ComingSoonView(feature: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Construction, null, tint = CoralOrange, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("$feature: Coming Soon", color = Color.White, fontWeight = FontWeight.Bold)
            Text("We are working hard to bring this feature to you.", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

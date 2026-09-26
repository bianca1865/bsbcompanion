package com.example.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * STUDENT360 - Central System ViewModel
 * Requirement: State preservation across pages and visual dashboard data calculation.
 * Updated to handle accurate financial data and integrated with BsbStudentService.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompanionViewModel(private val repository: Repository) : ViewModel() {

    private val aiService = Student360AIService(repository)
    private val bsbService: BsbStudentService = BsbStudentServiceImpl(repository)

    // --- BSB SERVICE INTEGRATION ---
    private val _simulatedDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    val simulatedDay = _simulatedDay.asStateFlow()

    private val _freeDataMode = MutableStateFlow(true)
    val freeDataMode = _freeDataMode.asStateFlow()

    private val _roundUpSavingsEnabled = MutableStateFlow(false)
    val roundUpSavingsEnabled = _roundUpSavingsEnabled.asStateFlow()

    val studentProfile: StateFlow<StudentProfile> = bsbService.getStudentProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfile())

    val financialSummary: StateFlow<FinancialSummary> = _simulatedDay.flatMapLatest { day ->
        bsbService.getFinancialSummary(day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, 30, 0.0, PacingStatus.ON_PACE, 85, "Good", "Keep it up"))

    val categorySpends: StateFlow<List<CategorySpendItem>> = bsbService.getCategorySpends()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val committedBills: StateFlow<List<CommittedBillItem>> = _simulatedDay.flatMapLatest { day ->
        bsbService.getCommittedBills(day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiInsights: StateFlow<List<FinancialInsight>> = _simulatedDay.flatMapLatest { day ->
        bsbService.getAiInsights(day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CORE DATA FLOWS ---
    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Also provide List<ExpenseItem> for components that expect it
    val expenseItems: StateFlow<List<ExpenseItem>> = repository.expenses.map { list ->
        list.map { ExpenseItem(it.merchant, it.amount, it.category, it.timestamp) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringExpenses: StateFlow<List<RecurringExpense>> = repository.recurringExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetAllocations: StateFlow<List<BudgetAllocation>> = repository.budgetAllocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dashboardInsight = MutableStateFlow("Welcome! Let's get your finances organised.")
    val dashboardInsight: StateFlow<String> = _dashboardInsight.asStateFlow()

    // Mapping for UI components that expect StudentSavingsGoal
    val studentSavingsGoals: StateFlow<List<StudentSavingsGoal>> = repository.savingsGoals.map { list ->
        list.map { goal ->
            StudentSavingsGoal(
                id = goal.id.toString(),
                title = goal.name,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                iconEmoji = "🎯",
                targetMonth = goal.deadline ?: "Dec 2026"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACCURATE FINANCIAL FIGURES ---
    val dashboardStats = combine(userProfile, expenses, budgetAllocations, recurringExpenses, savingsGoals) { profile, exps, allocs, recurrings, goals ->
        val allowance = profile?.monthlyAllowance ?: 0.0
        val totalSpent = exps.sumOf { it.amount }
        val allocated = allocs.sumOf { it.allocatedAmount }
        val committed = recurrings.filter { !it.isPaid }.sumOf { it.amount }
        val savings = goals.sumOf { it.currentAmount }
        val remaining = (allowance - totalSpent - committed).coerceAtLeast(0.0)
        
        DashboardStats(
            allowance = allowance,
            totalSpent = totalSpent,
            allocated = allocated,
            committed = committed,
            remaining = remaining,
            savings = savings,
            transactionCount = exps.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // --- WEEKLY ANALYTICS ---
    val weeklySpending: StateFlow<List<WeeklyDataPoint>> = expenses.map { list ->
        calculateWeeklyData(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun calculateWeeklyData(expenses: List<Expense>): List<WeeklyDataPoint> {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.firstDayOfWeek = Calendar.SUNDAY
        
        // Reset to start of current week (Sunday 00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        val weekStart = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val weekEnd = calendar.timeInMillis
        
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dailyTotals = MutableList(7) { 0.0 }
        
        expenses.filter { it.timestamp in weekStart until weekEnd }.forEach { exp ->
            calendar.timeInMillis = exp.timestamp
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val index = dayOfWeek - 1
            if (index in 0..6) {
                dailyTotals[index] += exp.amount
            }
        }
        
        return days.mapIndexed { index, name ->
            WeeklyDataPoint(name, dailyTotals[index])
        }
    }

    val categoryBreakdown: StateFlow<Map<String, Double>> = expenses.map { list ->
        list.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- UI PERSISTENCE STATE ---
    var homeScrollValue by mutableStateOf(0)
    var expensesScrollIndex by mutableStateOf(0)
    var expensesScrollOffset by mutableStateOf(0)
    var budgetScrollValue by mutableStateOf(0)
    var isEditingAllowance by mutableStateOf(false)
    var allowanceInput by mutableStateOf("")
    var orbitScrollIndex by mutableStateOf(0)
    var orbitScrollOffset by mutableStateOf(0)
    var orbitMessage by mutableStateOf("")
    var activeMoreModal by mutableStateOf<String?>(null)
    var moreScrollValue by mutableStateOf(0)

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    // --- GEMINI DIAGNOSTIC STATE ---
    private val _geminiStatus = MutableStateFlow<Student360AIService.GeminiStatus?>(null)
    val geminiStatus = _geminiStatus.asStateFlow()

    val isOrbitConfigured = MutableStateFlow(aiService.isApiKeyConfigured())

    init {
        viewModelScope.launch {
            repository.initializeApp()
            combine(userProfile, expenses, budgetAllocations) { profile, exp, alloc ->
                Triple(profile, exp, alloc)
            }.collect { (profile, exp, alloc) ->
                updateDashboardInsight(profile, exp, alloc)
                if (allowanceInput.isEmpty() && profile != null) {
                    allowanceInput = profile.monthlyAllowance.toInt().toString()
                }
            }
        }
        viewModelScope.launch {
            val messages = repository.chatMessages.first()
            if (messages.isEmpty()) {
                repository.addChatMessage("AI", "Hi! I'm Student360 Orbit. How can I help you manage your finances today!")
            }
        }
    }

    fun runGeminiDiagnostic() {
        viewModelScope.launch {
            _isThinking.value = true
            _geminiStatus.value = aiService.testGeminiConnection()
            isOrbitConfigured.value = aiService.isApiKeyConfigured()
            _isThinking.value = false
        }
    }

    fun sendMessage(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || _isThinking.value) return
        
        viewModelScope.launch {
            val currentHistory = repository.chatMessages.first().map { it.sender to it.text }
            repository.addChatMessage("User", trimmed)
            orbitMessage = ""
            _isThinking.value = true
            val response = aiService.generateResponse(trimmed, currentHistory)
            repository.addChatMessage("AI", response)
            _isThinking.value = false
            // Update configuration status in case it was fixed
            isOrbitConfigured.value = aiService.isApiKeyConfigured()
        }
    }

    private fun updateDashboardInsight(profile: UserProfile?, exp: List<Expense>, alloc: List<BudgetAllocation>) {
        val allowance = profile?.monthlyAllowance ?: 0.0
        val spent = exp.sumOf { it.amount }
        val allocated = alloc.sumOf { it.allocatedAmount }

        _dashboardInsight.value = when {
            allowance <= 0 -> "Set your monthly allowance to start planning your budget."
            spent == 0.0 && alloc.isEmpty() -> "Welcome ${profile?.firstName ?: "Student"}! Let's get your finances organised."
            spent > allowance -> "Warning: You've spent more than your allowance."
            else -> "You're doing great!"
        }
    }

    // --- SERVICE ACTIONS ---
    fun advanceSimulatedDay() { _simulatedDay.value = (_simulatedDay.value % 30) + 1 }
    fun toggleFreeDataMode() { _freeDataMode.value = !_freeDataMode.value }
    fun toggleRoundUpSavings() { _roundUpSavingsEnabled.value = !_roundUpSavingsEnabled.value }

    fun logStudentExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            bsbService.logQuickExpense(title, amount, category)
        }
    }

    fun deleteExpenseItem(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
    
    fun deleteExpenseItem(item: ExpenseItem) {
        viewModelScope.launch {
            val exp = repository.expenses.first().find { it.merchant == item.title && it.amount == item.amount && it.timestamp == item.timestamp }
            if (exp != null) repository.deleteExpense(exp)
        }
    }

    fun simulatePaydayDeposit(amount: Double) {
        viewModelScope.launch { bsbService.simulateAllowanceDeposit(amount) }
    }

    fun toggleBillRingFence(billId: Int, isCurrentlyRingFenced: Boolean) {
        viewModelScope.launch { bsbService.setRingFenced(billId, !isCurrentlyRingFenced) }
    }

    fun settleBillNow(billId: Int) {
        viewModelScope.launch { bsbService.settleBillNow(billId) }
    }

    fun transferToSesameSavings(amount: Double) {
        viewModelScope.launch { bsbService.transferToSavings(amount) }
    }

    fun addCommittedBill(title: String, amount: Double, dueDay: Int, category: String) {
        viewModelScope.launch { bsbService.addCommittedBill(title, amount, dueDay, category) }
    }

    fun rebalanceCategoryBudget(categoryName: String, newBudget: Double) {
        viewModelScope.launch { bsbService.rebalanceCategoryBudget(categoryName, newBudget) }
    }

    fun batchImportStatementTransactions(transactions: List<StatementTransaction>) {
        viewModelScope.launch {
            transactions.filter { it.isSelected }.forEach { tx ->
                bsbService.logQuickExpense(tx.description, tx.amount, tx.category)
            }
        }
    }

    fun importOcrReceipt(receipt: ScannedOcrReceipt) {
        viewModelScope.launch {
            bsbService.logQuickExpense(receipt.merchant, receipt.totalAmount, receipt.category)
        }
    }

    fun applyFullAllowanceAllocation(preset: AllowanceAllocationPreset) {
        viewModelScope.launch {
            bsbService.rebalanceCategoryBudget("Rent & Accommodation", preset.rent)
            bsbService.rebalanceCategoryBudget("Food & Meals", preset.food)
            bsbService.rebalanceCategoryBudget("Transport & Kombi", preset.transport)
            bsbService.rebalanceCategoryBudget("Study Materials", preset.study)
            bsbService.rebalanceCategoryBudget("Savings Reserve", preset.savings)
        }
    }

    fun simulateNotificationAlert(title: String, message: String) {
        // Placeholder
    }

    fun executeBsbPayment(payee: String, amount: Double, ref: String, category: String, token: String?, callback: (BsbPaymentReceipt) -> Unit) {
        viewModelScope.launch {
            val receipt = BsbPaymentReceipt(
                referenceNumber = "BSB-${(100000..999999).random()}",
                payeeName = payee,
                amount = amount,
                fromAccount = "10243950621",
                tokenCode = token
            )
            bsbService.logQuickExpense("Paid: $payee", amount, category)
            callback(receipt)
        }
    }

    fun depositToSavingsGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            val goal = repository.savingsGoals.first().find { it.id.toString() == goalId }
            if (goal != null) {
                repository.updateGoal(goal.copy(currentAmount = goal.currentAmount + amount))
                bsbService.transferToSavings(amount)
            }
        }
    }

    // --- REPOSITORY INTERACTION ---
    fun setAllowance(amount: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(monthlyAllowance = amount))
        }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(isDarkMode = isDark))
        }
    }

    fun updateNotificationSetting(type: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = when(type) {
                "rent" -> current.copy(rentReminder = enabled)
                "budget" -> current.copy(budgetAlerts = enabled)
                "savings" -> current.copy(savingsReminders = enabled)
                else -> current
            }
            repository.updateProfile(updated)
        }
    }

    fun signup(firstName: String, lastName: String, email: String, password: String, institution: String, allowance: Double) {
        viewModelScope.launch {
            repository.updateProfile(UserProfile(id = 1, firstName = firstName, lastName = lastName, email = email, password = password, institution = institution, monthlyAllowance = allowance, isLoggedIn = true, hasCompletedOnboarding = true))
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val profile = repository.userProfile.first()
            if (profile != null && profile.email == email && profile.password == password) {
                repository.setLoginStatus(true)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun logout() { viewModelScope.launch { repository.setLoginStatus(false) } }
    
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch { repository.updateProfile(profile) }
    }

    fun updateProfileDetails(firstName: String, lastName: String, email: String, institution: String, allowance: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(
                firstName = firstName,
                lastName = lastName,
                email = email,
                institution = institution,
                monthlyAllowance = allowance
            ))
        }
    }

    fun changePassword(current: String, new: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.password != current) {
                onResult(false, "Current password is incorrect")
            } else {
                repository.updateProfile(profile.copy(password = new))
                onResult(true, "Password changed successfully")
            }
        }
    }

    fun addManualExpense(merchant: String, amount: Double, category: String) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(now.time)
            repository.addExpense(Expense(merchant = merchant, amount = amount, category = category, date = dateStr, type = "Manual", timestamp = now.timeInMillis))
        }
    }

    fun processScannedReceipt(merchant: String, amount: Double, date: String, category: String) {
        viewModelScope.launch {
            repository.addExpense(Expense(merchant = merchant, amount = amount, category = category, date = date, type = "Scan", timestamp = System.currentTimeMillis()))
        }
    }

    fun processUploadedFile(uri: Uri) {
        viewModelScope.launch {
            addManualExpense("Uploaded Statement", 120.0, "Groceries")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun addAllocation(name: String, amount: Double, category: String, isRecurring: Boolean, dueDate: Int?) {
        viewModelScope.launch {
            repository.insertAllocation(BudgetAllocation(name = name, category = category, allocatedAmount = amount, isRecurring = isRecurring, dueDate = dueDate))
            if (isRecurring && dueDate != null) repository.addRecurring(RecurringExpense(name = name, amount = amount, dueDate = dueDate, category = category))
        }
    }

    fun updateAllocation(allocation: BudgetAllocation) {
        viewModelScope.launch {
            repository.updateAllocation(allocation)
            if (allocation.isRecurring && allocation.dueDate != null) {
                val existing = recurringExpenses.value.find { it.name == allocation.name }
                if (existing != null) repository.updateRecurring(existing.copy(amount = allocation.allocatedAmount, dueDate = allocation.dueDate, category = allocation.category))
                else repository.addRecurring(RecurringExpense(name = allocation.name, amount = allocation.allocatedAmount, dueDate = allocation.dueDate, category = allocation.category))
            }
        }
    }

    fun deleteAllocation(allocation: BudgetAllocation) {
        viewModelScope.launch {
            repository.deleteAllocation(allocation)
            val recurring = recurringExpenses.value.find { it.name == allocation.name }
            if (recurring != null) repository.deleteRecurring(recurring)
        }
    }

    fun markRecurringAsPaid(expense: RecurringExpense) {
        viewModelScope.launch {
            repository.updateRecurring(expense.copy(isPaid = true))
            addManualExpense(expense.name, expense.amount, expense.category)
        }
    }

    fun addSavingsGoal(name: String, target: Double) { viewModelScope.launch { repository.insertGoal(SavingsGoal(name = name, targetAmount = target)) } }
    fun addSavingsGoal(title: String, target: Double, emoji: String, month: String) {
        viewModelScope.launch { repository.insertGoal(SavingsGoal(name = title, targetAmount = target, deadline = month)) }
    }
    fun updateSavingsGoal(goal: SavingsGoal) { viewModelScope.launch { repository.updateGoal(goal) } }
    fun deleteSavingsGoal(goal: SavingsGoal) { viewModelScope.launch { repository.deleteGoal(goal) } }
    fun clearChat() { viewModelScope.launch { repository.clearChat() } }
    suspend fun optimizeBudget(): String = aiService.getBudgetOptimizationAdvice()
    suspend fun generateAIResponse(query: String): String = aiService.generateResponse(query)
}

data class DashboardStats(
    val allowance: Double = 0.0,
    val totalSpent: Double = 0.0,
    val allocated: Double = 0.0,
    val committed: Double = 0.0,
    val remaining: Double = 0.0,
    val savings: Double = 0.0,
    val transactionCount: Int = 0
)

data class WeeklyDataPoint(
    val day: String,
    val amount: Double
)

class CompanionViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanionViewModel::class.java)) return CompanionViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

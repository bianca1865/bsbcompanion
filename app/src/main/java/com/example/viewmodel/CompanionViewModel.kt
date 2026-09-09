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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * STUDENT360 - Central System ViewModel
 * Requirement: State preservation across pages and visual dashboard data calculation.
 */
class CompanionViewModel(private val repository: Repository) : ViewModel() {

    private val aiService = Student360AIService(repository)

    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringExpenses: StateFlow<List<RecurringExpense>> = repository.recurringExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetAllocations: StateFlow<List<BudgetAllocation>> = repository.budgetAllocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _dashboardInsight = MutableStateFlow("Welcome! Let's get your finances organised.")
    val dashboardInsight: StateFlow<String> = _dashboardInsight.asStateFlow()

    // --- DASHBOARD DATA (Real-time calculations for visuals) ---

    val weeklySpending: StateFlow<Map<String, Double>> = expenses.map { list ->
        val weekMap = mutableMapOf("Week 1" to 0.0, "Week 2" to 0.0, "Week 3" to 0.0, "Week 4" to 0.0)
        list.forEach { exp ->
            val day = exp.date.split(" ").firstOrNull()?.toIntOrNull() ?: 1
            when {
                day <= 7 -> weekMap["Week 1"] = weekMap["Week 1"]!! + exp.amount
                day <= 14 -> weekMap["Week 2"] = weekMap["Week 2"]!! + exp.amount
                day <= 21 -> weekMap["Week 3"] = weekMap["Week 3"]!! + exp.amount
                else -> weekMap["Week 4"] = weekMap["Week 4"]!! + exp.amount
            }
        }
        weekMap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapOf("Week 1" to 0.0, "Week 2" to 0.0, "Week 3" to 0.0, "Week 4" to 0.0))

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

    val chatMessages = mutableStateListOf<Pair<String, String>>()
    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

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
        if (chatMessages.isEmpty()) {
            chatMessages.add("AI" to "Hi! I'm Student360 Orbit. How can I help you manage your finances today!")
        }
    }

    fun sendMessage(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || _isThinking.value) return
        chatMessages.add("User" to trimmed)
        orbitMessage = ""
        viewModelScope.launch {
            _isThinking.value = true
            val response = aiService.generateResponse(trimmed)
            kotlinx.coroutines.delay(5000)
            chatMessages.add("AI" to response)
            _isThinking.value = false
        }
    }

    private fun updateDashboardInsight(profile: UserProfile?, exp: List<Expense>, alloc: List<BudgetAllocation>) {
        val allowance = profile?.monthlyAllowance ?: 0.0
        val spent = exp.sumOf { it.amount }
        val allocated = alloc.sumOf { it.allocatedAmount }

        _dashboardInsight.value = when {
            allowance <= 0 -> "Let's start by setting up your allowance and adding your first expense."
            spent == 0.0 && alloc.isEmpty() -> "Welcome ${profile?.firstName}! You have P$allowance to plan. Add your first budget allocation."
            spent > allowance -> "Warning: You've spent more than your allowance by P${spent - allowance}."
            spent > (allowance * 0.9) -> "Careful! You've used over 90% of your allowance."
            allocated > allowance -> "Your planned allocations exceed your allowance. Use 'Optimise' to fix it."
            else -> "You have P${(allowance - spent).toInt()} remaining. You're doing great!"
        }
    }

    // --- REPOSITORY INTERACTION ---
    fun setAllowance(amount: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(monthlyAllowance = amount))
        }
    }

    fun signup(firstName: String, lastName: String, email: String, password: String, institution: String, allowance: Double) {
        viewModelScope.launch {
            repository.updateProfile(UserProfile(id = 1, firstName = firstName, lastName = lastName, email = email, password = password, institution = institution, monthlyAllowance = allowance, isLoggedIn = true, hasCompletedOnboarding = false))
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
    fun updateProfile(profile: UserProfile) { viewModelScope.launch { repository.updateProfile(profile) } }
    fun addManualExpense(merchant: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.addExpense(Expense(merchant = merchant, amount = amount, category = category, date = "${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)} ${getMonthName()}", type = "Manual"))
            updateSpentInAllocation(category, amount)
        }
    }

    private fun getMonthName(): String = when(Calendar.getInstance().get(Calendar.MONTH)) {
        Calendar.JANUARY -> "Jan"; Calendar.FEBRUARY -> "Feb"; Calendar.MARCH -> "Mar"
        Calendar.APRIL -> "Apr"; Calendar.MAY -> "May"; Calendar.JUNE -> "Jun"
        Calendar.JULY -> "Jul"; Calendar.AUGUST -> "Aug"; Calendar.SEPTEMBER -> "Sep"
        Calendar.OCTOBER -> "Oct"; Calendar.NOVEMBER -> "Nov"; Calendar.DECEMBER -> "Dec"
        else -> ""
    }

    private fun updateSpentInAllocation(category: String, amount: Double) {
        viewModelScope.launch {
            val current = budgetAllocations.value.find { it.category.equals(category, ignoreCase = true) }
            if (current != null) repository.updateAllocation(current.copy(spentAmount = current.spentAmount + amount))
        }
    }

    fun processScannedReceipt(merchant: String, amount: Double, date: String, category: String) {
        viewModelScope.launch {
            repository.addExpense(Expense(merchant = merchant, amount = amount, category = category, date = date, type = "Scan"))
            updateSpentInAllocation(category, amount)
        }
    }

    fun processUploadedFile(uri: Uri) {
        viewModelScope.launch {
            addManualExpense("Statement Upload", 450.0, "Groceries")
            addManualExpense("Utility Bill", 150.0, "Data/WiFi")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            val current = budgetAllocations.value.find { it.category == expense.category }
            if (current != null) repository.updateAllocation(current.copy(spentAmount = (current.spentAmount - expense.amount).coerceAtLeast(0.0)))
        }
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
    fun updateSavingsGoal(goal: SavingsGoal) { viewModelScope.launch { repository.updateGoal(goal) } }
    fun deleteSavingsGoal(goal: SavingsGoal) { viewModelScope.launch { repository.deleteGoal(goal) } }
    suspend fun optimizeBudget(): String = aiService.getBudgetOptimizationAdvice()
    suspend fun generateAIResponse(query: String): String = aiService.generateResponse(query)
}

class CompanionViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanionViewModel::class.java)) return CompanionViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

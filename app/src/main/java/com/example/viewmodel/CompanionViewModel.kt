package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class CompanionViewModel(private val repository: Repository) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringExpenses: StateFlow<List<RecurringExpense>> = repository.recurringExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetAllocations: StateFlow<List<BudgetAllocation>> = repository.budgetAllocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loggedInUser: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    private fun updateSpentAmount(category: String, amount: Double) {
        viewModelScope.launch {
            val currentAllocations = budgetAllocations.value
            val allocation = currentAllocations.find { it.category.equals(category, ignoreCase = true) }
            if (allocation != null) {
                repository.updateAllocation(allocation.copy(spentAmount = allocation.spentAmount + amount))
            } else {
                repository.updateAllocation(BudgetAllocation(category, 500.0, amount, false))
            }
        }
    }

    fun addManualExpense(merchant: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    merchant = merchant,
                    amount = amount,
                    category = category,
                    date = "Today",
                    type = "Manual"
                )
            )
            updateSpentAmount(category, amount)
        }
    }

    fun processScannedReceipt(merchant: String, amount: Double, date: String, category: String) {
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    merchant = merchant,
                    amount = amount,
                    category = category,
                    date = date,
                    type = "Scan"
                )
            )
            updateSpentAmount(category, amount)
        }
    }

    fun processUploadedFile(uri: Uri) {
        viewModelScope.launch {
            val merchant = "Detected Merchant"
            val amount = 250.0
            val category = "Shopping"
            repository.addExpense(
                Expense(
                    merchant = merchant,
                    amount = amount,
                    date = "06 Sep",
                    category = category,
                    type = "Statement"
                )
            )
            updateSpentAmount(category, amount)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            val currentAllocations = budgetAllocations.value
            val allocation = currentAllocations.find { it.category.equals(expense.category, ignoreCase = true) }
            if (allocation != null) {
                repository.updateAllocation(allocation.copy(spentAmount = (allocation.spentAmount - expense.amount).coerceAtLeast(0.0)))
            }
        }
    }

    fun updateProfile(name: String, allowance: Double) {
        viewModelScope.launch {
            val current = loggedInUser.value ?: UserProfile()
            repository.updateProfile(current.copy(fullName = name, monthlyAllowance = allowance))
        }
    }

    fun updateAllocation(category: String, amount: Double, isEssential: Boolean) {
        viewModelScope.launch {
            val current = budgetAllocations.value.find { it.category == category }
            repository.updateAllocation(BudgetAllocation(
                category = category, 
                allocatedAmount = amount, 
                spentAmount = current?.spentAmount ?: 0.0,
                isEssential = isEssential
            ))
        }
    }
    
    fun addRecurring(name: String, amount: Double, day: Int, category: String) {
        viewModelScope.launch {
            repository.addRecurring(RecurringExpense(name = name, amount = amount, dueDate = day, category = category))
        }
    }

    fun deleteRecurring(recurring: RecurringExpense) {
        viewModelScope.launch {
            repository.deleteRecurring(recurring)
        }
    }

    fun markRecurringAsPaid(recurring: RecurringExpense) {
        viewModelScope.launch {
            repository.updateRecurring(recurring.copy(isPaid = true))
            addManualExpense(recurring.name, recurring.amount, recurring.category)
        }
    }

    fun addSavingsGoal(name: String, target: Double) {
        viewModelScope.launch {
            repository.addSavingsGoal(SavingsGoal(name, target))
        }
    }

    fun depositToSavings(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            // Logic to transfer from allowance/available balance to savings
            repository.addSavingsGoal(goal.copy(currentAmount = goal.currentAmount + amount))
            addManualExpense("Savings: ${goal.name}", amount, "Savings")
        }
    }
}

class CompanionViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompanionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

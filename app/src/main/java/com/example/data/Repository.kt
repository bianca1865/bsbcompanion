package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    private val expenseDao = db.expenseDao()
    private val recurringExpenseDao = db.recurringExpenseDao()
    private val budgetAllocationDao = db.budgetAllocationDao()
    private val savingsGoalDao = db.savingsGoalDao()
    private val userProfileDao = db.userProfileDao()

    val expenses: Flow<List<Expense>> = expenseDao.getAllExpensesFlow()
    val recurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAllRecurringFlow()
    val budgetAllocations: Flow<List<BudgetAllocation>> = budgetAllocationDao.getAllAllocationsFlow()
    val savingsGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoalsFlow()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()

    suspend fun addExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    suspend fun clearAllExpenses() = expenseDao.clearAllExpenses()

    suspend fun addRecurring(expense: RecurringExpense) = recurringExpenseDao.insertRecurring(expense)
    suspend fun updateRecurring(expense: RecurringExpense) = recurringExpenseDao.updateRecurring(expense)
    suspend fun deleteRecurring(expense: RecurringExpense) = recurringExpenseDao.deleteRecurring(expense)

    suspend fun updateAllocation(allocation: BudgetAllocation) = budgetAllocationDao.insertAllocation(allocation)
    suspend fun clearAllocations() = budgetAllocationDao.clearAll()

    suspend fun addSavingsGoal(goal: SavingsGoal) = savingsGoalDao.insertGoal(goal)
    suspend fun updateProfile(profile: UserProfile) = userProfileDao.updateProfile(profile)

    suspend fun seedDatabaseIfEmpty() {
        val profile = userProfileDao.getUserProfileFlow().first()
        if (profile == null) {
            userProfileDao.updateProfile(UserProfile(fullName = "Student User", monthlyAllowance = 2200.0))
            
            // Seed Budget Allocations
            budgetAllocationDao.insertAllocation(BudgetAllocation("Rent", 800.0, 800.0, true))
            budgetAllocationDao.insertAllocation(BudgetAllocation("Groceries", 450.0, 420.0, false))
            budgetAllocationDao.insertAllocation(BudgetAllocation("Transport", 250.0, 180.0, false))
            budgetAllocationDao.insertAllocation(BudgetAllocation("Data/WiFi", 150.0, 100.0, false))
            budgetAllocationDao.insertAllocation(BudgetAllocation("Savings", 300.0, 0.0, false))
            budgetAllocationDao.insertAllocation(BudgetAllocation("Entertainment", 100.0, 0.0, false))
            budgetAllocationDao.insertAllocation(BudgetAllocation("Emergency", 150.0, 0.0, false))

            // Seed Recurring Expenses
            recurringExpenseDao.insertRecurring(RecurringExpense(name = "Rent", amount = 800.0, dueDate = 1, category = "Rent"))
            recurringExpenseDao.insertRecurring(RecurringExpense(name = "Data/WiFi", amount = 100.0, dueDate = 5, category = "Data/WiFi"))
            recurringExpenseDao.insertRecurring(RecurringExpense(name = "Netflix", amount = 95.0, dueDate = 15, category = "Entertainment"))

            // Seed Demo Expenses
            expenseDao.insertExpense(Expense(merchant = "Choppies", amount = 126.50, date = "05 Sep", category = "Groceries", type = "Scan"))
            expenseDao.insertExpense(Expense(merchant = "Orange", amount = 95.0, date = "04 Sep", category = "Data/WiFi", type = "E-Receipt"))
            expenseDao.insertExpense(Expense(merchant = "KFC", amount = 85.0, date = "03 Sep", category = "Food & Takeaways", type = "Manual"))
        }
    }
}

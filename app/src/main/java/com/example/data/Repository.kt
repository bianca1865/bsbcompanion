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

    suspend fun insertAllocation(allocation: BudgetAllocation) = budgetAllocationDao.insertAllocation(allocation)
    suspend fun updateAllocation(allocation: BudgetAllocation) = budgetAllocationDao.updateAllocation(allocation)
    suspend fun deleteAllocation(allocation: BudgetAllocation) = budgetAllocationDao.deleteAllocation(allocation)
    suspend fun clearAllocations() = budgetAllocationDao.clearAll()

    suspend fun insertGoal(goal: SavingsGoal) = savingsGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: SavingsGoal) = savingsGoalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: SavingsGoal) = savingsGoalDao.deleteGoal(goal)

    suspend fun updateProfile(profile: UserProfile) = userProfileDao.updateProfile(profile)
    suspend fun setLoginStatus(isLoggedIn: Boolean) = userProfileDao.setLoginStatus(isLoggedIn)

    suspend fun initializeApp() {
        val profile = userProfileDao.getUserProfileFlow().first()
        if (profile == null) {
            // New user, create empty profile
            userProfileDao.updateProfile(UserProfile(id = 1, isLoggedIn = false, hasCompletedOnboarding = false))
        }
    }
}

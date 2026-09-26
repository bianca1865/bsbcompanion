package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    private val expenseDao = db.expenseDao()
    private val recurringExpenseDao = db.recurringExpenseDao()
    private val budgetAllocationDao = db.budgetAllocationDao()
    private val savingsGoalDao = db.savingsGoalDao()
    private val userProfileDao = db.userProfileDao()
    private val chatMessageDao = db.chatMessageDao()

    val expenses: Flow<List<Expense>> = expenseDao.getAllExpensesFlow()
    val recurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAllRecurringFlow()
    val budgetAllocations: Flow<List<BudgetAllocation>> = budgetAllocationDao.getAllAllocationsFlow()
    val savingsGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoalsFlow()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()
    val chatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessagesFlow()

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

    suspend fun addChatMessage(sender: String, text: String) {
        chatMessageDao.insertMessage(ChatMessage(sender = sender, text = text))
    }

    suspend fun clearChat() = chatMessageDao.clearChat()

    suspend fun initializeApp() {
        val profile = userProfileDao.getUserProfileFlow().first()
        if (profile == null) {
            // New user, create empty profile
            userProfileDao.updateProfile(UserProfile(id = 1, isLoggedIn = false, hasCompletedOnboarding = false))
        }
    }
}

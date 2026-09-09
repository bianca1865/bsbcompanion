package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY dueDate ASC")
    fun getAllRecurringFlow(): Flow<List<RecurringExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(expense: RecurringExpense): Long

    @Update
    suspend fun updateRecurring(expense: RecurringExpense)

    @Delete
    suspend fun deleteRecurring(expense: RecurringExpense)
}

@Dao
interface BudgetAllocationDao {
    @Query("SELECT * FROM budget_allocations")
    fun getAllAllocationsFlow(): Flow<List<BudgetAllocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: BudgetAllocation)

    @Update
    suspend fun updateAllocation(allocation: BudgetAllocation)

    @Delete
    suspend fun deleteAllocation(allocation: BudgetAllocation)

    @Query("DELETE FROM budget_allocations")
    suspend fun clearAll()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals")
    fun getAllGoalsFlow(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal)

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET isLoggedIn = :isLoggedIn WHERE id = 1")
    suspend fun setLoginStatus(isLoggedIn: Boolean)
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchant: String,
    val amount: Double,
    val date: String,
    val category: String,
    val type: String = "Manual", // "Manual", "Scan", "Statement", "E-Receipt"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val dueDate: Int, // Day of month
    val category: String,
    val frequency: String = "Monthly",
    val isPaid: Boolean = false
)

@Entity(tableName = "budget_allocations")
data class BudgetAllocation(
    @PrimaryKey val category: String,
    val allocatedAmount: Double,
    val spentAmount: Double = 0.0,
    val isEssential: Boolean = false
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Student",
    val monthlyAllowance: Double = 2200.0,
    val isDarkMode: Boolean = true
)

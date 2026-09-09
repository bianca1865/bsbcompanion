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
    val isPaid: Boolean = false,
    val isRecurring: Boolean = true
)

@Entity(tableName = "budget_allocations")
data class BudgetAllocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val allocatedAmount: Double,
    val spentAmount: Double = 0.0,
    val dueDate: Int? = null,
    val isRecurring: Boolean = false,
    val isEssential: Boolean = false
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: String? = null
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "", // Basic placeholder for local auth
    val institution: String = "",
    val monthlyAllowance: Double = 0.0,
    val isLoggedIn: Boolean = false,
    val isDarkMode: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val rentReminder: Boolean = true,
    val budgetAlerts: Boolean = true,
    val savingsReminders: Boolean = true
)

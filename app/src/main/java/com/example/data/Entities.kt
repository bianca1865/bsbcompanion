package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "bsb_accounts")
data class BSBAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountName: String,
    val accountNumber: String,
    val balance: Double
)

@Entity(
    tableName = "bsb_cards",
    foreignKeys = [
        ForeignKey(
            entity = BSBAccount::class,
            parentColumns = ["id"],
            childColumns = ["linkedAccountId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BSBCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardHolder: String,
    val cardNumberMasked: String,
    val cardExpiry: String,
    val linkedAccountId: Int,
    val cardType: String = "Student Card"
)

@Entity(tableName = "scheduled_payments")
data class ScheduledPayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val paymentType: String, // "Savings Account", "Wifi", "Mobile Subscription", "Rent", "Other"
    val payeeName: String,   // e.g. "BTC Broadband", "Mascom Online", "Netflix", "Education Savings"
    val amount: Double,
    val paymentDay: Int,     // 1 - 31
    val lastPaymentDate: Long? = null, // timestamp
    val selectedAccountId: Int, // Paid from this BSB account
    val selectedCardId: Int? = null, // Optionally linked card
    val isActive: Boolean = true,
    val recipientAccount: String? = null,
    val recipientBranchNumber: String? = null,
    val recipientBranchName: String? = null,
    val recipientName: String? = null
)

@Entity(tableName = "expense_items")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String, // "Savings", "Wifi", "Mobile Subscription", "Other Outflow"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "registered_users")
data class RegisteredUser(
    @PrimaryKey val email: String,
    val fullName: String,
    val cellphone: String,
    val cardNumber: String,
    val cardExpiry: String,
    val cardCvvOrPin: String,
    val passwordHash: String,
    val biometricsEnabled: Boolean = false,
    val dailyCardLimit: Double = 5000.0,
    val smsAlertsEnabled: Boolean = true,
    val isCardFrozen: Boolean = false,
    val contactlessEnabled: Boolean = true,
    val statementFrequency: String = "Monthly",
    val isDarkMode: Boolean = true,
    // Allocator Bounds (Max Limits)
    val foodMaxLimit: Double = 1500.0,
    val rentMaxLimit: Double = 3000.0,
    val transportMaxLimit: Double = 1000.0,
    val savingsMaxLimit: Double = 2000.0,
    val wifiMaxLimit: Double = 1000.0,
    val mobileMaxLimit: Double = 1000.0,
    // Current Allocations (Persistent State)
    val foodAlloc: Double = 1000.0,
    val rentAlloc: Double = 700.0,
    val transportAlloc: Double = 250.0,
    val savingsAlloc: Double = 250.0,
    val wifiAlloc: Double = 0.0,
    val mobileAlloc: Double = 0.0,
    val totalAllowanceLimit: Double = 2200.0,
    // Visible Components in Allocator (Comma separated categories)
    val visibleCategories: String = "Groceries,Rent,Transport,Savings"
)

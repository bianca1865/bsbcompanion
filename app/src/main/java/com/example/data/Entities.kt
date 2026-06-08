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
    val paymentType: String, // "Savings Account", "Wifi", "Mobile Subscription", "Other"
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
    val isDarkMode: Boolean = true
)

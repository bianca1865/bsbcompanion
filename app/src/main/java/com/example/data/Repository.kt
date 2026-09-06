package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    private val accountDao = db.accountDao()
    private val cardDao = db.cardDao()
    private val paymentDao = db.paymentDao()
    private val expenseDao = db.expenseDao()
    private val notificationDao = db.notificationDao()
    private val userDao = db.userDao()

    val accounts: Flow<List<BSBAccount>> = accountDao.getAllAccountsFlow()
    val cards: Flow<List<BSBCard>> = cardDao.getAllCardsFlow()
    val payments: Flow<List<ScheduledPayment>> = paymentDao.getAllPaymentsFlow()
    val expenses: Flow<List<ExpenseItem>> = expenseDao.getAllExpensesFlow()
    val notifications: Flow<List<AppNotification>> = notificationDao.getAllNotificationsFlow()
    val registeredUsers: Flow<List<RegisteredUser>> = userDao.getAllRegisteredUsers()

    suspend fun getFirstUser(): RegisteredUser? = userDao.getFirstUser()
    suspend fun getUserByEmail(email: String): RegisteredUser? = userDao.getUserByEmail(email)

    suspend fun registerUser(user: RegisteredUser) {
        userDao.insertUser(user)
    }

    suspend fun clearUser() {
        userDao.clearUser()
    }

    suspend fun getAccountById(id: Int): BSBAccount? = accountDao.getAccountById(id)

    suspend fun addAccount(account: BSBAccount): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: BSBAccount) {
        accountDao.updateAccount(account)
    }

    suspend fun addCard(card: BSBCard): Long {
        return cardDao.insertCard(card)
    }

    suspend fun addPayment(payment: ScheduledPayment): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun updatePayment(payment: ScheduledPayment) {
        paymentDao.updatePayment(payment)
    }

    suspend fun deletePayment(payment: ScheduledPayment) {
        paymentDao.deletePayment(payment)
    }

    suspend fun addExpense(expense: ExpenseItem): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseItem) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun clearAllExpenses() {
        expenseDao.clearAllExpenses()
    }

    suspend fun addNotification(title: String, message: String) {
        val lowerTitle = title.lowercase()
        val lowerMsg = message.lowercase()
        
        val isPayment = lowerTitle.contains("payment") || lowerTitle.contains("paid") || lowerTitle.contains("bill") || lowerTitle.contains("rule") || lowerTitle.contains("cancelled") || lowerMsg.contains("payment") || lowerMsg.contains("paid") || lowerMsg.contains("bill")
        val isDeposit = lowerTitle.contains("deposit") || lowerTitle.contains("allowance") || lowerTitle.contains("received") || lowerTitle.contains("added") || lowerMsg.contains("deposit") || lowerMsg.contains("allowance") || lowerMsg.contains("received") || lowerMsg.contains("credit")
        val isWithdrawal = lowerTitle.contains("withdraw") || lowerTitle.contains("deducted") || lowerTitle.contains("outflow") || lowerMsg.contains("withdraw") || lowerMsg.contains("deducted") || lowerMsg.contains("outflow")
        val isTransaction = lowerTitle.contains("purchase") || lowerTitle.contains("declined") || lowerTitle.contains("approved") || lowerTitle.contains("blocked") || lowerTitle.contains("expense") || lowerTitle.contains("transaction") || lowerMsg.contains("purchase") || lowerMsg.contains("declined") || lowerMsg.contains("approved") || lowerMsg.contains("blocked") || lowerMsg.contains("expense") || lowerMsg.contains("transaction")

        if (isPayment || isDeposit || isWithdrawal || isTransaction) {
            notificationDao.insertNotification(
                AppNotification(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun markNotificationAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearNotifications() {
        notificationDao.clearAll()
    }

    // Seed realistic Student 360 data if empty
    suspend fun seedDatabaseIfEmpty() {
        val existingAccounts = accountDao.getAllAccountsDirect()
        if (existingAccounts.isEmpty()) {
            Log.d("Repository", "Seeding initial app demo data...")

            // No default bank account is created anymore — the app focuses on receipt/statement analytics
            // Seed a few scheduled payments (no linked account) to demonstrate reminders and analytics
            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Wifi",
                    payeeName = "Campus Wifi Subscription",
                    amount = 149.00,
                    paymentDay = 15,
                    selectedAccountId = null,
                    isActive = true
                )
            )

            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Mobile Subscription",
                    payeeName = "Student Data Plan",
                    amount = 99.00,
                    paymentDay = 5,
                    selectedAccountId = null,
                    isActive = true
                )
            )

            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Rent",
                    payeeName = "Shared Accommodation Rent",
                    amount = 1200.00,
                    paymentDay = 1,
                    selectedAccountId = null,
                    isActive = true
                )
            )

            // Seed Expense Entries (demo)
            expenseDao.insertExpense(
                ExpenseItem(
                    title = "Mascom Student Data",
                    amount = 99.00,
                    category = "Mobile Subscription",
                    timestamp = System.currentTimeMillis() - 86400000_1L // 1 day ago
                )
            )

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "University Bookstore (Study Guides)",
                    amount = 250.00,
                    category = "Study Materials",
                    timestamp = System.currentTimeMillis() - 86400000_2L // 2 days ago
                )
            )

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "Combi Ride to Campus",
                    amount = 14.50,
                    category = "Transport",
                    timestamp = System.currentTimeMillis() - 86400000_3L // 3 days ago
                )
            )

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "Campus Cafeteria Combo",
                    amount = 45.00,
                    category = "Groceries",
                    timestamp = System.currentTimeMillis() - 86400000_4L
                )
            )

            // Optionally ensure there is at least one registered user (local device profile) so UI has settings
            val existingUser = userDao.getFirstUser()
            if (existingUser == null) {
                userDao.insertUser(
                    RegisteredUser(
                        email = "local@device",
                        fullName = "Local Student",
                        cellphone = "", cardNumber = "", cardExpiry = "", cardCvvOrPin = "",
                        passwordHash = "", biometricsEnabled = false
                    )
                )
            }

            // Seed Initial Notifications
        }
    }

    /**
     * Executes automatic payments listed for the specific day of the month.
     * Checks balances, subtracts the amount, logs expense, and triggers notifications.
     */
    suspend fun executePaymentsForDay(dayOfMonth: Int): Int {
        var paymentsExecuted = 0
        val allPayments = paymentDao.getAllPaymentsDirect()
        val duePayments = allPayments.filter { it.paymentDay == dayOfMonth && it.isActive }

        for (payment in duePayments) {
            // For analytics-first behavior we don't touch bank balances here. Instead we record the occurrence
            // as an expense entry and create a reminder/notification that a scheduled payment would have been executed.

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "Scheduled: ${payment.payeeName}",
                    amount = payment.amount,
                    category = when (payment.paymentType) {
                        "Savings Account" -> "Savings"
                        "Wifi" -> "Wifi"
                        "Mobile Subscription" -> "Mobile Subscription"
                        "Rent" -> "Rent"
                        else -> "Other Outflow"
                    }
                )
            )

            // Mark last payment date
            paymentDao.updatePayment(payment.copy(lastPaymentDate = System.currentTimeMillis()))

            // Notify the user — generic message (no account-specific wording)
            notificationDao.insertNotification(
                AppNotification(
                    title = "Reminder: Scheduled Payment",
                    message = "A scheduled payment of P${String.format("%.2f", payment.amount)} for ${payment.payeeName} is due/processed for day $dayOfMonth.",
                    timestamp = System.currentTimeMillis()
                )
            )

            paymentsExecuted++
        }
        return paymentsExecuted
    }
}

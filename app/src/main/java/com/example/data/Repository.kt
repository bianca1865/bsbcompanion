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

    // Seed realistic Botswana Savings Bank data if empty
    suspend fun seedDatabaseIfEmpty() {
        val existingAccounts = accountDao.getAllAccountsDirect()
        if (existingAccounts.isEmpty()) {
            Log.d("Repository", "Seeding initial BSB companion data...")
            
            // Seed BSB Accounts (BWP - Botswana Pula)
            val acc1Id = accountDao.insertAccount(
                BSBAccount(
                    accountName = "BSB Student Allowance",
                    accountNumber = "10243950621",
                    balance = 2200.00
                )
            ).toInt()

            val acc3Id = accountDao.insertAccount(
                BSBAccount(
                    accountName = "Sesame Smart Youth Savings",
                    accountNumber = "30591248560",
                    balance = 150.00
                )
            ).toInt()

            // Seed Cards linked to accounts
            cardDao.insertCard(
                BSBCard(
                    cardHolder = "Masego L. Kaelo",
                    cardNumberMasked = "**** **** **** 5678",
                    cardExpiry = "10/29",
                    linkedAccountId = acc1Id,
                    cardType = "Student Card"
                )
            )

            cardDao.insertCard(
                BSBCard(
                    cardHolder = "Masego L. Kaelo",
                    cardNumberMasked = "**** **** **** 1111",
                    cardExpiry = "09/31",
                    linkedAccountId = acc3Id,
                    cardType = "Youth Card"
                )
            )

            // Seed Scheduled Payments
            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Wifi",
                    payeeName = "Mascom Campus Wifi Pack",
                    amount = 149.00,
                    paymentDay = 15,
                    selectedAccountId = acc1Id,
                    isActive = true
                )
            )

            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Mobile Subscription",
                    payeeName = "Orange Student Data Plus",
                    amount = 99.00,
                    paymentDay = 5,
                    selectedAccountId = acc1Id,
                    isActive = true
                )
            )

            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Savings",
                    payeeName = "Emergency Reserve Saver",
                    amount = 200.00,
                    paymentDay = 25,
                    selectedAccountId = acc1Id,
                    isActive = true
                )
            )

            // Seed Expense Entries
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
                    title = "Kombi Ride to Campus",
                    amount = 14.50,
                    category = "Transport",
                    timestamp = System.currentTimeMillis() - 86400000_3L // 3 days ago
                )
            )

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "Campus Cafeteria Combo",
                    amount = 45.00,
                    category = "Food",
                    timestamp = System.currentTimeMillis() - 86400000_4L
                )
            )

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
            val account = accountDao.getAccountById(payment.selectedAccountId)
            if (account != null) {
                if (account.balance >= payment.amount) {
                    // Update balance
                    val updatedAccount = account.copy(balance = account.balance - payment.amount)
                    accountDao.updateAccount(updatedAccount)

                    // Log expense
                    expenseDao.insertExpense(
                        ExpenseItem(
                            title = "Auto Paid & Link: ${payment.payeeName}",
                            amount = payment.amount,
                            category = when (payment.paymentType) {
                                "Savings Account" -> "Savings"
                                "Wifi" -> "Wifi"
                                "Mobile Subscription" -> "Mobile Subscription"
                                else -> "Other Outflow"
                            }
                        )
                    )

                    // Update payment's last billing date
                    paymentDao.updatePayment(payment.copy(lastPaymentDate = System.currentTimeMillis()))

                    // Log notification
                    notificationDao.insertNotification(
                        AppNotification(
                            title = "Auto-Payment Done: P${String.format("%.2f", payment.amount)}",
                            message = "Success: P${String.format("%.2f", payment.amount)} was sent to ${payment.payeeName} from ${account.accountName} (${account.accountNumber.takeLast(4)}).",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    paymentsExecuted++
                } else {
                    // Insufficient funds notification
                    notificationDao.insertNotification(
                        AppNotification(
                            title = "Payment Failed: Insufficient Funds",
                            message = "Declined: Could not pay P${String.format("%.2f", payment.amount)} to ${payment.payeeName} from ${account.accountName} due to low balance.",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                // Invalid account linked
                notificationDao.insertNotification(
                    AppNotification(
                        title = "Payment Failed: Account Missing",
                        message = "Declined: The source BSB Account for paying ${payment.payeeName} is no longer active.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        return paymentsExecuted
    }
}

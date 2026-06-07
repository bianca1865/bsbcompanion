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
        notificationDao.insertNotification(
            AppNotification(
                title = title,
                message = message,
                timestamp = System.currentTimeMillis()
            )
        )
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
                    accountName = "BSB Ordinary Savings",
                    accountNumber = "10243950621",
                    balance = 12500.00
                )
            ).toInt()

            val acc2Id = accountDao.insertAccount(
                BSBAccount(
                    accountName = "Save-As-You-Earn Plan",
                    accountNumber = "20485769123",
                    balance = 4800.00
                )
            ).toInt()

            val acc3Id = accountDao.insertAccount(
                BSBAccount(
                    accountName = "BSB Sesame Smart Account",
                    accountNumber = "30591248560",
                    balance = 1650.00
                )
            ).toInt()

            // Seed Cards linked to accounts
            cardDao.insertCard(
                BSBCard(
                    cardHolder = "Masego L. Kaelo",
                    cardNumberMasked = "**** **** **** 5678",
                    cardExpiry = "10/29",
                    linkedAccountId = acc1Id,
                    cardType = "Platinum Black Card"
                )
            )

            cardDao.insertCard(
                BSBCard(
                    cardHolder = "Masego L. Kaelo",
                    cardNumberMasked = "**** **** **** 3412",
                    cardExpiry = "05/30",
                    linkedAccountId = acc2Id,
                    cardType = "Visa Classic Debit Card"
                )
            )

            cardDao.insertCard(
                BSBCard(
                    cardHolder = "Masego L. Kaelo",
                    cardNumberMasked = "**** **** **** 1111",
                    cardExpiry = "09/31",
                    linkedAccountId = acc3Id,
                    cardType = "Youth Debit Card"
                )
            )

            // Seed Scheduled Payments
            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Wifi Subscription",
                    payeeName = "Mascom MyHome Wifi",
                    amount = 450.00,
                    paymentDay = 15,
                    selectedAccountId = acc1Id,
                    isActive = true
                )
            )

            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Mobile Subscription",
                    payeeName = "Orange SuperData Plus",
                    amount = 175.00,
                    paymentDay = 5,
                    selectedAccountId = acc1Id,
                    isActive = true
                )
            )

            paymentDao.insertPayment(
                ScheduledPayment(
                    paymentType = "Savings Account Pot",
                    payeeName = "Emergency Reserve Saver",
                    amount = 1000.00,
                    paymentDay = 25,
                    selectedAccountId = acc1Id,
                    isActive = true
                )
            )

            // Seed Expense Entries
            expenseDao.insertExpense(
                ExpenseItem(
                    title = "BTC Broadband Fibers",
                    amount = 350.00,
                    category = "Wifi",
                    timestamp = System.currentTimeMillis() - 86400000_1L // 1 day ago
                )
            )

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "Mascom Airtime Bunches",
                    amount = 120.00,
                    category = "Mobile Subscription",
                    timestamp = System.currentTimeMillis() - 86400000_2L // 2 days ago
                )
            )

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "Monthly Savings Accumulation",
                    amount = 1200.00,
                    category = "Savings",
                    timestamp = System.currentTimeMillis() - 86400000_3L // 3 days ago
                )
            )

            expenseDao.insertExpense(
                ExpenseItem(
                    title = "General Groceries Choppies",
                    amount = 750.00,
                    category = "Other Outflow",
                    timestamp = System.currentTimeMillis() - 86400000_4L
                )
            )

            // Seed Initial Notifications
            notificationDao.insertNotification(
                AppNotification(
                    title = "Companion Setup Successful",
                    message = "Your Botswana Savings Bank Companion App is configured. Linked with ordinary and savings wallets.",
                    timestamp = System.currentTimeMillis() - 86400000,
                    isRead = true
                )
            )
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

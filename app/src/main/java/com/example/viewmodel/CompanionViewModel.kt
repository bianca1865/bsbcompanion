package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class CompanionViewModel(private val repository: Repository) : ViewModel() {

    // Main streams
    val accounts: StateFlow<List<BSBAccount>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cards: StateFlow<List<BSBCard>> = repository.cards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<ScheduledPayment>> = repository.payments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseItem>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.notifications
        .map { list ->
            list.filter { notif ->
                val lowerTitle = notif.title.lowercase()
                val lowerMsg = notif.message.lowercase()
                
                val isPayment = lowerTitle.contains("payment") || lowerTitle.contains("paid") || lowerTitle.contains("bill") || lowerTitle.contains("rule") || lowerTitle.contains("cancelled") || lowerMsg.contains("payment") || lowerMsg.contains("paid") || lowerMsg.contains("bill")
                val isDeposit = lowerTitle.contains("deposit") || lowerTitle.contains("allowance") || lowerTitle.contains("received") || lowerTitle.contains("added") || lowerMsg.contains("deposit") || lowerMsg.contains("allowance") || lowerMsg.contains("received") || lowerMsg.contains("credit")
                val isWithdrawal = lowerTitle.contains("withdraw") || lowerTitle.contains("deducted") || lowerTitle.contains("outflow") || lowerMsg.contains("withdraw") || lowerMsg.contains("deducted") || lowerMsg.contains("outflow")
                val isTransaction = lowerTitle.contains("purchase") || lowerTitle.contains("declined") || lowerTitle.contains("approved") || lowerTitle.contains("blocked") || lowerTitle.contains("expense") || lowerTitle.contains("transaction") || lowerMsg.contains("purchase") || lowerMsg.contains("declined") || lowerMsg.contains("approved") || lowerMsg.contains("blocked") || lowerMsg.contains("expense") || lowerMsg.contains("transaction")

                isPayment || isDeposit || isWithdrawal || isTransaction
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated Calendar Day (set to current day)
    private val _simulatedDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    val simulatedDay: StateFlow<Int> = _simulatedDay.asStateFlow()

    // Free Data zero-rating status (Monetization network mode)
    private val _freeDataMode = MutableStateFlow(true)
    val freeDataMode: StateFlow<Boolean> = _freeDataMode.asStateFlow()

    val registeredUsers: StateFlow<List<RegisteredUser>> = repository.registeredUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loggedInUser = MutableStateFlow<RegisteredUser?>(null)
    val loggedInUser: StateFlow<RegisteredUser?> = _loggedInUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _pendingApproval = MutableStateFlow<PendingPurchaseApproval?>(null)
    val pendingApproval: StateFlow<PendingPurchaseApproval?> = _pendingApproval.asStateFlow()

    // Triggered status to notify user in immediate snackbar of payments executed
    private val _paymentExecutionEvent = MutableSharedFlow<String>(replay = 0)
    val paymentExecutionEvent: SharedFlow<String> = _paymentExecutionEvent.asSharedFlow()

    // Student360 BSB Service Layer
    val studentService: BsbStudentService = BsbStudentServiceImpl(repository)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val financialSummary: StateFlow<FinancialSummary> = _simulatedDay
        .flatMapLatest { day -> studentService.getFinancialSummary(day) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            FinancialSummary(
                monthlyAllowance = 2200.0,
                totalSpent = 408.50,
                totalCommitted = 448.0,
                totalSavings = 200.0,
                remainingFreeToSpend = 1143.50,
                daysRemainingInCycle = 15,
                safeDailySpend = 76.23,
                pacingStatus = PacingStatus.ON_PACE,
                healthScore = 84,
                healthGrade = "Healthy Pacing",
                healthAdvice = "Allowance is pacing sustainably for the remaining days."
            )
        )

    val categorySpends: StateFlow<List<CategorySpendItem>> = studentService.getCategorySpends()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val committedBills: StateFlow<List<CommittedBillItem>> = _simulatedDay
        .flatMapLatest { day -> studentService.getCommittedBills(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val aiInsights: StateFlow<List<FinancialInsight>> = _simulatedDay
        .flatMapLatest { day -> studentService.getAiInsights(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentProfile: StateFlow<StudentProfile> = studentService.getStudentProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfile())

    fun logStudentExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            studentService.logQuickExpense(title, amount, category)
            _paymentExecutionEvent.emit("Logged expense: $title (-P${String.format("%.2f", amount)})")
        }
    }

    fun simulatePaydayDeposit(amount: Double = 2200.0) {
        viewModelScope.launch {
            studentService.simulateAllowanceDeposit(amount)
            _paymentExecutionEvent.emit("DTEF Allowance Credited (+P${String.format("%.2f", amount)})")
        }
    }

    private val _roundUpSavingsEnabled = MutableStateFlow(true)
    val roundUpSavingsEnabled: StateFlow<Boolean> = _roundUpSavingsEnabled.asStateFlow()

    fun toggleRoundUpSavings() {
        _roundUpSavingsEnabled.value = !_roundUpSavingsEnabled.value
        viewModelScope.launch {
            val status = if (_roundUpSavingsEnabled.value) "enabled" else "paused"
            _paymentExecutionEvent.emit("BSB Smart Round-Up Savings $status.")
        }
    }

    fun toggleBillRingFence(billId: Int, isCurrentlyRingFenced: Boolean) {
        viewModelScope.launch {
            studentService.setRingFenced(billId, !isCurrentlyRingFenced)
            val msg = if (!isCurrentlyRingFenced) "Obligation ring-fenced & protected." else "Obligation protection released."
            _paymentExecutionEvent.emit(msg)
        }
    }

    fun addCommittedBill(title: String, amount: Double, dueDay: Int, category: String) {
        viewModelScope.launch {
            studentService.addCommittedBill(title, amount, dueDay, category)
            _paymentExecutionEvent.emit("Added obligation: $title (P${String.format("%.2f", amount)})")
        }
    }

    fun settleBillNow(billId: Int) {
        viewModelScope.launch {
            studentService.settleBillNow(billId)
            _paymentExecutionEvent.emit("Committed bill executed and paid successfully!")
        }
    }

    fun transferToSesameSavings(amount: Double) {
        viewModelScope.launch {
            studentService.transferToSavings(amount)
            _paymentExecutionEvent.emit("Saved P${String.format("%.2f", amount)} into BSB Sesame Savings!")
        }
    }

    fun rebalanceCategoryBudget(categoryName: String, newBudget: Double) {
        viewModelScope.launch {
            studentService.rebalanceCategoryBudget(categoryName, newBudget)
            _paymentExecutionEvent.emit("$categoryName envelope updated to P${String.format("%.2f", newBudget)}")
        }
    }

    fun deleteExpenseItem(expense: ExpenseItem) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _paymentExecutionEvent.emit("Removed expense: ${expense.title}")
        }
    }

    // PRIORITY 8: Savings Goals State
    private val _savingsGoals = MutableStateFlow(
        listOf(
            StudentSavingsGoal("g1", "Laptop for CompSci", 3500.0, 1250.0, "💻", "Dec 2026"),
            StudentSavingsGoal("g2", "Emergency Cushion", 1000.0, 500.0, "🛡️", "Nov 2026"),
            StudentSavingsGoal("g3", "Semester Textbooks", 600.0, 420.0, "📚", "Oct 2026"),
            StudentSavingsGoal("g4", "Graduation Attire", 1200.0, 300.0, "🎓", "May 2027")
        )
    )
    val savingsGoals: StateFlow<List<StudentSavingsGoal>> = _savingsGoals.asStateFlow()

    fun depositToSavingsGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            _savingsGoals.update { goals ->
                goals.map { goal ->
                    if (goal.id == goalId) {
                        goal.copy(currentAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount))
                    } else goal
                }
            }
            studentService.transferToSavings(amount)
            val goalTitle = _savingsGoals.value.find { it.id == goalId }?.title ?: "Goal"
            _paymentExecutionEvent.emit("Deposited P${String.format("%.2f", amount)} into $goalTitle!")
            repository.addNotification("Savings Goal Boosted", "Transferred P${String.format("%.2f", amount)} from allowance into '$goalTitle' (Sesame Savings).")
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, iconEmoji: String, targetMonth: String) {
        val newGoal = StudentSavingsGoal(
            id = "g_${System.currentTimeMillis()}",
            title = title,
            targetAmount = targetAmount,
            currentAmount = 0.0,
            iconEmoji = iconEmoji,
            targetMonth = targetMonth
        )
        _savingsGoals.update { it + newGoal }
        viewModelScope.launch {
            _paymentExecutionEvent.emit("Created new savings goal: $title (Target: P${String.format("%.0f", targetAmount)})")
        }
    }

    // PRIORITY 2: Statement Batch Import
    fun batchImportStatementTransactions(transactions: List<StatementTransaction>) {
        viewModelScope.launch {
            val selected = transactions.filter { it.isSelected }
            if (selected.isEmpty()) return@launch

            var importedCount = 0
            var totalExpenseImported = 0.0

            selected.forEach { tx ->
                if (!tx.isCredit) {
                    repository.addExpense(
                        ExpenseItem(
                            title = tx.description,
                            amount = tx.amount,
                            category = tx.category,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    totalExpenseImported += tx.amount
                    importedCount++
                }
            }

            repository.addNotification(
                "Statement Imported",
                "Successfully imported $importedCount transactions from statement totaling -P${String.format("%.2f", totalExpenseImported)}."
            )
            _paymentExecutionEvent.emit("Imported $importedCount statement transactions into BSB records!")
        }
    }

    // PRIORITY 3: Physical Receipt OCR Import
    fun importOcrReceipt(receipt: ScannedOcrReceipt) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseItem(
                    title = "${receipt.merchant} (OCR Scanned)",
                    amount = receipt.totalAmount,
                    category = receipt.category,
                    timestamp = System.currentTimeMillis()
                )
            )
            repository.addNotification(
                "Receipt Scanned & Verified",
                "OCR captured P${String.format("%.2f", receipt.totalAmount)} at ${receipt.merchant}. Categorized under ${receipt.category}."
            )
            _paymentExecutionEvent.emit("Receipt from ${receipt.merchant} verified and added to expenses!")
        }
    }

    // PRIORITY 4: P2,200 Allowance Allocator Application
    fun applyFullAllowanceAllocation(preset: AllowanceAllocationPreset) {
        viewModelScope.launch {
            studentService.rebalanceCategoryBudget("Rent & Accommodation", preset.rent)
            studentService.rebalanceCategoryBudget("Food & Meals", preset.food)
            studentService.rebalanceCategoryBudget("Transport & Kombi", preset.transport)
            studentService.rebalanceCategoryBudget("Study Materials", preset.study)
            studentService.rebalanceCategoryBudget("Savings Reserve", preset.savings)

            repository.addNotification(
                "Allowance Allocator Applied",
                "Applied '${preset.name}' P2,200 plan: Food P${preset.food.toInt()}, Rent P${preset.rent.toInt()}, Transport P${preset.transport.toInt()}, Study P${preset.study.toInt()}, Savings P${preset.savings.toInt()}."
            )
            _paymentExecutionEvent.emit("Locked P2,200 '${preset.name}' budget envelopes!")
        }
    }

    // PRIORITY 6: Trigger Simulated Local Notification
    fun simulateNotificationAlert(title: String, message: String) {
        viewModelScope.launch {
            repository.addNotification(title, message)
            _paymentExecutionEvent.emit("🔔 Alert: $title")
        }
    }

    // PRIORITY 7: Execute BSB Payment Flow
    fun executeBsbPayment(
        payeeName: String,
        amount: Double,
        reference: String,
        category: String,
        tokenCode: String? = null,
        onSuccess: (BsbPaymentReceipt) -> Unit
    ) {
        viewModelScope.launch {
            val allAccounts = repository.accounts.first()
            val primaryAcc = allAccounts.find { it.accountName.contains("Allowance", ignoreCase = true) }
                ?: allAccounts.firstOrNull()

            if (primaryAcc != null) {
                val newBal = (primaryAcc.balance - amount).coerceAtLeast(0.0)
                repository.updateAccount(primaryAcc.copy(balance = newBal))
            }

            repository.addExpense(
                ExpenseItem(
                    title = "BSB Pay: $payeeName",
                    amount = amount,
                    category = category,
                    timestamp = System.currentTimeMillis()
                )
            )

            val refNumber = "BSB-${(100000..999999).random()}-${System.currentTimeMillis().toString().takeLast(4)}"
            val receipt = BsbPaymentReceipt(
                referenceNumber = refNumber,
                payeeName = payeeName,
                amount = amount,
                fromAccount = primaryAcc?.accountNumber ?: "10243950621",
                timestamp = System.currentTimeMillis(),
                tokenCode = tokenCode,
                fee = 0.00
            )

            repository.addNotification(
                "BSB Payment Successful",
                "Paid P${String.format("%.2f", amount)} to $payeeName (Ref: $refNumber). ${if (tokenCode != null) "Token: $tokenCode" else ""}"
            )
            _paymentExecutionEvent.emit("Payment to $payeeName successful! Ref: $refNumber")
            onSuccess(receipt)
        }
    }

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Load the first registered user if present
            val user = repository.getFirstUser()
            _loggedInUser.value = user
            // Auth removed: default the app to 'logged in' so signup/login screens are skipped
            _isLoggedIn.value = true
        }
    }

    fun updateUserSettings(
        dailyCardLimit: Double,
        smsAlertsEnabled: Boolean,
        isCardFrozen: Boolean,
        contactlessEnabled: Boolean,
        statementFrequency: String,
        biometricsEnabled: Boolean,
        fullName: String,
        cellphone: String,
        isDarkMode: Boolean,
        foodMaxLimit: Double = 1500.0,
        rentMaxLimit: Double = 3000.0,
        transportMaxLimit: Double = 1000.0,
        savingsMaxLimit: Double = 2000.0,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val current = _loggedInUser.value ?: repository.getFirstUser()
            if (current != null) {
                val updated = current.copy(
                    fullName = fullName.trim(),
                    cellphone = cellphone.trim(),
                    dailyCardLimit = dailyCardLimit,
                    smsAlertsEnabled = smsAlertsEnabled,
                    isCardFrozen = isCardFrozen,
                    contactlessEnabled = contactlessEnabled,
                    statementFrequency = statementFrequency,
                    biometricsEnabled = biometricsEnabled,
                    isDarkMode = isDarkMode,
                    foodMaxLimit = foodMaxLimit,
                    rentMaxLimit = rentMaxLimit,
                    transportMaxLimit = transportMaxLimit,
                    savingsMaxLimit = savingsMaxLimit
                )
                repository.registerUser(updated)
                _loggedInUser.value = updated
            }
            onComplete()
        }
    }

    fun registerCustomer(
        email: String,
        fullName: String,
        passwordHash: String,
        biometricsEnabled: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "Please enter a valid email address.")
            return
        }
        if (fullName.isBlank() || fullName.length < 3) {
            onResult(false, "Please enter a valid full name.")
            return
        }
        if (passwordHash.length < 4) {
            onResult(false, "Password must be at least 4 characters long.")
            return
        }

        viewModelScope.launch {
            val user = RegisteredUser(
                email = email.trim(),
                fullName = fullName.trim(),
                cellphone = "",
                cardNumber = "",
                cardExpiry = "",
                cardCvvOrPin = "",
                passwordHash = passwordHash,
                biometricsEnabled = biometricsEnabled,
                // New user: no preset allocations, total allowance P2200
                foodAlloc = 0.0,
                rentAlloc = 0.0,
                transportAlloc = 0.0,
                savingsAlloc = 0.0,
                wifiAlloc = 0.0,
                mobileAlloc = 0.0,
                totalAllowanceLimit = 2200.0,
                // no visible categories by default
                visibleCategories = ""
            )
            repository.registerUser(user)
            _loggedInUser.value = user

            // No bank account is auto-created on registration — auth/registration UI is removed in the app flow.
            onResult(true, "OK")
        }
    }

    fun loginWithPassword(email: String, passwordHash: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim())
            if (user == null) {
                onResult(false, "Profile not found. Please register first.")
            } else if (user.passwordHash == passwordHash) {
                _loggedInUser.value = user
                _isLoggedIn.value = true
                onResult(true, "Authentication successful!")
            } else {
                onResult(false, "Incorrect password. Please try again.")
            }
        }
    }

    fun loginWithBiometrics(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getFirstUser()
            if (user == null) {
                onResult(false, "No registered profiles found. Please register.")
            } else if (!user.biometricsEnabled) {
                onResult(false, "Biometrics not enabled. Please log in with password and configure biometric access in settings.")
            } else {
                _loggedInUser.value = user
                _isLoggedIn.value = true
                onResult(true, "Authentication successful!")
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            _isLoggedIn.value = false
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearUser()
            _loggedInUser.value = null
            _isLoggedIn.value = false
        }
    }

    fun addAccount(name: String, number: String, balance: Double) {
        viewModelScope.launch {
            repository.addAccount(
                BSBAccount(
                    accountName = name,
                    accountNumber = number,
                    balance = balance
                )
            )
        }
    }

    fun addCard(holder: String, numberMasked: String, expiry: String, accountId: Int, cardType: String) {
        viewModelScope.launch {
            repository.addCard(
                BSBCard(
                    cardHolder = holder,
                    cardNumberMasked = numberMasked,
                    cardExpiry = expiry,
                    linkedAccountId = accountId,
                    cardType = cardType
                )
            )
        }
    }

    fun addPayment(
        type: String,
        payee: String,
        amount: Double,
        day: Int,
        accountId: Int?,
        cardId: Int?,
        recipientNum: String? = null,
        recipientBranchNo: String? = null,
        recipientBranchNm: String? = null,
        recipientNm: String? = null
    ) {
        viewModelScope.launch {
            repository.addPayment(
                ScheduledPayment(
                    paymentType = type,
                    payeeName = payee,
                    amount = amount,
                    paymentDay = day,
                    selectedAccountId = accountId,
                    selectedCardId = cardId,
                    isActive = true,
                    recipientAccount = recipientNum,
                    recipientBranchNumber = recipientBranchNo,
                    recipientBranchName = recipientBranchNm,
                    recipientName = recipientNm
                )
            )
        }
    }

    fun deletePayment(payment: ScheduledPayment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    fun addManualExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseItem(
                    title = title,
                    amount = amount,
                    category = category
                )
            )
        }
    }

    fun deleteExpense(expense: ExpenseItem) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun toggleFreeDataMode() {
        viewModelScope.launch {
            val nextVal = !_freeDataMode.value
            _freeDataMode.value = nextVal
        }
    }

    /**
     * Advances the calendar. If we advance, we check for scheduled payments matching the new day.
     */
    fun advanceSimulatedDay() {
        viewModelScope.launch {
            var nextDay = _simulatedDay.value + 1
            if (nextDay > 31) {
                nextDay = 1
                repository.clearAllExpenses()
            }
            _simulatedDay.value = nextDay

            // Run transaction operations for this day!
            val count = repository.executePaymentsForDay(nextDay)
            if (count > 0) {
                _paymentExecutionEvent.emit("$count pending automatic payments executed for Day $nextDay!")
            }
        }
    }

    fun clearAllExpenses() {
        viewModelScope.launch {
            repository.clearAllExpenses()
        }
    }

    /**
     * Manually triggers immediate billing execution for all scheduled payments assigned on standard day of choice.
     */
    fun triggerAllImmediateDue() {
        viewModelScope.launch {
            val currentDayVal = _simulatedDay.value
            val count = repository.executePaymentsForDay(currentDayVal)
            if (count > 0) {
                _paymentExecutionEvent.emit("Forced Processing: $count automatic bills executed successfully!")
            } else {
                _paymentExecutionEvent.emit("No automatic bills scheduled for simulated day $currentDayVal.")
            }
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    fun triggerSimulatedPurchase(merchant: String, amount: Double) {
        viewModelScope.launch {
            val user = _loggedInUser.value ?: repository.getFirstUser()
            val cardsList = repository.cards.first()
            val cardNum = cardsList.firstOrNull()?.cardNumberMasked ?: "**** **** **** 1234"
            _pendingApproval.value = PendingPurchaseApproval(
                merchantName = merchant,
                amount = amount,
                cardNumberMasked = cardNum
            )
        }
    }

    fun approvePurchase(purchase: PendingPurchaseApproval, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = _loggedInUser.value
            if (user != null && user.isCardFrozen) {
                _pendingApproval.value = null
                repository.addNotification(
                    title = "Purchase Declined (Card Frozen)",
                    message = "A simulated purchase of BWP ${purchase.amount} at ${purchase.merchantName} was blocked because your card is frozen."
                )
                onResult(false, "Transaction declined: Your card is currently frozen!")
                return@launch
            }
            // Get active account
            val accountsList = repository.accounts.first()
            val firstAccount = accountsList.firstOrNull()
            if (firstAccount == null) {
                _pendingApproval.value = null
                onResult(false, "No active Student 360 bank account linked!")
                return@launch
            }
            if (user != null && purchase.amount > user.dailyCardLimit) {
                _pendingApproval.value = null
                repository.addNotification(
                    title = "Purchase Declined (Limit Exceeded)",
                    message = "A simulated purchase of BWP ${purchase.amount} at ${purchase.merchantName} was blocked: exceeds daily card limit."
                )
                onResult(false, "Transaction declined: Exceeds daily card spend limit of BWP ${user.dailyCardLimit}!")
                return@launch
            }
            if (firstAccount.balance < purchase.amount) {
                _pendingApproval.value = null
                repository.addNotification(
                    title = "Purchase Declined (Low Balance)",
                    message = "A simulated purchase of BWP ${purchase.amount} at ${purchase.merchantName} failed due to insufficient funds."
                )
                onResult(false, "Transaction declined: Insufficient funds!")
                return@launch
            }

            // Deduct balance
            val updatedAccount = firstAccount.copy(balance = firstAccount.balance - purchase.amount)
            repository.updateAccount(updatedAccount)
            
            // Create ExpenseItem
            repository.addExpense(
                ExpenseItem(
                    title = purchase.merchantName,
                    amount = purchase.amount,
                    category = "Other Outflow"
                )
            )

            // Notifications log
            repository.addNotification(
                title = "Purchase Approved",
                message = "Securely authorized online checkout of BWP ${String.format("%.2f", purchase.amount)} at ${purchase.merchantName} with registered companion card."
            )

            _pendingApproval.value = null
            onResult(true, "Transaction of BWP ${purchase.amount} authorized successfully!")
        }
    }

    fun declinePurchase(purchase: PendingPurchaseApproval) {
        viewModelScope.launch {
            _pendingApproval.value = null
            repository.addNotification(
                title = "Purchase Blocked by Owner",
                message = "A simulated checkout request of BWP ${String.format("%.2f", purchase.amount)} at ${purchase.merchantName} was rejected/declined."
            )
        }
    }

    fun depositStudentAllowance() {
        viewModelScope.launch {
            val dbAccounts = repository.accounts.first()
            val allowanceAcc = dbAccounts.find { it.accountName.contains("Allowance", ignoreCase = true) } ?: dbAccounts.firstOrNull()
            if (allowanceAcc != null) {
                val updated = allowanceAcc.copy(balance = allowanceAcc.balance + 2200.00)
                repository.updateAccount(updated)
                repository.addNotification(
                    title = "Student 360 Allowance Direct Deposit",
                    message = "Your monthly tertiary student allowance of BWP 2,200.00 has been successfully deposited into account ${allowanceAcc.accountNumber} by Botswana Savings Bank."
                )
                _paymentExecutionEvent.emit("Allowance of BWP 2,200.00 Deposited!")
            }
        }
    }
}

data class PendingPurchaseApproval(
    val id: String = java.util.UUID.randomUUID().toString(),
    val merchantName: String,
    val amount: Double,
    val cardNumberMasked: String,
    val timestamp: Long = System.currentTimeMillis()
)

class CompanionViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompanionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

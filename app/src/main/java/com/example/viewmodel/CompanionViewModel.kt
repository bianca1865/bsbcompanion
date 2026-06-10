package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    // Simulated Calendar Day (1 - 28)
    private val _simulatedDay = MutableStateFlow(10)
    val simulatedDay: StateFlow<Int> = _simulatedDay.asStateFlow()

    // Free Data zero-rating status (Monetization network mode)
    private val _freeDataMode = MutableStateFlow(true)
    val freeDataMode: StateFlow<Boolean> = _freeDataMode.asStateFlow()

    val registeredUsers: StateFlow<List<RegisteredUser>> = repository.registeredUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loggedInUser = MutableStateFlow<RegisteredUser?>(null)
    val loggedInUser: StateFlow<RegisteredUser?> = _loggedInUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _pendingApproval = MutableStateFlow<PendingPurchaseApproval?>(null)
    val pendingApproval: StateFlow<PendingPurchaseApproval?> = _pendingApproval.asStateFlow()

    // Triggered status to notify user in immediate snackbar of payments executed
    private val _paymentExecutionEvent = MutableSharedFlow<String>(replay = 0)
    val paymentExecutionEvent: SharedFlow<String> = _paymentExecutionEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Check if there is any registered user to pre-load as biometric/login option
            var user = repository.getFirstUser()
            if (user == null) {
                // Seed a default registered user so there is always an active profile to customize
                val defaultUser = RegisteredUser(
                    email = "masego@gmail.com",
                    fullName = "Masego L. Kaelo",
                    cellphone = "71649231",
                    cardNumber = "4556102434529012",
                    cardExpiry = "10/29",
                    cardCvvOrPin = "123",
                    passwordHash = "1234",
                    biometricsEnabled = true,
                    dailyCardLimit = 2000.0,
                    smsAlertsEnabled = true,
                    isCardFrozen = false,
                    contactlessEnabled = true,
                    statementFrequency = "Monthly"
                )
                repository.registerUser(defaultUser)
                user = defaultUser
            }
            _loggedInUser.value = user
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
        wifiMaxLimit: Double = 1000.0,
        mobileMaxLimit: Double = 1000.0,
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
                    savingsMaxLimit = savingsMaxLimit,
                    wifiMaxLimit = wifiMaxLimit,
                    mobileMaxLimit = mobileMaxLimit
                )
                repository.registerUser(updated)
                _loggedInUser.value = updated
            }
            onComplete()
        }
    }

    fun updateAllocations(
        food: Double? = null,
        rent: Double? = null,
        transport: Double? = null,
        savings: Double? = null,
        wifi: Double? = null,
        mobile: Double? = null,
        total: Double? = null
    ) {
        viewModelScope.launch {
            val current = _loggedInUser.value ?: return@launch
            val updated = current.copy(
                foodAlloc = food ?: current.foodAlloc,
                rentAlloc = rent ?: current.rentAlloc,
                transportAlloc = transport ?: current.transportAlloc,
                savingsAlloc = savings ?: current.savingsAlloc,
                wifiAlloc = wifi ?: current.wifiAlloc,
                mobileAlloc = mobile ?: current.mobileAlloc,
                totalAllowanceLimit = total ?: current.totalAllowanceLimit
            )
            repository.registerUser(updated)
            _loggedInUser.value = updated

            // SYNC: If Rent in allocator changes, update the Rent Auto-Pay if it exists
            if (rent != null) {
                val rentPayment = payments.value.find { it.paymentType == "Rent" }
                if (rentPayment != null && rentPayment.amount != rent) {
                    repository.updatePayment(rentPayment.copy(amount = rent))
                }
            }
            
            // SYNC: If Wifi in allocator changes, update the Wifi Auto-Pay if it exists
            if (wifi != null) {
                val wifiPayment = payments.value.find { it.paymentType == "Wifi" }
                if (wifiPayment != null && wifiPayment.amount != wifi) {
                    repository.updatePayment(wifiPayment.copy(amount = wifi))
                }
            }
            
            // SYNC: If Mobile in allocator changes, update the Mobile Subscription Auto-Pay if it exists
            if (mobile != null) {
                val mobilePayment = payments.value.find { it.paymentType == "Mobile Subscription" }
                if (mobilePayment != null && mobilePayment.amount != mobile) {
                    repository.updatePayment(mobilePayment.copy(amount = mobile))
                }
            }
        }
    }

    fun toggleCategoryVisibility(category: String) {
        viewModelScope.launch {
            val current = _loggedInUser.value ?: return@launch
            val categories = current.visibleCategories.split(",").map { it.trim() }.toMutableList()
            if (categories.contains(category)) {
                categories.remove(category)
            } else {
                categories.add(category)
            }
            val updated = current.copy(visibleCategories = categories.filter { it.isNotBlank() }.joinToString(","))
            repository.registerUser(updated)
            _loggedInUser.value = updated
        }
    }

    fun registerCustomer(
        email: String,
        fullName: String,
        cellphone: String,
        cardNumber: String,
        cardExpiry: String,
        cardCvvOrPin: String,
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
        val cleanPhone = cellphone.replace(" ", "").replace("-", "")
        if (cleanPhone.length < 8) {
            onResult(false, "Please enter a valid Botswana cellphone number (e.g. 71XXXXXX).")
            return
        }
        val cleanCard = cardNumber.replace(" ", "").replace("-", "")
        if (cleanCard.length < 16) {
            onResult(false, "Please enter a valid 16-digit card number.")
            return
        }
        if (cardExpiry.length < 5 || !cardExpiry.contains("/")) {
            onResult(false, "Expiry date must be in MM/YY format.")
            return
        }
        if (cardCvvOrPin.length < 3) {
            onResult(false, "Please enter a valid 3-digit CVV or 4-digit ATM PIN.")
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
                cellphone = cleanPhone,
                cardNumber = cleanCard,
                cardExpiry = cardExpiry.trim(),
                cardCvvOrPin = cardCvvOrPin,
                passwordHash = passwordHash,
                biometricsEnabled = biometricsEnabled
            )
            repository.registerUser(user)
            _loggedInUser.value = user
            
            // Link a custom default account matching user's register details
            val accId = repository.addAccount(
                BSBAccount(
                    accountName = "BSB Ordinary Savings",
                    accountNumber = "1024" + (1000000..9999999).random().toString(),
                    balance = 7500.00
                )
            ).toInt()

            val last4Digits = cleanCard.takeLast(4)
            val maskedNo = "**** **** **** $last4Digits"
            repository.addCard(
                BSBCard(
                    cardHolder = fullName,
                    cardNumberMasked = maskedNo,
                    cardExpiry = cardExpiry,
                    linkedAccountId = accId,
                    cardType = "Student Card"
                )
            )

            onResult(true, "Registration successful! Welcome to Botswana Savings Bank companion.")
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
        accountId: Int,
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

            // SYNC: Update Allocator if adding certain Auto-Pays
            val current = _loggedInUser.value ?: return@launch
            val updated = when (type) {
                "Rent" -> current.copy(rentAlloc = amount)
                "Wifi" -> current.copy(wifiAlloc = amount)
                "Mobile Subscription" -> current.copy(mobileAlloc = amount)
                else -> current
            }
            if (updated != current) {
                repository.registerUser(updated)
                _loggedInUser.value = updated
            }
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
                onResult(false, "No active BSB bank account linked!")
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
                    title = "BSB Allowance Direct Deposit",
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

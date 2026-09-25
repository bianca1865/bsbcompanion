package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class BsbAccount(
    val id: Int,
    val accountName: String,
    val balance: Double
)

data class ScheduledPayment(
    val id: Int = (1000..9999).random(),
    val paymentType: String,
    val payeeName: String,
    val amount: Double,
    val paymentDay: Int,
    val selectedAccountId: Int,
    val isActive: Boolean
)

/**
 * Service abstraction layer for BSB Banking Integration.
 * Allows Student360 to communicate through well-defined contracts,
 * enabling seamless swap between local storage / mock BSB sandbox
 * and production BSB core banking APIs.
 */
interface BsbStudentService {
    fun getStudentProfile(): Flow<StudentProfile>
    fun getFinancialSummary(simulatedDay: Int): Flow<FinancialSummary>
    fun getCategorySpends(): Flow<List<CategorySpendItem>>
    fun getCommittedBills(simulatedDay: Int): Flow<List<CommittedBillItem>>
    fun getAiInsights(simulatedDay: Int): Flow<List<FinancialInsight>>
    suspend fun logQuickExpense(title: String, amount: Double, category: String)
    suspend fun simulateAllowanceDeposit(amount: Double = 2200.0)
    suspend fun setRingFenced(billId: Int, ringFenced: Boolean)
    suspend fun addCommittedBill(title: String, amount: Double, dueDay: Int, category: String)
    suspend fun settleBillNow(billId: Int)
    suspend fun transferToSavings(amount: Double)
    suspend fun rebalanceCategoryBudget(categoryName: String, newBudget: Double)
}

class BsbStudentServiceImpl(
    private val repository: Repository
) : BsbStudentService {

    private val defaultProfile = StudentProfile()

    private val _customBudgets = MutableStateFlow(
        mapOf(
            "Food & Meals" to 700.0,
            "Transport & Kombi" to 250.0,
            "Study Materials" to 350.0,
            "Data & Wifi" to 250.0,
            "Personal & Social" to 250.0,
            "Savings Reserve" to 400.0
        )
    )

    private val _accounts = MutableStateFlow(
        listOf(
            BsbAccount(1, "BSB Student Allowance Account", 1500.0),
            BsbAccount(2, "BSB Sesame Youth Savings", 250.0)
        )
    )

    private val _payments = MutableStateFlow(
        listOf(
            ScheduledPayment(1, "Rent", "Off-Campus Student Rez", 700.0, 2, 1, true),
            ScheduledPayment(2, "Data", "Orange Botswana", 150.0, 10, 1, true),
            ScheduledPayment(3, "Insurance", "Student Medical Cover", 80.0, 15, 1, false)
        )
    )

    override fun getStudentProfile(): Flow<StudentProfile> {
        return flowOf(defaultProfile)
    }

    override fun getFinancialSummary(simulatedDay: Int): Flow<FinancialSummary> {
        return combine(
            _accounts,
            repository.expenses,
            _payments
        ) { accounts, expenses, payments ->
            val allowance = defaultProfile.monthlyAllowance
            val totalSpent = expenses.sumOf { it.amount }

            // Committed is active scheduled payments that haven't passed yet or are ring-fenced
            val totalCommitted = payments
                .filter { it.isActive }
                .sumOf { it.amount }

            // Savings account balance in BSB
            val savingsAccount = accounts.find { it.accountName.contains("Savings", ignoreCase = true) }
            val totalSavings = savingsAccount?.balance ?: 150.0

            val remainingFreeToSpend = max(0.0, allowance - totalSpent - totalCommitted)

            // Cycle calculation: payday is the 25th.
            val cycleDaysTotal = 30
            val daysRemaining = if (simulatedDay <= defaultProfile.allowanceDepositDay) {
                max(1, defaultProfile.allowanceDepositDay - simulatedDay)
            } else {
                max(1, (cycleDaysTotal - simulatedDay) + defaultProfile.allowanceDepositDay)
            }

            val safeDailySpend = if (daysRemaining > 0) remainingFreeToSpend / daysRemaining else 0.0

            // Baseline ideal daily allowance
            val idealDailyBaseline = allowance / cycleDaysTotal // P73.33/day
            val pacingRatio = if (idealDailyBaseline > 0) safeDailySpend / idealDailyBaseline else 1.0

            val pacingStatus = when {
                remainingFreeToSpend <= 0.0 -> PacingStatus.OVERSPENT
                pacingRatio >= 1.05 -> PacingStatus.AHEAD_OF_PACE
                pacingRatio >= 0.75 -> PacingStatus.ON_PACE
                else -> PacingStatus.CAUTION
            }

            // Health score computation (0 - 100)
            val spentRatio = (totalSpent / allowance).coerceIn(0.0, 1.0)
            val dayProgressRatio = (simulatedDay.toDouble() / cycleDaysTotal).coerceIn(0.0, 1.0)
            
            var score = 85
            if (spentRatio > dayProgressRatio + 0.15) {
                score -= 20 // Spending faster than calendar days
            } else if (spentRatio < dayProgressRatio) {
                score += 8 // Under pacing
            }
            if (totalSavings > 100.0) score += 5
            if (remainingFreeToSpend < 150.0 && daysRemaining > 5) score -= 15
            val finalScore = score.coerceIn(25, 98)

            val (grade, advice) = when {
                finalScore >= 80 -> Pair("Excellent Pacing", "Discretionary allowance is safely tracking. You're set to save P${(totalSavings + 200).toInt()} this month.")
                finalScore >= 65 -> Pair("Stable Track", "Spending is balanced with upcoming bills. Avoid unplanned outings before the 25th.")
                else -> Pair("High Burn Rate", "Allowance is depleting faster than days left. Keep daily spend under P${safeDailySpend.roundToInt()} to prevent shortfalls.")
            }

            FinancialSummary(
                monthlyAllowance = allowance,
                totalSpent = totalSpent,
                totalCommitted = totalCommitted,
                totalSavings = totalSavings,
                remainingFreeToSpend = remainingFreeToSpend,
                daysRemainingInCycle = daysRemaining,
                safeDailySpend = safeDailySpend,
                pacingStatus = pacingStatus,
                healthScore = finalScore,
                healthGrade = grade,
                healthAdvice = advice
            )
        }
    }

    override fun getCategorySpends(): Flow<List<CategorySpendItem>> {
        return combine(
            repository.expenses,
            _customBudgets
        ) { expenses, budgets ->
            val categories = listOf(
                Triple("Food & Meals", budgets["Food & Meals"] ?: 700.0, "food"),
                Triple("Transport & Kombi", budgets["Transport & Kombi"] ?: 250.0, "transport"),
                Triple("Study Materials", budgets["Study Materials"] ?: 350.0, "study"),
                Triple("Data & Wifi", budgets["Data & Wifi"] ?: 250.0, "data"),
                Triple("Personal & Social", budgets["Personal & Social"] ?: 250.0, "personal"),
                Triple("Savings Reserve", budgets["Savings Reserve"] ?: 400.0, "savings")
            )

            categories.map { (catName, budget, iconKey) ->
                val spent = expenses.filter { exp ->
                    when (catName) {
                        "Food & Meals" -> exp.category.contains("Groceries", true) || exp.category.contains("Food", true) || exp.merchant.contains("Cafeteria", true)
                        "Transport & Kombi" -> exp.category.contains("Transport", true) || exp.merchant.contains("Combi", true) || exp.merchant.contains("Kombi", true)
                        "Study Materials" -> exp.category.contains("Study", true) || exp.merchant.contains("Book", true) || exp.merchant.contains("Print", true)
                        "Data & Wifi" -> exp.category.contains("Wifi", true) || exp.category.contains("Mobile", true) || exp.merchant.contains("Data", true)
                        "Savings Reserve" -> exp.category.contains("Savings", true)
                        else -> exp.category.contains("Other", true) || exp.category.contains("Personal", true)
                    }
                }.sumOf { it.amount }

                CategorySpendItem(
                    categoryName = catName,
                    spentAmount = spent,
                    allocatedBudget = budget,
                    iconKey = iconKey
                )
            }
        }
    }

    override fun getCommittedBills(simulatedDay: Int): Flow<List<CommittedBillItem>> {
        return _payments.combine(flowOf(simulatedDay)) { payments, currentDay ->
            payments.map { p ->
                val daysUntil = if (p.paymentDay >= currentDay) {
                    p.paymentDay - currentDay
                } else {
                    (30 - currentDay) + p.paymentDay
                }
                CommittedBillItem(
                    id = p.id,
                    title = p.payeeName,
                    amount = p.amount,
                    dueDay = p.paymentDay,
                    category = p.paymentType,
                    isRingFenced = p.isActive,
                    daysUntilDue = daysUntil
                )
            }.sortedBy { it.daysUntilDue }
        }
    }

    override fun getAiInsights(simulatedDay: Int): Flow<List<FinancialInsight>> {
        return combine(
            getFinancialSummary(simulatedDay),
            _payments,
            repository.expenses
        ) { summary, payments, expenses ->
            val insights = mutableListOf<FinancialInsight>()

            // 1. Safe-to-Spend pacing insight
            if (summary.pacingStatus == PacingStatus.AHEAD_OF_PACE) {
                insights.add(
                    FinancialInsight(
                        id = "pacing_positive",
                        type = InsightType.POSITIVE,
                        title = "Healthy Allowance Pacing",
                        message = "You have P${String.format("%.2f", summary.safeDailySpend)}/day safe-to-spend for the next ${summary.daysRemainingInCycle} days. You're on track to end the month with a surplus!",
                        tag = "Allowance Forecast",
                        actionText = "Transfer to Savings"
                    )
                )
            } else if (summary.pacingStatus == PacingStatus.CAUTION) {
                insights.add(
                    FinancialInsight(
                        id = "pacing_caution",
                        type = InsightType.WARNING,
                        title = "Pacing Acceleration Detected",
                        message = "Spend velocity is currently running above normal. Limiting non-essential cafeteria visits could preserve P120 this week.",
                        tag = "Pacing Alert",
                        actionText = "Review Outflows"
                    )
                )
            }

            // 2. Ring-fenced committed bills insight
            val upcomingBill = payments.filter { it.isActive && it.paymentDay >= simulatedDay }
                .minByOrNull { it.paymentDay }
            if (upcomingBill != null) {
                val daysToBill = upcomingBill.paymentDay - simulatedDay
                insights.add(
                    FinancialInsight(
                        id = "ring_fence_bill",
                        type = InsightType.RING_FENCE,
                        title = "Committed Outflow Ring-Fenced",
                        message = "${upcomingBill.payeeName} (P${String.format("%.2f", upcomingBill.amount)}) is due in $daysToBill days. Funds are protected in your BSB account.",
                        tag = "Auto Ring-Fence",
                        actionText = "View Obligations"
                    )
                )
            }

            // 3. Kombi & Transport analysis
            val transportSpent = expenses.filter { 
                it.category.contains("Transport", true) || it.merchant.contains("Combi", true) || it.merchant.contains("Kombi", true)
            }.sumOf { it.amount }
            insights.add(
                FinancialInsight(
                    id = "transport_kombi",
                    type = InsightType.TIP,
                    title = "Kombi & Commute Efficiency",
                    message = "Transport spend is currently P${String.format("%.2f", transportSpent)}. Tip: Off-peak student kombi sharing between Main Mall and campus saves up to P30/week.",
                    tag = "Student Tip",
                    actionText = "Transport Budget"
                )
            )

            // 4. BSB Youth savings perk
            insights.add(
                FinancialInsight(
                    id = "bsb_sesame_saver",
                    type = InsightType.SAVINGS,
                    title = "BSB Sesame Smart Youth Growth",
                    message = "BSB Sesame Youth account earns competitive compound interest on tertiary savings. Setting aside P50/month builds an emergency cushion of P600/year.",
                    tag = "BSB Savings",
                    actionText = "Boost Savings"
                )
            )

            insights
        }
    }

    override suspend fun logQuickExpense(title: String, amount: Double, category: String) {
        val now = Calendar.getInstance()
        val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(now.time)
        repository.addExpense(
            Expense(
                merchant = title,
                amount = amount,
                date = dateStr,
                category = category,
                type = "Manual",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun simulateAllowanceDeposit(amount: Double) {
        _accounts.value = _accounts.value.map { account ->
            if (account.id == 1) account.copy(balance = account.balance + amount) else account
        }
    }

    override suspend fun setRingFenced(billId: Int, ringFenced: Boolean) {
        _payments.value = _payments.value.map { p ->
            if (p.id == billId) p.copy(isActive = ringFenced) else p
        }
    }

    override suspend fun addCommittedBill(title: String, amount: Double, dueDay: Int, category: String) {
        val newBill = ScheduledPayment(
            paymentType = category,
            payeeName = title,
            amount = amount,
            paymentDay = dueDay.coerceIn(1, 31),
            selectedAccountId = 1,
            isActive = true
        )
        _payments.value = _payments.value + newBill
    }

    override suspend fun settleBillNow(billId: Int) {
        val bill = _payments.value.find { it.id == billId } ?: return
        val primary = _accounts.value.find { it.id == 1 }
        if (primary != null && primary.balance >= bill.amount) {
            _accounts.value = _accounts.value.map {
                if (it.id == 1) it.copy(balance = it.balance - bill.amount) else it
            }
            val now = Calendar.getInstance()
            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(now.time)
            repository.addExpense(
                Expense(
                    merchant = "Paid: ${bill.payeeName}",
                    amount = bill.amount,
                    date = dateStr,
                    category = bill.paymentType,
                    type = "Manual",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun transferToSavings(amount: Double) {
        val primary = _accounts.value.find { it.id == 1 }
        if (primary != null && primary.balance >= amount) {
            _accounts.value = _accounts.value.map {
                when (it.id) {
                    1 -> it.copy(balance = it.balance - amount)
                    2 -> it.copy(balance = it.balance + amount)
                    else -> it
                }
            }
            val now = Calendar.getInstance()
            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(now.time)
            repository.addExpense(
                Expense(
                    merchant = "Deposit to BSB Sesame Savings",
                    amount = amount,
                    date = dateStr,
                    category = "Savings Reserve",
                    type = "Manual",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun rebalanceCategoryBudget(categoryName: String, newBudget: Double) {
        val current = _customBudgets.value.toMutableMap()
        current[categoryName] = newBudget.coerceAtLeast(50.0)
        _customBudgets.value = current
    }
}

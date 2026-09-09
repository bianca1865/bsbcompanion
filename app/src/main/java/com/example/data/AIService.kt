package com.example.data

import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.random.Random

/**
 * Orbit AI Service for Student360.
 * Requirement 2: Reasoning layer capable of understanding financial context.
 */
class Student360AIService(private val repository: Repository) {

    /**
     * Requirement 10: Structured Financial Context
     * Generates a reasoning-based response by analyzing actual Student360 data.
     */
    suspend fun generateResponse(query: String): String {
        val user = repository.userProfile.first()
        val userName = user?.firstName ?: "Student"
        val expenses = repository.expenses.first()
        val allocations = repository.budgetAllocations.first()
        val recurring = repository.recurringExpenses.first()
        val allowance = user?.monthlyAllowance ?: 0.0

        // --- Requirement 11: Authoritative Calculations ---
        val totalSpent = expenses.sumOf { it.amount }
        val remainingAllowance = allowance - totalSpent
        
        // Committed Money (Requirement 6): Unpaid upcoming bills
        val upcomingCommitments = recurring.filter { !it.isPaid }.sumOf { it.amount }
        
        // Flexible Money (Requirement 6): Money left after spending AND commitments
        val flexibleMoney = (remainingAllowance - upcomingCommitments).coerceAtLeast(0.0)
        
        val lowerQuery = query.trim().lowercase()

        // --- Requirement 3 & 13: Financial Reasoning Result-based ---
        
        // 1. AFFORDABILITY REASONING (Requirement 3)
        if (lowerQuery.contains("afford") || lowerQuery.contains("can i buy") || lowerQuery.contains("spend")) {
            val amountMatch = "\\d+".toRegex().find(query)
            val cost = amountMatch?.value?.toDoubleOrNull() ?: 0.0
            
            if (cost == 0.0) return "To help you figure out if you can afford it, please tell me the price!"
            
            return if (cost > flexibleMoney) {
                "$userName, you have P${remainingAllowance.toInt()} available right now, but P${upcomingCommitments.toInt()} is needed for upcoming bills. That leaves you with P${flexibleMoney.toInt()} in flexible money. Spending P${cost.toInt()} would exceed your flexible amount by P${(cost - flexibleMoney).toInt()} and might make it hard to cover your commitments!"
            } else {
                "Yes! After accounting for your P${upcomingCommitments.toInt()} in committed expenses, you still have P${flexibleMoney.toInt()} in flexible money. Spending P${cost.toInt()} fits comfortably within your current budget!"
            }
        }

        // 2. TRANSACTION ANALYSIS (Requirement 5)
        if (lowerQuery.contains("spent") || lowerQuery.contains("spending") || lowerQuery.contains("transactions") || lowerQuery.contains("biggest")) {
            if (expenses.isEmpty()) return "I don't see any transactions in your record yet! Once you scan some receipts or upload a statement, I can identify patterns and the biggest expenses for you."
            
            val categorySpending = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
            val topCategory = categorySpending.maxByOrNull { it.value }
            val biggestExpense = expenses.maxByOrNull { it.amount }
            
            var report = "You've spent P${totalSpent.toInt()} so far this month. Your largest category is ${topCategory?.key} at P${topCategory?.value?.toInt()}."
            
            if (lowerQuery.contains("biggest") && biggestExpense != null) {
                report = "Your biggest single expense was P${biggestExpense.amount.toInt()} at ${biggestExpense.merchant}."
            }

            // Reasoning about Patterns (Requirement 7)
            if ((categorySpending["Food"] ?: 0.0) > (allowance * 0.3)) {
                report += "\n\nI noticed you're spending over 30% of your allowance on food. Weekend takeout might be driving this up—maybe try meal prepping to free up some flexible money!"
            }
            
            return report
        }

        // 3. BUDGET REASONING (Requirement 6)
        if (lowerQuery.contains("budget") || lowerQuery.contains("how am i doing") || lowerQuery.contains("overspending")) {
            val overAllocated = allocations.filter { it.spentAmount > it.allocatedAmount }
            if (overAllocated.isNotEmpty()) {
                val cat = overAllocated.first()
                return "You're currently over budget in ${cat.category} by P${(cat.spentAmount - cat.allocatedAmount).toInt()}. Since you have P${flexibleMoney.toInt()} in flexible money, you might want to move some funds to cover this category."
            }
            if (totalSpent > allowance) {
                return "You've spent P${totalSpent.toInt()}, which is P${(totalSpent - allowance).toInt()} over your total allowance. We should look at your upcoming payments to see what can be adjusted."
            }
            return "You're on track, $userName! Every category is currently within its allocated budget, and you have P${flexibleMoney.toInt()} left after all commitments are considered."
        }

        // 4. REMAINING BALANCE REASONING
        if (lowerQuery.contains("left") || lowerQuery.contains("remaining") || lowerQuery.contains("how much money")) {
            return "You have P${remainingAllowance.toInt()} left from your allowance, but remember that P${upcomingCommitments.toInt()} is already committed to upcoming bills. This leaves you with P${flexibleMoney.toInt()} that is truly flexible to spend!"
        }

        // 5. GENERAL FINANCIAL REASONING (Requirement 8)
        if (lowerQuery.contains("emergency fund")) {
            return "An emergency fund is a safety net set aside for unexpected costs like phone repairs or urgent travel! For a student, aiming for P500 to P1,000 is a great first goal to prevent debt when life gets messy."
        }
        
        if (lowerQuery.contains("need") && lowerQuery.contains("want")) {
            return "Needs are essentials like rent, groceries, and tuition. Wants are for fun, like movies or new clothes! A healthy budget prioritises 100% of your needs before you spend anything on wants."
        }

        if (lowerQuery.contains("save") || lowerQuery.contains("saving")) {
            return "To save more effectively, $userName, I recommend the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings! Based on your P${allowance.toInt()} allowance, you could aim to save P${(allowance * 0.2).toInt()} monthly."
        }

        // DEFAULT SUPPORTIVE PERSONALITY (Requirement 14)
        val defaults = listOf(
            "Hi $userName! I'm Orbit, your financial companion! I've analysed your P${allowance.toInt()} allowance and P${totalSpent.toInt()} spending. What can I help you with today?",
            "Hello! Orbit here. I can help you track your P${flexibleMoney.toInt()} flexible money or check if you're over budget. Ask me anything!",
            "I'm ready to help, $userName! We can look at your upcoming P${upcomingCommitments.toInt()} in bills or talk about saving for your next goal!"
        )
        return defaults[Random.nextInt(defaults.size)]
    }

    suspend fun getBudgetOptimizationAdvice(): String {
        val user = repository.userProfile.first() ?: return "Setup your profile first!"
        val allocations = repository.budgetAllocations.first()
        val allowance = user.monthlyAllowance
        val userName = user.firstName
        
        if (allowance <= 0) return "Please set your monthly allowance in the Budget section first!"
        
        val totalAllocated = allocations.sumOf { it.allocatedAmount }
        
        return if (totalAllocated > allowance) {
            val deficit = totalAllocated - allowance
            "Orbit detected a deficit: your allocations exceed your allowance by P${deficit.toInt()}! I recommend reducing your non-essential budgets in categories like Entertainment to balance things out."
        } else {
            "Your budget is looking good! You have P${(allowance - totalAllocated).toInt()} remaining to allocate to your savings or flexible spending."
        }
    }
}

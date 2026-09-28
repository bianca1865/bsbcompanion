package com.example.data

import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.random.Random

/**
 * Orbit AI Service for Student360.
 * Restored to the original rule-based reasoning model.
 * Enhanced with conversation history and authoritative financial context.
 * No external API dependencies.
 */
class Student360AIService(private val repository: Repository) {

    enum class GeminiStatus {
        SUCCESS,
        API_KEY_MISSING,
        UNAUTHORIZED,
        RATE_LIMITED,
        NETWORK_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * Generates a reasoning-based response by analyzing actual Student360 data
     * and taking conversation history into account.
     */
    suspend fun generateResponse(query: String, history: List<Pair<String, String>> = emptyList()): String {
        val user = repository.userProfile.first()
        val userName = user?.firstName ?: "Student"
        val expenses = repository.expenses.first()
        val allocations = repository.budgetAllocations.first()
        val recurring = repository.recurringExpenses.first()
        val allowance = user?.monthlyAllowance ?: 0.0

        // Authoritative Calculations
        val totalSpent = expenses.sumOf { it.amount }
        val remainingAllowance = allowance - totalSpent
        val upcomingCommitments = recurring.filter { !it.isPaid }.sumOf { it.amount }
        val flexibleMoney = (remainingAllowance - upcomingCommitments).coerceAtLeast(0.0)
        
        val trimmedQuery = query.trim()
        val lowerQuery = trimmedQuery.lowercase()

        // --- CONVERSATION HISTORY ANALYSIS ---
        val lastBotMessage = history.lastOrNull { it.first == "AI" }?.second?.lowercase() ?: ""
        
        val priceMatch = "\\d+".toRegex().find(trimmedQuery)
        val extractedPrice = priceMatch?.value?.toDoubleOrNull() ?: 0.0

        // 1. Follow-up Price Check (Price provided after Orbit asked for it)
        if (extractedPrice > 0.0 && (lastBotMessage.contains("tell me the price") || lastBotMessage.contains("how much"))) {
            return calculateAffordability(userName, extractedPrice, flexibleMoney, remainingAllowance, upcomingCommitments)
        }

        // 2. Context Continuity / "Remember" queries
        if (lowerQuery.contains("remember") || lowerQuery.contains("we talked about")) {
            if (history.any { it.second.lowercase().contains("grocery") || it.second.lowercase().contains("food") }) {
                val grocerySpent = expenses.filter { it.category.contains("Groceries", true) || it.category.contains("Food", true) }.sumOf { it.amount }
                return "Yes, I remember! We were discussing your food spending. You've currently spent P${grocerySpent.toInt()} in that category this month. Should we look at ways to save on groceries?"
            }
            return "I remember our conversation! We've been looking at your P${totalSpent.toInt()} total spending. Is there something specific you'd like to follow up on?"
        }

        // 3. AFFORDABILITY REASONING
        if (lowerQuery.contains("afford") || lowerQuery.contains("can i buy") || lowerQuery.contains("spend")) {
            if (extractedPrice == 0.0) {
                return "To help you figure out if you can afford it, please tell me the price!"
            }
            return calculateAffordability(userName, extractedPrice, flexibleMoney, remainingAllowance, upcomingCommitments)
        }

        // 4. TRANSACTION ANALYSIS
        if (lowerQuery.contains("spent") || lowerQuery.contains("spending") || lowerQuery.contains("transactions") || lowerQuery.contains("biggest") || lowerQuery.contains("spending the most")) {
            if (expenses.isEmpty()) return "I don't see any transactions in your record yet! Once you log some expenses or scan a receipt, I can analyze your spending for you."
            
            val categorySpending = expenses.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
            val topCategory = categorySpending.maxByOrNull { it.value }
            val biggestExpense = expenses.maxByOrNull { it.amount }
            
            if (lowerQuery.contains("biggest") || lowerQuery.contains("most")) {
                return "You're spending the most on ${topCategory?.key ?: "nothing yet"} (P${topCategory?.value?.toInt() ?: 0}). Your single biggest purchase was P${biggestExpense?.amount?.toInt() ?: 0} at ${biggestExpense?.merchant ?: "none"}."
            }
            
            var report = "You've spent P${totalSpent.toInt()} so far this month. Your largest category is ${topCategory?.key} at P${topCategory?.value?.toInt()}."
            if ((categorySpending["Food"] ?: 0.0) > (allowance * 0.3)) {
                report += "\n\nI noticed you're spending quite a bit on food. Maybe try meal prepping to free up some flexible money!"
            }
            return report
        }

        // 5. BUDGET REASONING & "HOW AM I DOING"
        if (lowerQuery.contains("budget") || lowerQuery.contains("how am i doing") || lowerQuery.contains("overspending")) {
            if (lowerQuery.contains("what is a budget")) {
                return "A budget is a roadmap for your money! It ensures your needs like rent and transport are covered first, leaving you with 'flexible money' for everything else."
            }
            
            val overAllocated = allocations.filter { it.spentAmount > it.allocatedAmount }
            if (overAllocated.isNotEmpty()) {
                val cat = overAllocated.first()
                return "You're currently over budget in ${cat.category} by P${(cat.spentAmount - cat.allocatedAmount).toInt()}. Since you have P${flexibleMoney.toInt()} in flexible money, we could adjust your allocations to cover it."
            }
            
            if (totalSpent > allowance) {
                return "You've spent P${totalSpent.toInt()}, which is P${(totalSpent - allowance).toInt()} over your allowance. Let's review your upcoming commitments to get things back on track."
            }
            return "You're doing great, $userName! Every category is within its budget, and you have P${flexibleMoney.toInt()} flexible money left after all bills are considered."
        }

        // 6. REMAINING BALANCE REASONING
        if (lowerQuery.contains("left") || lowerQuery.contains("remaining") || lowerQuery.contains("how much money")) {
            return "You have P${remainingAllowance.toInt()} remaining from your allowance. However, P${upcomingCommitments.toInt()} is already committed to bills. This leaves you with P${flexibleMoney.toInt()} that is truly safe to spend!"
        }

        // 7. GENERAL FINANCIAL LITERACY
        if (lowerQuery.contains("emergency fund")) {
            return "An emergency fund is a cash cushion for unexpected costs like medical bills or repairs. For a student, P500 to P1,000 is a great starter goal to keep you out of debt!"
        }
        
        if (lowerQuery.contains("need") && lowerQuery.contains("want")) {
            return "Needs are essentials (rent, food, study). Wants are extras (concerts, fashion). A healthy student budget covers 100% of needs before spending on wants!"
        }

        if (lowerQuery.contains("save") || lowerQuery.contains("saving")) {
            return "To save effectively, $userName, try the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings! With your P${allowance.toInt()} allowance, your savings goal could be P${(allowance * 0.2).toInt()}."
        }

        // 8. NATURAL CONVERSATION (Greetings, help, etc.)
        if (lowerQuery == "hi" || lowerQuery == "hello" || lowerQuery.contains("good morning") || lowerQuery.contains("good afternoon")) {
            return "Hi $userName! I'm Orbit, your financial companion. How can I help you manage your P${flexibleMoney.toInt()} in flexible money today?"
        }
        
        if (lowerQuery == "thank you" || lowerQuery == "thanks" || lowerQuery == "okay" || lowerQuery == "ok" || lowerQuery.contains("that helps")) {
            return "You're very welcome! I'm always here if you have more questions about your spending or budget."
        }

        // DEFAULT SUPPORTIVE RESPONSES
        val defaults = listOf(
            "Orbit here! I've analyzed your P${allowance.toInt()} allowance. Would you like to check if you're on track with your budget?",
            "I'm ready to help, $userName! We can look at your upcoming P${upcomingCommitments.toInt()} in bills or talk about your savings goals.",
            "I've got your latest financial data ready. Ask me anything about your spending, budget categories, or if you can afford a new purchase!"
        )
        return defaults[Random.nextInt(defaults.size)]
    }

    private fun calculateAffordability(userName: String, cost: Double, flexibleMoney: Double, remainingAllowance: Double, upcomingCommitments: Double): String {
        return if (cost > flexibleMoney) {
            "$userName, you have P${remainingAllowance.toInt()} left, but P${upcomingCommitments.toInt()} is needed for upcoming bills. That leaves P${flexibleMoney.toInt()} in flexible money. Spending P${cost.toInt()} would exceed your safe amount by P${(cost - flexibleMoney).toInt()}!"
        } else {
            "Yes! After accounting for P${upcomingCommitments.toInt()} in committed expenses, you still have P${flexibleMoney.toInt()} in flexible money. Spending P${cost.toInt()} fits comfortably within your budget."
        }
    }

    suspend fun getBudgetOptimizationAdvice(): String {
        val user = repository.userProfile.first() ?: return "Setup your profile first!"
        val allocations = repository.budgetAllocations.first()
        val allowance = user.monthlyAllowance
        
        if (allowance <= 0) return "Please set your monthly allowance in the Budget section first!"
        
        val totalAllocated = allocations.sumOf { it.allocatedAmount }
        
        return if (totalAllocated > allowance) {
            val deficit = totalAllocated - allowance
            "Orbit detected a budget gap: your allocations exceed your allowance by P${deficit.toInt()}! I recommend reducing your non-essential budgets to balance things."
        } else {
            "Your budget is looking healthy! You have P${(allowance - totalAllocated).toInt()} remaining to allocate to savings or flexible spending."
        }
    }
}

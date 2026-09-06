package com.example.data

enum class PacingStatus(val label: String, val description: String) {
    AHEAD_OF_PACE("Ahead of Pace", "Under budget - potential savings surplus"),
    ON_PACE("On Pace", "Spending at a sustainable daily rate"),
    CAUTION("Pacing Alert", "Accelerated spending - tap brakes on luxuries"),
    OVERSPENT("Allowance Overspent", "Discretionary funds exhausted for this cycle")
}

enum class InsightType {
    POSITIVE,
    WARNING,
    TIP,
    RING_FENCE,
    SAVINGS
}

data class StudentProfile(
    val fullName: String = "Kagiso Molefe",
    val institution: String = "University of Botswana",
    val studentNumber: String = "UB-2022-04829",
    val campus: String = "Gaborone Main Campus",
    val sponsorName: String = "DTEF Tertiary Allowance",
    val primaryAccountName: String = "BSB Student Allowance Account",
    val primaryAccountNumber: String = "10243950621",
    val monthlyAllowance: Double = 2200.00,
    val allowanceDepositDay: Int = 25
)

data class FinancialSummary(
    val monthlyAllowance: Double,
    val totalSpent: Double,
    val totalCommitted: Double,
    val totalSavings: Double,
    val remainingFreeToSpend: Double,
    val daysRemainingInCycle: Int,
    val safeDailySpend: Double,
    val pacingStatus: PacingStatus,
    val healthScore: Int,
    val healthGrade: String,
    val healthAdvice: String
)

data class FinancialInsight(
    val id: String,
    val type: InsightType,
    val title: String,
    val message: String,
    val tag: String,
    val actionText: String? = null
)

data class CategorySpendItem(
    val categoryName: String,
    val spentAmount: Double,
    val allocatedBudget: Double,
    val iconKey: String
)

data class CommittedBillItem(
    val id: Int,
    val title: String,
    val amount: Double,
    val dueDay: Int,
    val category: String,
    val isRingFenced: Boolean,
    val daysUntilDue: Int
)

data class StatementTransaction(
    val id: String,
    val date: String,
    val description: String,
    val amount: Double,
    val isCredit: Boolean,
    val category: String,
    val isSelected: Boolean = true
)

data class ScannedOcrReceipt(
    val id: String,
    val merchant: String,
    val date: String,
    val totalAmount: Double,
    val vatAmount: Double,
    val items: List<String>,
    val category: String,
    val confidence: Float = 0.96f
)

data class AllowanceAllocationPreset(
    val name: String,
    val rent: Double,
    val food: Double,
    val transport: Double,
    val study: Double,
    val savings: Double,
    val description: String
)

data class StudentSavingsGoal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val iconEmoji: String,
    val targetMonth: String
)

data class BsbPaymentReceipt(
    val referenceNumber: String,
    val payeeName: String,
    val amount: Double,
    val fromAccount: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCode: String? = null,
    val fee: Double = 0.0
)

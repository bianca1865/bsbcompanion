package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Orbit AI Service for Student360.
 * Powered by Google Gemini.
 * Diagnosis and Fix implementation.
 */
class Student360AIService(private val repository: Repository) {

    private val TAG = "Student360AIService"

    // Diagnosis status
    enum class GeminiStatus {
        SUCCESS,
        API_KEY_MISSING,
        API_KEY_PLACEHOLDER,
        UNAUTHORIZED,
        RATE_LIMITED,
        NETWORK_ERROR,
        INVALID_MODEL,
        PARSING_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * Checks if the API key is configured without exposing the key itself.
     */
    fun isApiKeyConfigured(): Boolean {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        return apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "unused"
    }

    /**
     * Diagnostic check for the Gemini connection.
     * Performs a lightweight request to verify the entire pipeline.
     */
    suspend fun testGeminiConnection(): GeminiStatus {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        
        Log.d(TAG, "Diagnostic: Gemini API key configured: ${isApiKeyConfigured()}")
        
        if (apiKey.isEmpty()) {
            Log.e(TAG, "Diagnostic: API Key is missing from BuildConfig")
            return GeminiStatus.API_KEY_MISSING
        }
        
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey == "unused") {
            Log.e(TAG, "Diagnostic: API Key is still a placeholder")
            return GeminiStatus.API_KEY_PLACEHOLDER
        }

        return try {
            // Using a specific model and simple prompt for connection test
            val testModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)
            val response = testModel.generateContent("Reply with the word CONNECTED.")
            val responseText = response.text?.trim()
            
            if (responseText?.contains("CONNECTED", ignoreCase = true) == true) {
                Log.d(TAG, "Diagnostic: Connection test SUCCESS")
                GeminiStatus.SUCCESS
            } else {
                Log.e(TAG, "Diagnostic: Unexpected response format: $responseText")
                GeminiStatus.PARSING_ERROR
            }
        } catch (e: Exception) {
            Log.e(TAG, "Diagnostic: Connection test failed", e)
            mapExceptionToStatus(e)
        }
    }

    private fun mapExceptionToStatus(e: Exception): GeminiStatus {
        val msg = e.message ?: ""
        return when {
            msg.contains("403") || msg.contains("permission", ignoreCase = true) || msg.contains("API_KEY_INVALID") -> GeminiStatus.UNAUTHORIZED
            msg.contains("429") -> GeminiStatus.RATE_LIMITED
            msg.contains("400") -> GeminiStatus.INVALID_MODEL
            msg.contains("Unable to resolve host") || msg.contains("timeout") || msg.contains("NetworkError") -> GeminiStatus.NETWORK_ERROR
            else -> GeminiStatus.UNKNOWN_ERROR
        }
    }

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
            )
        )
    }

    suspend fun generateResponse(query: String, history: List<Pair<String, String>> = emptyList()): String {
        Log.d(TAG, "generateResponse called with query: $query")
        
        if (!isApiKeyConfigured()) {
            Log.e(TAG, "Configuration Error: GEMINI_API_KEY is not set correctly in local.properties")
            return "Orbit's reasoning engine is not configured. Please set a valid GEMINI_API_KEY in your local.properties file."
        }

        return try {
            val user = repository.userProfile.first()
            val userName = user?.firstName ?: "Student"
            val expenses = repository.expenses.first()
            val allocations = repository.budgetAllocations.first()
            val recurring = repository.recurringExpenses.first()
            val goals = repository.savingsGoals.first()
            val allowance = user?.monthlyAllowance ?: 0.0

            val totalSpent = expenses.sumOf { it.amount }
            val remainingAllowance = allowance - totalSpent
            val upcomingCommitments = recurring.filter { !it.isPaid }.sumOf { it.amount }
            val flexibleMoney = (remainingAllowance - upcomingCommitments).coerceAtLeast(0.0)

            val systemInstructionText = """
                IDENTITY: You are Orbit, a friendly financial companion for tertiary students.
                USER: $userName
                
                LIVE DATA:
                - Allowance: P${String.format(Locale.US, "%.2f", allowance)}
                - Spent: P${String.format(Locale.US, "%.2f", totalSpent)}
                - Remaining: P${String.format(Locale.US, "%.2f", remainingAllowance)}
                - Upcoming Bills: P${String.format(Locale.US, "%.2f", upcomingCommitments)}
                - Flexible Money: P${String.format(Locale.US, "%.2f", flexibleMoney)}
                
                BUDGET CATEGORIES:
                ${allocations.joinToString("\n") { "- ${it.name}: P${it.allocatedAmount} allocated, P${it.spentAmount} spent" }}
                
                DATE: ${SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US).format(Date())}
                
                RULES:
                1. You are ORBIT. No mention of AI models or Gemini.
                2. Use the LIVE DATA for accurate reasoning. Do not invent numbers.
                3. Be conversational and supportive.
                4. Maintain continuity using chat history.
            """.trimIndent()

            val geminiHistory = history.takeLast(10).map { (sender, text) ->
                content(role = if (sender == "User") "user" else "model") { text(text) }
            }

            val chat = generativeModel.startChat(history = geminiHistory)
            val fullPrompt = "System Context:\n$systemInstructionText\n\nUser Message: $query"
            
            val result = chat.sendMessage(fullPrompt)
            val responseText = result.text
            
            if (responseText.isNullOrBlank()) {
                Log.e(TAG, "Gemini returned empty response")
                "I'm listening, but I couldn't quite find the right words. Could you try rephrasing that?"
            } else {
                Log.d(TAG, "Gemini response successful")
                responseText
            }

        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Exception: ${e.message}", e)
            val status = mapExceptionToStatus(e)
            when (status) {
                GeminiStatus.UNAUTHORIZED -> "I don't have permission to access my reasoning engine. Please verify the API key in local.properties."
                GeminiStatus.RATE_LIMITED -> "I'm receiving too many requests right now. Please wait a moment."
                GeminiStatus.NETWORK_ERROR -> "I'm having trouble reaching the internet. Please check your connection and try again."
                GeminiStatus.INVALID_MODEL -> "My reasoning engine version is no longer supported. Please check for app updates."
                else -> "I'm having a bit of trouble connecting to my reasoning engine (${e.javaClass.simpleName}). Please try again in a moment!"
            }
        }
    }

    suspend fun getBudgetOptimizationAdvice(): String {
        return generateResponse("Based on my data, give me one specific tip to save money.")
    }
}

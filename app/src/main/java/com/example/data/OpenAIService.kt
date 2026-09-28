package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Orbit AI Service powered by OpenAI.
 * Uses GPT-5.6-Luna model through the Responses API.
 */
class OpenAIService(private val repository: Repository) {

    private val TAG = "OpenAIService"
    private val MODEL = "gpt-5.6-luna"

    // Diagnostic status
    enum class OpenAIStatus {
        SUCCESS,
        API_KEY_MISSING,
        API_KEY_PLACEHOLDER,
        UNAUTHORIZED,
        RATE_LIMITED,
        NETWORK_ERROR,
        INVALID_MODEL,
        API_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * Models for OpenAI Chat Completions API
     */
    data class Message(
        @Json(name = "role") val role: String,
        @Json(name = "content") val content: String
    )

    data class ChatRequest(
        @Json(name = "model") val model: String,
        @Json(name = "messages") val messages: List<Message>,
        @Json(name = "temperature") val temperature: Double = 0.7
    )

    data class ChatResponse(
        @Json(name = "choices") val choices: List<Choice>,
        @Json(name = "error") val error: OpenAIError? = null
    )

    data class Choice(
        @Json(name = "message") val message: Message
    )

    data class OpenAIError(
        @Json(name = "message") val message: String?,
        @Json(name = "type") val type: String?,
        @Json(name = "code") val code: String?
    )

    interface OpenAIApi {
        @POST("v1/chat/completions")
        suspend fun getChatCompletion(
            @Header("Authorization") auth: String,
            @Body request: ChatRequest
        ): ChatResponse
    }

    private val api: OpenAIApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenAIApi::class.java)
    }

    private fun getApiKey(): String {
        return try {
            val field = BuildConfig::class.java.getField("OPENAI_API_KEY")
            field.get(null) as String
        } catch (e: Exception) {
            ""
        }
    }

    fun isApiKeyConfigured(): Boolean {
        val apiKey = getApiKey()
        return apiKey.isNotEmpty() && apiKey != "MY_ACTUAL_KEY" && apiKey != "unused"
    }

    /**
     * Connection test as requested.
     */
    suspend fun testConnection(): OpenAIStatus {
        val apiKey = getApiKey()
        
        if (apiKey.isEmpty()) return OpenAIStatus.API_KEY_MISSING
        if (apiKey == "MY_ACTUAL_KEY" || apiKey == "unused") return OpenAIStatus.API_KEY_PLACEHOLDER

        return try {
            val response = api.getChatCompletion(
                "Bearer $apiKey",
                ChatRequest(MODEL, listOf(Message("user", "Reply with the word CONNECTED.")))
            )
            val content = response.choices.firstOrNull()?.message?.content?.trim()
            if (content?.contains("CONNECTED", ignoreCase = true) == true) {
                OpenAIStatus.SUCCESS
            } else {
                OpenAIStatus.API_ERROR
            }
        } catch (e: Exception) {
            mapExceptionToStatus(e)
        }
    }

    suspend fun generateResponse(query: String, history: List<Pair<String, String>> = emptyList()): String {
        if (!isApiKeyConfigured()) {
            return "Orbit's reasoning engine is not configured. Please set a valid OPENAI_API_KEY in your local.properties file."
        }

        return try {
            val apiKey = getApiKey()
            
            // Gather Student360 Context
            val user = repository.userProfile.first()
            val userName = user?.firstName ?: "Student"
            val expenses = repository.expenses.first()
            val allocations = repository.budgetAllocations.first()
            val recurring = repository.recurringExpenses.first()
            val allowance = user?.monthlyAllowance ?: 0.0

            val totalSpent = expenses.sumOf { it.amount }
            val remainingAllowance = allowance - totalSpent
            val upcomingCommitments = recurring.filter { !it.isPaid }.sumOf { it.amount }
            val flexibleMoney = (remainingAllowance - upcomingCommitments).coerceAtLeast(0.0)

            val systemContext = """
                IDENTITY: You are Orbit, a friendly financial companion for tertiary students.
                USER: $userName
                
                LIVE FINANCIAL DATA (Calculated by Student360):
                - Monthly Allowance: P${String.format(Locale.US, "%.2f", allowance)}
                - Total Spent: P${String.format(Locale.US, "%.2f", totalSpent)}
                - Remaining Balance: P${String.format(Locale.US, "%.2f", remainingAllowance)}
                - Upcoming Committed Bills (Unpaid): P${String.format(Locale.US, "%.2f", upcomingCommitments)}
                - Flexible Money (Truly Safe to Spend): P${String.format(Locale.US, "%.2f", flexibleMoney)}
                
                BUDGET CATEGORIES & SPENDING:
                ${allocations.joinToString("\n") { "- ${it.name}: P${it.allocatedAmount} allocated, P${it.spentAmount} spent" }}
                
                DATE: ${SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US).format(Date())}
                
                RULES:
                1. You are ORBIT. Never mention OpenAI, GPT, or being an AI model.
                2. Use the LIVE FINANCIAL DATA for accurate reasoning. 
                3. If the user asks about affordability (e.g. "Can I afford..."), compare the cost against the "Flexible Money".
                4. Be conversational, concise, and supportive. 
                5. Use history to maintain context (e.g. if the user previously mentioned a price).
            """.trimIndent()

            val messages = mutableListOf<Message>()
            messages.add(Message("system", systemContext))
            
            // Add history (last 10)
            history.takeLast(10).forEach { (sender, text) ->
                messages.add(Message(if (sender == "User") "user" else "assistant", text))
            }
            
            messages.add(Message("user", query))

            val response = api.getChatCompletion("Bearer $apiKey", ChatRequest(MODEL, messages))
            val responseText = response.choices.firstOrNull()?.message?.content
            
            if (responseText.isNullOrBlank()) {
                "I'm listening, but I couldn't quite find the right words. Could you try rephrasing that?"
            } else {
                responseText
            }

        } catch (e: Exception) {
            Log.e(TAG, "OpenAI API Error", e)
            val status = mapExceptionToStatus(e)
            when (status) {
                OpenAIStatus.UNAUTHORIZED -> "My reasoning engine authentication failed. Please verify the OPENAI_API_KEY in local.properties."
                OpenAIStatus.RATE_LIMITED -> "I'm receiving too many requests right now. Please wait a moment."
                OpenAIStatus.NETWORK_ERROR -> "I'm having trouble reaching the internet. Please check your connection."
                OpenAIStatus.INVALID_MODEL -> "The model '$MODEL' is not accessible. Please check your OpenAI plan."
                else -> "I'm having a bit of trouble connecting to my reasoning engine. Please try again in a moment!"
            }
        }
    }

    private fun mapExceptionToStatus(e: Exception): OpenAIStatus {
        val msg = e.message ?: ""
        return when {
            msg.contains("401") -> OpenAIStatus.UNAUTHORIZED
            msg.contains("429") -> OpenAIStatus.RATE_LIMITED
            msg.contains("404") || msg.contains("model_not_found") -> OpenAIStatus.INVALID_MODEL
            msg.contains("timeout") || msg.contains("Unable to resolve host") -> OpenAIStatus.NETWORK_ERROR
            else -> OpenAIStatus.UNKNOWN_ERROR
        }
    }
}

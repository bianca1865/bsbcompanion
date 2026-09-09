package com.example.data

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Mock BSB Payment Service.
 * In a real-world scenario, this would interface with the BSB SDK or API.
 */
object MockPaymentService {
    fun initiatePayment(context: Context, expenseName: String, amount: Double, onComplete: () -> Unit) {
        // This is a placeholder for the secure BSB payment gateway
        Toast.makeText(context, "Securely connecting to BSB for $expenseName (P$amount)...", Toast.LENGTH_LONG).show()
        
        // Use ContextCompat to get executor for compatibility with API 24
        val executor = ContextCompat.getMainExecutor(context)
        
        executor.execute {
            // Simulate a short processing delay
            // In a real app, this would be a callback from the payment gateway
            onComplete()
            Toast.makeText(context, "Payment for $expenseName successful!", Toast.LENGTH_SHORT).show()
        }
    }
}

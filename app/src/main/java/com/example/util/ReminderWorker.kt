package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.example.data.AppDatabase
import com.example.data.Repository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(database)
        val notificationHelper = NotificationHelper(applicationContext)

        val user = repository.userProfile.first() ?: return ListenableWorker.Result.success()
        if (!user.isLoggedIn) return ListenableWorker.Result.success()

        val recurring = repository.recurringExpenses.first()
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        recurring.filter { !it.isPaid }.forEach { expense ->
            val daysLeft = expense.dueDate - today
            
            when {
                daysLeft == 7 && user.rentReminder -> {
                    notificationHelper.showPaymentReminder(
                        "${expense.name} Reminder",
                        "${expense.name} is coming up in 7 days — P${expense.amount.toInt()}"
                    )
                }
                daysLeft == 3 && user.rentReminder -> {
                    notificationHelper.showPaymentReminder(
                        "${expense.name} Reminder",
                        "${expense.name} is due in 3 days — P${expense.amount.toInt()}"
                    )
                }
                daysLeft == 0 && user.rentReminder -> {
                    notificationHelper.showPaymentReminder(
                        "${expense.name} Due Today",
                        "${expense.name} is due today — P${expense.amount.toInt()}"
                    )
                }
            }
        }

        return ListenableWorker.Result.success()
    }
}

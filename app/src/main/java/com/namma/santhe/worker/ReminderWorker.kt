package com.namma.santhe.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.namma.santhe.data.repository.LedgerRepository
import com.namma.santhe.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: LedgerRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val totalDue = repository.getGlobalTotalDue().first()
        
        if (totalDue > 0) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showReminder(
                title = "Payment Reminders",
                message = "You have ₹${String.format("%.2f", totalDue)} in outstanding dues from your customers."
            )
        }
        
        return Result.success()
    }
}

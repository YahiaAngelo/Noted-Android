package com.noted.noted.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.noted.noted.MainActivity
import com.noted.noted.R
import com.noted.noted.receiver.ReminderActionReceiver
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext: Context, workerParameters: WorkerParameters): CoroutineWorker(appContext, workerParameters) {
    private val channelId = "alarm"

    companion object{
        fun setReminder(taskTitle: String, reminderId: Long, taskId: Long,reminderDate: Long, repeat: Boolean, context: Context){
            val workManager = WorkManager.getInstance(context)
            val nowTime = System.currentTimeMillis()
            var newReminderDate = reminderDate
            // if reminder is before current time set reminder for next day
            if (newReminderDate < nowTime){
                newReminderDate = reminderDate + 43200000
            }

            val data = workDataOf("taskTitle" to taskTitle, "reminderId" to reminderId,"taskId" to taskId , "reminderDate" to newReminderDate, "repeat" to repeat)
            val duration = (newReminderDate - nowTime)
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(duration, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            workManager.enqueueUniqueWork("reminder${reminderId}", ExistingWorkPolicy.REPLACE, workRequest)

        }

        fun cancel(reminderId : Long, context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("reminder$reminderId")
        }

    }
    override suspend fun doWork(): Result  = coroutineScope {
        val worker = this
        val context = applicationContext

        val taskTitle = inputData.getString("taskTitle")
        val reminderId = inputData.getLong("reminderId", 0)
        val taskId = inputData.getLong("taskId", 0)
        val reminderDate = inputData.getLong("reminderDate", 0)
        val repeat = inputData.getBoolean("repeat", false)

        try {

            val doneIntent = Intent(context, ReminderActionReceiver::class.java).apply {
                action = "action_done"
                putExtra("task_id", taskId)
            }
            val donePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1234, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_sticky_note)
                .setContentTitle("Reminder")
                .setContentText(taskTitle)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_done, "Mark as done", donePendingIntent)
                .setAutoCancel(true)

            val i = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT)
            builder.setContentIntent(pendingIntent)
            manager.notify(6969, builder.build())

            Result.success()
        }catch (e: Exception){
            if (runAttemptCount > 3) {
                return@coroutineScope Result.success()
            }
            Result.failure()
        }finally {

            if (repeat){
                // Add 12 hours for the next day reminder
                val newReminderDate = reminderDate + 43200000
                setReminder(taskTitle!!, reminderId, taskId, newReminderDate , repeat, context)
            }
        }
    }


}
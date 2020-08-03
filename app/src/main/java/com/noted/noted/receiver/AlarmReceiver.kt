package com.noted.noted.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.noted.noted.MainActivity
import com.noted.noted.R

class AlarmReceiver : BroadcastReceiver() {
    private val channelId = "alarm"

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent!!.getStringExtra("myAction") != null && intent.getStringExtra("myAction") == "alarmNotify"){
            val doneIntent = Intent(context, ReminderActionReceiver::class.java).apply {
                action = "action_done"
                putExtra("task_id", intent.getLongExtra("task_id", 0))
            }
            val donePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 1234, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val manager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_sticky_note)
                .setContentTitle("Reminder")
                .setContentText(intent.getStringExtra("title"))
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_done, "Mark as done", donePendingIntent)
                .setAutoCancel(true)

            val i = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT)
            builder.setContentIntent(pendingIntent)
            manager.notify(6969, builder.build())
        }
    }


}
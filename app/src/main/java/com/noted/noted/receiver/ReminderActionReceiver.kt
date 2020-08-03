package com.noted.noted.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noted.noted.model.Task
import com.noted.noted.utils.AlarmUtils
import io.realm.Realm

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "action_done"){
            val realm = Realm.getDefaultInstance()
            val alarmUtils = AlarmUtils()
            realm.use {
               val task = it.where(Task::class.java).equalTo("id", intent.getLongExtra("task_id", 0)).findFirst()
                alarmUtils.cancelAlarm(task!!.reminder!!, context)
                it.beginTransaction()
                task.checked = true
                it.commitTransaction()
                it.close()
            }
            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(it)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(6969)
        }
    }
}

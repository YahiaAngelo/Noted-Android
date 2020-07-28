package com.noted.noted.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.noted.noted.model.Task
import com.noted.noted.utils.AlarmUtils
import io.realm.Realm

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("Noted", "Just received an action")
        if (intent.action == "action_done"){
            Log.e("Noted", "Just received done action")
            val realm = Realm.getDefaultInstance()
            val alarmUtils = AlarmUtils()
            realm.use {
               val task = realm.where(Task::class.java).equalTo("id", intent.getLongExtra("task_id", 0)).findFirst()
                alarmUtils.cancelAlarm(task!!.reminder!!, context)
                it.beginTransaction()
                task.checked = true
                it.commitTransaction()
            }
            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(it)
        }
    }
}

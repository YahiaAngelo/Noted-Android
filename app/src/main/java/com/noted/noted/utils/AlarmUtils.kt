package com.noted.noted.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import com.noted.noted.receiver.AlarmReceiver
import org.parceler.Parcels
import java.util.*

 class AlarmUtils {

     fun setAlarm(task: Task, context: Context){
        val alarmMgr: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent: PendingIntent = Intent(context, AlarmReceiver::class.java).let {
            it.putExtra("myAction", "alarmNotify")
            it.putExtra("title", task.title)
            PendingIntent.getBroadcast(context, task.reminder!!.id.toInt(), it, 0)
        }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = task.reminder!!.date
        }
         Log.e("Noted", "Setting alarm at ${calendar.timeInMillis}")
         if (task.reminder!!.repeat){
             Log.e("Noted", "Setting a repeating alarm")
             alarmMgr.setInexactRepeating(
                 AlarmManager.RTC_WAKEUP,
                 calendar.timeInMillis,
                 AlarmManager.INTERVAL_DAY,
                 alarmIntent
             )
         }


     }
}
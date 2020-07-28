package com.noted.noted.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import com.noted.noted.receiver.AlarmReceiver
import java.util.*

 class AlarmUtils {

     fun setAlarm(task: Task, context: Context){
        val alarmMgr: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent: PendingIntent = Intent(context, AlarmReceiver::class.java).let {
            it.putExtra("myAction", "alarmNotify")
            it.putExtra("title", task.title)
            it.putExtra("task_id", task.id)
            PendingIntent.getBroadcast(context, task.reminder!!.id.toInt(), it, 0)
        }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = task.reminder!!.date
        }
         if (task.reminder!!.repeat){
             alarmMgr.setInexactRepeating(
                 AlarmManager.RTC_WAKEUP,
                 calendar.timeInMillis,
                 AlarmManager.INTERVAL_DAY,
                 alarmIntent
             )
         } else{
             alarmMgr.set( AlarmManager.RTC_WAKEUP,
                 calendar.timeInMillis,
                 alarmIntent)
         }

     }


      fun cancelAlarm(reminder: Reminder, context: Context){
         val alarmMgr: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
         val intent = Intent(context, AlarmReceiver::class.java)
         val pendingIntent = PendingIntent.getBroadcast(context, reminder.id.toInt(), intent, 0)
         alarmMgr.cancel(pendingIntent)
     }
}
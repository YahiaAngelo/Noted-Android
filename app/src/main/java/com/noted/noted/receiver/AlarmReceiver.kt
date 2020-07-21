package com.noted.noted.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import com.noted.noted.MainActivity
import com.noted.noted.R
import com.noted.noted.model.Reminder
import com.noted.noted.model.Task
import io.realm.Realm
import io.realm.kotlin.where
import org.parceler.Parcels

class AlarmReceiver : BroadcastReceiver() {
    private val channelId = "alarm"

    override fun onReceive(context: Context?, intent: Intent?) { 

        if (intent!!.getStringExtra("myAction") != null && intent.getStringExtra("myAction") == "alarmNotify"){
            val manager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_sticky_note)
                .setContentTitle("Reminder")
                .setContentText(intent.getStringExtra("title"))
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val i = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT)
            builder.setContentIntent(pendingIntent)
            manager.notify(6969, builder.build())
        }
    }


}
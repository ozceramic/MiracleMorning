package com.example.miraclemorning.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object MyAlarmUtil {
    fun setQuoteAlarm(context: Context, hour: Int, minute: Int, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MyAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}

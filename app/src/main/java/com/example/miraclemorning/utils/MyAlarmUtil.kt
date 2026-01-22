
package com.example.miraclemorning.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.miraclemorning.alarm.MyAlarmReceiver
import java.util.Calendar

object MyAlarmUtil {

    // 루틴 시작 전 알림 3개 예약
    @SuppressLint("ScheduleExactAlarm")
    fun setRoutinePreAlarms(
        context: Context,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        routineContent: String,
        routineId: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val minutesBefore = listOf(15, 10, 5)

        for ((index, beforeMin) in minutesBefore.withIndex()) {
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                add(Calendar.MINUTE, -beforeMin)
            }

            val requestCode = routineId * 10 + index

            val intent = Intent(context, MyAlarmReceiver::class.java).apply {
                putExtra("content", "$routineContent 시작 ${beforeMin}분 전입니다")
                putExtra("routineId", routineId)
                putExtra("requestCode", requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    // 루틴 알림 전체 취소 (시작하면 호출)
    fun cancelRoutinePreAlarms(context: Context, routineId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (index in 0..2) {
            val requestCode = routineId * 10 + index
            val intent = Intent(context, MyAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}

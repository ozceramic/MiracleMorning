package com.example.miraclemorning.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object MyAlarmUtil {

    /* 명언 알람 등록 (매일 반복) */
    fun setQuoteAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        requestCode: Int
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /* 명언 알람 취소 */
    fun cancelQuoteAlarm(
        context: Context,
        requestCode: Int
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MyAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /* 루틴 시작 전 알람 등록 (15, 10, 5분 전) */
    fun setRoutinePreAlarms(
        context: Context,
        year: Int,
        month: Int,   // Calendar 기준 (1월 = 0)
        day: Int,
        hour: Int,
        minute: Int,
        routineTitle: String,
        baseRequestCode: Int
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val minutesList = listOf(15, 10, 5)

        minutesList.forEachIndexed { index, beforeMinute ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                add(Calendar.MINUTE, -beforeMinute)
            }

            if (calendar.timeInMillis < System.currentTimeMillis()) return@forEachIndexed

            val intent = Intent(context, MyAlarmReceiver::class.java).apply {
                putExtra(
                    "content",
                    "⏰ '$routineTitle' 루틴 시작 ${beforeMinute}분 전이에요!"
                )
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                baseRequestCode + index,
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

    /* 루틴 사전 알람 취소 */
    fun cancelRoutinePreAlarms(
        context: Context,
        baseRequestCode: Int
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        repeat(3) { index ->
            val intent = Intent(context, MyAlarmReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                baseRequestCode + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
        }
    }

    /* 명언 + 루틴 알람 전부 취소 (선택) */
    fun cancelAllMyAlarms(context: Context) {
        // 명언 알람 3개
        cancelQuoteAlarm(context, 2001)
        cancelQuoteAlarm(context, 2002)
        cancelQuoteAlarm(context, 2003)

        // 예시로 루틴 baseRequestCode = 3000
        cancelRoutinePreAlarms(context, 3000)
    }
}

package com.example.miraclemorning.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.miraclemorning.alarm.AlarmReceiver
import java.util.Calendar

object MyAlarmUtil {

    // ===============================
    // 공통: 안전하게 알람 설정
    // ===============================
    private fun setSafeAlarm(
        context: Context,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ : 정확 알람 권한 체크
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    // 권한 없으면 일반 알람으로 폴백 (크래시 방지)
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                // Android 11 이하
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // 혹시 모를 크래시 방지
            e.printStackTrace()
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    // ===============================
    // ✅ 명언 알람 설정
    // ===============================
    @SuppressLint("ScheduleExactAlarm")
    fun setQuoteAlarm(context: Context, hour: Int, minute: Int, requestCode: Int) {
        val intent = Intent(context, MyAlarmReceiver::class.java).apply {
            putExtra("content", "명언 알림")
            putExtra("requestCode", requestCode)
        }

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

            // 이미 지난 시간이면 다음 날
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        setSafeAlarm(context, calendar.timeInMillis, pendingIntent)
    }

    // ===============================
    // ✅ 루틴 시작 전 알림 3개 (15, 10, 5분 전)
    // ===============================
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

            setSafeAlarm(context, calendar.timeInMillis, pendingIntent)
        }
    }

    // ===============================
    // ✅ 루틴 알람 3개 모두 취소
    // ===============================
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

    // ===============================
    // ✅ 모든 명언 알람 취소 (SettingsActivity용)
    // ===============================
    fun cancelAllMyAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 명언 알람 requestCode들 (네가 쓰는 값들)
        val requestCodes = listOf(
            2001, // 오전 6시
            2002, // 오후 12시
            2003  // 오후 8시
        )

        for (code in requestCodes) {
            val intent = Intent(context, MyAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}



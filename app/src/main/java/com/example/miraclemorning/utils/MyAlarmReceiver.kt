package com.example.miraclemorning.utils

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.miraclemorning.MainActivity
import com.example.miraclemorning.R
import com.example.miraclemorning.alarm.RoutineStartReceiver
import com.example.miraclemorning.utils.QuoteProvider
import com.example.miraclemorning.utils.RoutineStateManager

class MyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra("content") ?: "루틴 알림"
        val requestCode = intent.getIntExtra("requestCode", 0)
        val routineId = intent.getIntExtra("routineId", -1)

        // ✅ 루틴이 이미 시작된 경우 알림 무시
        if (routineId != -1 && RoutineStateManager.isStarted(context, routineId)) {
            return
        }

        // ✅ 알림 권한 체크 (Android 13+에서 앱 꺼짐 방지)
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 없으면 알림 보내지 않고 그냥 종료 (앱 안 꺼짐)
            return
        }

        val quote = QuoteProvider.getRandomQuote()

        // ✅ 루틴 시작 버튼 액션
        val startIntent = Intent(context, RoutineStartReceiver::class.java).apply {
            putExtra("routineId", routineId)
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            routineId,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ 앱 열기 액션
        val openAppIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullText = "☑ $content\n\n🧠 오늘의 명언:\n\"$quote\""

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("루틴 알림")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setContentIntent(mainPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "루틴 시작",
                startPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // ✅ SecurityException 방지용 try-catch
        try {
            NotificationManagerCompat.from(context).notify(requestCode, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}


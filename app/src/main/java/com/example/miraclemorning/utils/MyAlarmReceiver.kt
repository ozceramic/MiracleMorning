package com.example.miraclemorning.alarm

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.miraclemorning.R
import com.example.miraclemorning.MainActivity
import com.example.miraclemorning.utils.QuoteProvider
import com.example.miraclemorning.utils.RoutineStateManager

class MyAlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra("content") ?: "루틴 알림"
        val requestCode = intent.getIntExtra("requestCode", -1)
        val routineId = intent.getIntExtra("routineId", -1)

        
        if (routineId != -1 && RoutineStateManager.isStarted(context, routineId)) {
            return
        }

        
        val quote = QuoteProvider.getRandomQuote()

        
        val startIntent = Intent(context, RoutineStartReceiver::class.java).apply {
            putExtra("routineId", routineId)
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            routineId,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        
        val openAppIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullText = "\u2611 $content\n\n\uD83E\uDDE0 오늘의 명언: \n\"$quote\""

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("루틴 알림")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "루틴 시작", startPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }
}

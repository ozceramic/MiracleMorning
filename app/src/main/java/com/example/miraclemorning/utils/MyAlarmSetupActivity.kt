package com.example.miraclemorning.utils

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.miraclemorning.R

class MyAlarmSetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_alarm_setup)

        findViewById<Button>(R.id.btnSetAlarms).setOnClickListener {
            MyAlarmUtil.setQuoteAlarm(this, 6, 0, 2001)
            MyAlarmUtil.setQuoteAlarm(this, 12, 0, 2002)
            MyAlarmUtil.setQuoteAlarm(this, 20, 0, 2003)
        }
    }}

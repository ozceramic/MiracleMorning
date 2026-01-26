package com.example.miraclemorning.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.miraclemorning.R
import com.example.miraclemorning.utils.MyAlarmUtil
import com.example.miraclemorning.MainActivity
class QuoteAlarmActivity : AppCompatActivity() {

    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quote_alarm)

        val tvSelectedTime = findViewById<TextView>(R.id.tvSelectedTime)
        val btnPickTime = findViewById<Button>(R.id.btnPickTime)
        val btnSetAlarm = findViewById<Button>(R.id.btnSetQuoteAlarm)
        val btnBack = findViewById<Button>(R.id.btnBackToMain)

        updateTimeText(tvSelectedTime)

        btnPickTime.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                updateTimeText(tvSelectedTime)
            }

            TimePickerDialog(
                this,
                timeSetListener,
                6, 0,
                false
            ).show()
        }

        btnSetAlarm.setOnClickListener {
            if (selectedHour != -1 && selectedMinute != -1) {
                MyAlarmUtil.setQuoteAlarm(this, selectedHour, selectedMinute, 200)
                Toast.makeText(this, "알람이 설정되었습니다!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "시간을 먼저 선택해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateTimeText(tv: TextView) {
        if (selectedHour == -1 || selectedMinute == -1) {
            tv.text = "선택된 시간: 없음"
        } else {
            val amPm = if (selectedHour < 12) "오전" else "오후"
            val hour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            val timeStr = String.format("%s %d:%02d", amPm, hour, selectedMinute)
            tv.text = "선택된 시간: $timeStr"
        }
    }
}



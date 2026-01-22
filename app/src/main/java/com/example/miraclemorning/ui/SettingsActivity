package com.example.miraclemorning.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.miraclemorning.R
import com.example.miraclemorning.utils.MyAlarmUtil

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnCancelAlarms = findViewById<Button>(R.id.btnCancelAlarms)

        btnCancelAlarms.setOnClickListener {
            MyAlarmUtil.cancelAllMyAlarms(this)
            Toast.makeText(this, "알림이 모두 취소되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.miraclemorning.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.miraclemorning.R
import com.example.miraclemorning.data.ScheduleDBHelper
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: ScheduleDBHelper

    private var scheduleId: Long = -1L
    private var date: String = ""
    private var time: String = ""

    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var etContent: EditText

    private lateinit var btnEditTime: Button
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var btnRoutineRange: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = ScheduleDBHelper(this)

        scheduleId = intent.getLongExtra("id", -1L)
        date = intent.getStringExtra("date") ?: ""
        time = intent.getStringExtra("time") ?: ""
        val content = intent.getStringExtra("content") ?: ""

        if (scheduleId == -1L) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        etContent = findViewById(R.id.etContent)

        btnEditTime = findViewById(R.id.btnEditTime)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        btnRoutineRange = findViewById(R.id.btnRoutineRange)

        tvDate.text = date
        tvTime.text = time
        etContent.setText(content)

        // 시간 변경
        btnEditTime.setOnClickListener {
            val parts = time.split(":")
            val initH = parts.getOrNull(0)?.toIntOrNull() ?: 7
            val initM = parts.getOrNull(1)?.toIntOrNull() ?: 0

            TimePickerDialog(this, { _, h, m ->
                time = String.format("%02d:%02d", h, m)
                tvTime.text = time
            }, initH, initM, true).show()
        }

        // 저장(수정)
        btnSave.setOnClickListener {
            val newContent = etContent.text.toString().trim()
            if (newContent.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dbHelper.updateSchedule(scheduleId, date, time, newContent)

            setResult(RESULT_OK)
            finish()
        }

        // 삭제(현재 항목만 삭제)
        btnDelete.setOnClickListener {
            dbHelper.deleteSchedule(scheduleId)

            setResult(RESULT_OK)
            finish()
        }

        // 루틴 등록(기간 선택 → 해당 날짜들에 자동 생성)
        btnRoutineRange.setOnClickListener {
            val contentNow = etContent.text.toString().trim()
            if (contentNow.isEmpty()) {
                Toast.makeText(this, "루틴 내용(할 일)을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1) 달력으로 기간 선택
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("루틴 기간 선택")
                .build()

            picker.addOnPositiveButtonClickListener { range ->
                val startMillis = range.first ?: return@addOnPositiveButtonClickListener
                val endMillis = range.second ?: return@addOnPositiveButtonClickListener

                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = fmt.format(Date(startMillis))
                val endDate = fmt.format(Date(endMillis))

                // 2) 시간 선택(루틴 시간)
                val nowParts = time.split(":")
                val initH = nowParts.getOrNull(0)?.toIntOrNull() ?: 7
                val initM = nowParts.getOrNull(1)?.toIntOrNull() ?: 0

                TimePickerDialog(this, { _, h, m ->
                    val routineTime = String.format("%02d:%02d", h, m)

                    val count = dbHelper.insertRoutineRange(
                        startDate = startDate,
                        endDate = endDate,
                        time = routineTime,
                        content = contentNow
                    )

                    Toast.makeText(this, "루틴 ${count}일 등록 완료", Toast.LENGTH_SHORT).show()

                    setResult(RESULT_OK)
                    finish()

                }, initH, initM, true).show()
            }

            picker.show(supportFragmentManager, "rangePicker")
        }
    }
}

package com.example.miraclemorning

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miraclemorning.alarm.AlarmUtil
import com.example.miraclemorning.data.Schedule
import com.example.miraclemorning.data.ScheduleDBHelper
import com.example.miraclemorning.ui.ScheduleAdapter
import com.example.miraclemorning.ui.ScheduleDetailActivity
import com.example.miraclemorning.utils.MyAlarmUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: ScheduleDBHelper
    private lateinit var calendarView: CalendarView
    private lateinit var etContent: EditText
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter

    private val scheduleList = mutableListOf<Schedule>()
    private var selectedDate: String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()
        requestNotificationPermission()

        dbHelper = ScheduleDBHelper(this)

        calendarView = findViewById(R.id.calendarView)
        etContent = findViewById(R.id.etContent)
        btnAdd = findViewById(R.id.btnAdd)
        recyclerView = findViewById(R.id.recyclerView)

        setupRecyclerView()
        loadSchedules()

        calendarView.setOnDateChangeListener { _, y, m, d ->
            selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            loadSchedules()
        }

        btnAdd.setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "일정 내용을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openTimePicker(content)
        }

        // 명언 알람 등록 버튼 클릭 시
        findViewById<Button>(R.id.btnQuoteAlarms).setOnClickListener {
            MyAlarmUtil.setQuoteAlarm(this, 6, 0, 2001)
            MyAlarmUtil.setQuoteAlarm(this, 12, 0, 2002)
            MyAlarmUtil.setQuoteAlarm(this, 20, 0, 2003)
            Toast.makeText(this, "명언 알람이 설정되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 앱 실행 시 루틴 알람 테스트
        testRoutineAlarms()
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        // 2. 현재 탭 설정 (달력)
        bottomNav.selectedItemId = R.id.nav_calendar
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> true // 현재 화면이므로 아무것도 안 함
                R.id.nav_achievement -> {
                    // 달성률 화면으로 이동
                    val intent = Intent(this, AchievementActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0) // 화면 전환 애니메이션 제거 (자연스럽게)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(
            list = scheduleList,
            db = dbHelper,
            refresh = { loadSchedules() }
        ) { item ->
            val intent = Intent(this, ScheduleDetailActivity::class.java)
            intent.putExtra("id", item.id)
            intent.putExtra("date", item.date)
            intent.putExtra("time", item.time)
            intent.putExtra("content", item.content)
            startActivityForResult(intent, 2001)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadSchedules() {
        scheduleList.clear()
        scheduleList.addAll(dbHelper.getSchedulesByDate(selectedDate))
        adapter.notifyDataSetChanged()
    }

    private fun openTimePicker(content: String) {
        val now = Calendar.getInstance()

        TimePickerDialog(this, { _, hour, minute ->
            val time = String.format("%02d:%02d", hour, minute)
            val insertedId = dbHelper.insertSchedule(selectedDate, time, content)

            if (insertedId == -1L) {
                Toast.makeText(this, "DB 저장 실패", Toast.LENGTH_SHORT).show()
                return@TimePickerDialog
            }

            // 알람 설정 (실패해도 앱은 계속 돌아가게)
            try {
                val parts = selectedDate.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()

                AlarmUtil.setAlarm(
                    this,
                    year,
                    month,
                    day,
                    hour,
                    minute,
                    content,
                    insertedId.toInt()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            etContent.setText("")
            loadSchedules()
            Toast.makeText(this, "일정이 저장되었습니다.", Toast.LENGTH_SHORT).show()

        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            loadSchedules()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "일정 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "미라클 모닝 알람 채널" }

            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    // 앱 실행 시 루틴 알람 3개 테스트용 예약
    private fun testRoutineAlarms() {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH)
        val day = now.get(Calendar.DAY_OF_MONTH)
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE) + 1 // 1분 후 루틴 시작 가정

        MyAlarmUtil.setRoutinePreAlarms(
            context = this,
            year = year,
            month = month,
            day = day,
            hour = hour,
            minute = minute,
            routineContent = "테스트 루틴",
            routineId = 9999
        )
    }
}


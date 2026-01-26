package com.example.miraclemorning

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.miraclemorning.data.ScheduleDBHelper
import java.text.SimpleDateFormat
import java.util.*

class AchievementActivity : AppCompatActivity() {

    private lateinit var dbHelper: ScheduleDBHelper // DB 헬퍼 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        dbHelper = ScheduleDBHelper(this)

        // 1. 하단 내비게이션 바 설정
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_achievement

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_achievement -> true
                else -> false
            }
        }

        // 2. 그래프 그리기 호출
        setupChart()
    }

    private fun setupChart() {
        val pieChart = findViewById<PieChart>(R.id.achievementPieChart)
        val tvSummary = findViewById<TextView>(R.id.tvSummary)

        // 현재 시점까지의 전체 일정과 완료된 일정을 가져옴
        val (totalCount, doneCount) = dbHelper.getAchievementRateUntilNow()

        // 데이터가 없을 때 0으로 나누기 방지
        if (totalCount == 0) {
            tvSummary.text = "현재 시점까지 예정된 일정이 없습니다."
            pieChart.clear()
            pieChart.setNoDataText("표시할 데이터가 없습니다.")
            return
        }

        // 달성률 계산 (현재 시점 기준)
        val rate = (doneCount.toFloat() / totalCount.toFloat() * 100).toInt()
        tvSummary.text = "현재까지 예정된 ${totalCount}개 중 ${doneCount}개를 완료하여\n달성률 ${rate}%를 기록 중입니다!"

        // 그래프 데이터 설정
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(doneCount.toFloat(), "달성"))
        entries.add(PieEntry((totalCount - doneCount).toFloat(), "미달성"))

        val dataSet = PieDataSet(entries, "").apply {
            // 달성: 초록색, 미달성: 빨간색
            colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#FF5252"))
            valueTextSize = 14f
            valueTextColor = Color.WHITE
        }

        pieChart.apply {
            data = PieData(dataSet)
            centerText = "현재 기준\n${rate}%"
            setCenterTextSize(20f)
            description.isEnabled = false // 설명 라벨 숨기기
            animateY(1000) // 애니메이션 효과
            invalidate() // 화면 갱신
        }
    }
}
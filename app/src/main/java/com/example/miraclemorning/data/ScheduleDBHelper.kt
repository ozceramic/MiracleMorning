package com.example.miraclemorning.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleDBHelper(context: Context) :
    SQLiteOpenHelper(context, "schedule.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE schedule (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date TEXT," +
                    "time TEXT," +
                    "content TEXT," +
                    "isDone INTEGER DEFAULT 0," +
                    //  (date, time, content) 동일한 일정은 1개만 허용
                    "UNIQUE(date, time, content)" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE schedule ADD COLUMN isDone INTEGER DEFAULT 0")
        }

        // 기존에 이미 생성된 DB에도 중복 방지 적용
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_schedule_unique " +
                    "ON schedule(date, time, content)"
        )
    }

    fun insertSchedule(date: String, time: String, content: String): Long {
        val values = ContentValues().apply {
            put("date", date)
            put("time", time)
            put("content", content)
            put("isDone", 0)
        }

        //  중복이면 무시(필요없으면 insert로 바꿔도 됨)
        return writableDatabase.insertWithOnConflict(
            "schedule",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun getSchedulesByDate(date: String): List<Schedule> {
        val cursor = readableDatabase.query(
            "schedule",
            arrayOf("id", "date", "time", "content", "isDone"),
            "date=?",
            arrayOf(date),
            null,
            null,
            "time ASC"
        )

        val list = mutableListOf<Schedule>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val time = cursor.getString(cursor.getColumnIndexOrThrow("time"))
            val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
            val isDone = cursor.getInt(cursor.getColumnIndexOrThrow("isDone"))
            list.add(Schedule(id, date, time, content, isDone))
        }
        cursor.close()
        return list
    }

    fun updateSchedule(id: Long, date: String, time: String, content: String) {
        val values = ContentValues().apply {
            put("date", date)
            put("time", time)
            put("content", content)
        }
        writableDatabase.update("schedule", values, "id=?", arrayOf(id.toString()))
    }

    fun updateDone(id: Long, isDone: Int) {
        val values = ContentValues().apply { put("isDone", isDone) }
        writableDatabase.update("schedule", values, "id=?", arrayOf(id.toString()))
    }

    fun deleteSchedule(id: Long) {
        writableDatabase.delete("schedule", "id=?", arrayOf(id.toString()))
    }

    //(date,time,content) 중복이면 자동 무시(CONFLICT_IGNORE) → 오늘이 2번 들어가는 기존 문제 해결
    fun insertRoutineRange(startDate: String, endDate: String, time: String, content: String): Int {
        val db = writableDatabase
        db.beginTransaction()

        var count = 0
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = Calendar.getInstance().apply { this.time = fmt.parse(startDate)!! }
            val end = Calendar.getInstance().apply { this.time = fmt.parse(endDate)!! }

            // start > end면 swap
            if (start.after(end)) {
                val tmp = start.time
                start.time = end.time
                end.time = tmp
            }

            while (!start.after(end)) {
                val d = fmt.format(start.time)

                val values = ContentValues().apply {
                    put("date", d)
                    put("time", time)
                    put("content", content)
                    put("isDone", 0)
                }

                // 중복이면 rowId = -1 반환(무시됨)
                val rowId = db.insertWithOnConflict(
                    "schedule",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
                if (rowId != -1L) count++

                start.add(Calendar.DAY_OF_MONTH, 1)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return count
    }
    fun getAchievementRateUntilNow(): Pair<Int, Int> {
        val db = readableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val now = sdf.format(Calendar.getInstance().time)
        val nowDate = now.substring(0, 10) // yyyy-MM-dd
        val nowTime = now.substring(11)    // HH:mm

        // 조건: 날짜가 오늘보다 이전이거나, 날짜는 오늘인데 시간이 현재 시간보다 이전인 경우
        val whereClause = "date < ? OR (date = ? AND time <= ?)"
        val whereArgs = arrayOf(nowDate, nowDate, nowTime)

        // 1. 현재까지 진행했어야 할 전체 일정 개수
        val totalCursor = db.rawQuery(
            "SELECT COUNT(*) FROM schedule WHERE $whereClause",
            whereArgs
        )
        totalCursor.moveToFirst()
        val totalCount = totalCursor.getInt(0)
        totalCursor.close()

        // 2. 그 중 완료(isDone = 1)된 일정 개수
        val doneCursor = db.rawQuery(
            "SELECT COUNT(*) FROM schedule WHERE ($whereClause) AND isDone = 1",
            whereArgs
        )
        doneCursor.moveToFirst()
        val doneCount = doneCursor.getInt(0)
        doneCursor.close()

        return Pair(totalCount, doneCount)
    }
}

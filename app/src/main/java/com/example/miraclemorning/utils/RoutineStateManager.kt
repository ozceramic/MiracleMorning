package com.example.miraclemorning.utils

import android.content.Context
import android.content.SharedPreferences

object RoutineStateManager {
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("routine_state", Context.MODE_PRIVATE)

    fun isStarted(context: Context, routineId: Int): Boolean =
        prefs(context).getBoolean("routine_started_$routineId", false)

    fun setStarted(context: Context, routineId: Int) {
        prefs(context).edit().putBoolean("routine_started_$routineId", true).apply()
    }

    fun clearStarted(context: Context, routineId: Int) {
        prefs(context).edit().remove("routine_started_$routineId").apply()
    }
}

package com.example.stepcounter

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val PREF = "theme_pref"
    private const val KEY = "theme"

    enum class Mode(val value: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun save(context: Context, mode: Mode) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, mode.name)
            .apply()
    }

    fun load(context: Context): Mode {
        val name = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, Mode.SYSTEM.name)
        return Mode.valueOf(name!!)
    }

    fun apply(mode: Mode) {
        AppCompatDelegate.setDefaultNightMode(mode.value)
    }
}
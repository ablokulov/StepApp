package com.example.stepcounter

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.stepcounter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val statsFragment = StatsFragment()
    private val historyFragment = HistoryFragment()
    private val settingsFragment = SettingsFragment()

    private val PREFS = "StepPrefs"
    private val KEY_INIT = "initSteps"
    private val KEY_DAILY = "dailySteps"

    private val isDark get() = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(ThemeManager.load(this))
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bottom nav fon rangini tema bo'yicha o'rnatish
        binding.bottomNav.setBackgroundColor(
            if (isDark) android.graphics.Color.parseColor("#080F18")
            else android.graphics.Color.parseColor("#F0F4FF")
        )

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
            setActiveTab(0)
        }

        binding.navHome.setOnClickListener { loadFragment(homeFragment); setActiveTab(0) }
        binding.navStats.setOnClickListener { loadFragment(statsFragment); setActiveTab(1) }
        binding.navHistory.setOnClickListener { loadFragment(historyFragment); setActiveTab(2) }
        binding.navSettings.setOnClickListener { loadFragment(settingsFragment); setActiveTab(3) }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setActiveTab(index: Int) {
        val activeColor = if (isDark)
            android.graphics.Color.parseColor("#00B4FF")
        else
            android.graphics.Color.parseColor("#4F46E5")

        val inactiveColor = if (isDark)
            android.graphics.Color.parseColor("#44FFFFFF")
        else
            android.graphics.Color.parseColor("#94A3B8")

        val icons = listOf(
            binding.icHome, binding.icStats,
            binding.icHistory, binding.icSettings
        )
        val labels = listOf(
            binding.lblHome, binding.lblStats,
            binding.lblHistory, binding.lblSettings
        )
        for (i in 0..3) {
            val color = if (i == index) activeColor else inactiveColor
            icons[i].setColorFilter(color)
            labels[i].setTextColor(color)
        }
    }

    fun resetSteps() {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_INIT, -1L)
            .putInt(KEY_DAILY, 0)
            .apply()
        Toast.makeText(this, "Qadamlar nollandi!", Toast.LENGTH_SHORT).show()
    }
}
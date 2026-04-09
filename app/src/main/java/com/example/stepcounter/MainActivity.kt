package com.example.stepcounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.stepcounter.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager

    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var sensorActive = false

    private val PREFS = "StepPrefs"
    private val KEY_INIT = "initSteps"
    private val KEY_DAILY = "dailySteps"
    private val KEY_DATE = "lastDate"
    private val KEY_GOAL = "goal"
    private val KEY_WEEK = "weekData"

    private var initialSteps = -1L
    var currentSteps = 0
    var dailyGoal = 10000
    var weekSteps = IntArray(7)

    private val homeFragment = HomeFragment()
    private val statsFragment = StatsFragment()
    private val historyFragment = HistoryFragment()
    private val settingsFragment = SettingsFragment()

    private val isDark get() = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) registerSensors()
        else Toast.makeText(this, "Ruxsat kerak!", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(ThemeManager.load(this))
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        loadData()
        setupNavColors()
        setupHomeFragment()

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
            setActiveTab(0)
        }

        binding.navHome.setOnClickListener { loadFragment(homeFragment); setActiveTab(0) }
        binding.navStats.setOnClickListener { loadFragment(statsFragment); setActiveTab(1) }
        binding.navHistory.setOnClickListener { loadFragment(historyFragment); setActiveTab(2) }
        binding.navSettings.setOnClickListener { loadFragment(settingsFragment); setActiveTab(3) }

        checkPermission()
    }

    private fun setupHomeFragment() {
        homeFragment.onGoalChanged = { newGoal ->
            dailyGoal = newGoal
            updateHomeUI()
        }
    }

    private fun loadData() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString(KEY_DATE, "")

        val weekStr = prefs.getString(KEY_WEEK, "0,0,0,0,0,0,0") ?: "0,0,0,0,0,0,0"
        weekSteps = weekStr.split(",").map { it.toIntOrNull() ?: 0 }.toIntArray()

        if (lastDate != today) {
            for (i in 0..5) weekSteps[i] = weekSteps[i + 1]
            weekSteps[6] = 0
            prefs.edit()
                .putString(KEY_DATE, today)
                .putLong(KEY_INIT, -1L)
                .putInt(KEY_DAILY, 0)
                .putString(KEY_WEEK, weekSteps.joinToString(","))
                .apply()
            initialSteps = -1L
            currentSteps = 0
        } else {
            initialSteps = prefs.getLong(KEY_INIT, -1L)
            currentSteps = prefs.getInt(KEY_DAILY, 0)
        }
        dailyGoal = prefs.getInt(KEY_GOAL, 10000)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
                registerSensors()
            } else {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            registerSensors()
        }
    }

    private fun registerSensors() {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        when {
            stepCounterSensor != null -> {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
                sensorActive = true
            }
            stepDetectorSensor != null -> {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
                sensorActive = true
            }
            else -> {
                sensorActive = false
            }
        }
        updateHomeUI()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0].toLong()
                if (initialSteps == -1L) {
                    initialSteps = total
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().putLong(KEY_INIT, initialSteps).apply()
                }
                currentSteps = (total - initialSteps).toInt()
                saveDaily(currentSteps)
                updateHomeUI()
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                currentSteps++
                saveDaily(currentSteps)
                updateHomeUI()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun updateHomeUI() {
        weekSteps[6] = currentSteps
        if (homeFragment.isAdded) {
            homeFragment.updateUI(currentSteps, dailyGoal, weekSteps, sensorActive)
        }
    }

    fun resetSteps() {
        currentSteps = 0
        initialSteps = -1L
        getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_INIT, -1L)
            .putInt(KEY_DAILY, 0)
            .apply()
        updateHomeUI()
        Toast.makeText(this, "Qadamlar nollandi!", Toast.LENGTH_SHORT).show()
    }

    private fun saveDaily(v: Int) {
        weekSteps[6] = v
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_DAILY, v)
            .putString(KEY_WEEK, weekSteps.joinToString(","))
            .apply()
    }

    private fun setupNavColors() {
        binding.bottomNav.setBackgroundColor(
            if (isDark) android.graphics.Color.parseColor("#080F18")
            else android.graphics.Color.parseColor("#F0F4FF")
        )
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setActiveTab(index: Int) {
        val activeColor = if (isDark) android.graphics.Color.parseColor("#00B4FF")
        else android.graphics.Color.parseColor("#4F46E5")
        val inactiveColor = if (isDark) android.graphics.Color.parseColor("#44FFFFFF")
        else android.graphics.Color.parseColor("#94A3B8")

        val icons = listOf(binding.icHome, binding.icStats, binding.icHistory, binding.icSettings)
        val labels = listOf(binding.lblHome, binding.lblStats, binding.lblHistory, binding.lblSettings)

        for (i in 0..3) {
            val color = if (i == index) activeColor else inactiveColor
            icons[i].setColorFilter(color)
            labels[i].setTextColor(color)
        }
    }

    override fun onResume() {
        super.onResume()
        stepCounterSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        stepDetectorSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        updateHomeUI()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
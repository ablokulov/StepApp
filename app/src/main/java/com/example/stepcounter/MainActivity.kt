package com.example.stepcounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.stepcounter.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager

    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private val PREFS = "StepPrefs"
    private val KEY_INIT = "initSteps"
    private val KEY_DAILY = "dailySteps"
    private val KEY_DATE = "lastDate"
    private val KEY_GOAL = "goal"
    private val KEY_WEEK = "weekData"

    private var initialSteps = -1L
    private var currentSteps = 0
    private var dailyGoal = 10000
    private var weekSteps = IntArray(7)

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
        setupUI()
        checkPermission()
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

    private fun setupUI() {
        updateUI()
        binding.btnSetGoal.setOnClickListener { showGoalDialog() }
        binding.btnReset.setOnClickListener { showThemeDialog() }
        binding.btnNollash.setOnClickListener { resetSteps() }
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
                binding.tvSensorStatus.text = "● Faol"
            }
            stepDetectorSensor != null -> {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
                binding.tvSensorStatus.text = "● Faol"
            }
            else -> {
                binding.tvSensorStatus.text = "○ Sensor yo'q"
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0].toLong()
                if (initialSteps == -1L) {
                    initialSteps = total
                    saveInitial(initialSteps)
                }
                currentSteps = (total - initialSteps).toInt()
                saveDaily(currentSteps)
                updateUI()
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                currentSteps++
                saveDaily(currentSteps)
                updateUI()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateUI() {
        val pct = ((currentSteps.toFloat() / dailyGoal) * 100).toInt().coerceAtMost(100)
        val calories = (currentSteps * 0.04).toInt()
        val distanceKm = currentSteps * 0.762 / 1000.0
        val minutes = currentSteps / 100

        binding.ringView.setProgress(pct)
        binding.tvStepCount.text = "%,d".format(currentSteps)
        binding.tvPercent.text = "$pct%"
        binding.tvGoalSub.text = "/ %,d maqsad".format(dailyGoal)

        binding.tvCalories.text = "$calories"
        binding.tvDistance.text = "%.2f".format(distanceKm)
        binding.tvMinutes.text = "$minutes"

        binding.calRing.setProgress((calories.toFloat() / 800 * 100).toInt().coerceAtMost(100))
        binding.distRing.setProgress((distanceKm / 15 * 100).toInt().coerceAtMost(100))
        binding.timeRing.setProgress((minutes.toFloat() / 180 * 100).toInt().coerceAtMost(100))

        binding.tvMotivation.text = when {
            currentSteps == 0 -> "Yuring! Sog'lik – boylik!"
            currentSteps < dailyGoal / 4 -> "Ajoyib boshladingiz!"
            currentSteps < dailyGoal / 2 -> "Yarmi keldi! Davom eting!"
            currentSteps < dailyGoal -> "Maqsadga yaqinlashyapsiz!"
            else -> "Maqsadga yetdingiz! Barakalla!"
        }

        weekSteps[6] = currentSteps
        binding.weekChart.setData(weekSteps, dailyGoal)
    }

    private fun showGoalDialog() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(dailyGoal.toString())
            setPadding(32, 24, 32, 24)
        }
        AlertDialog.Builder(this, R.style.NeonDialogTheme)
            .setTitle("Kunlik maqsad")
            .setView(input)
            .setPositiveButton("Saqlash") { _, _ ->
                val v = input.text.toString().toIntOrNull()
                if (v != null && v > 0) {
                    dailyGoal = v
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().putInt(KEY_GOAL, v).apply()
                    updateUI()
                }
            }
            .setNegativeButton("Bekor", null)
            .show()
    }

    private fun showThemeDialog() {
        val modes = arrayOf("☀️ Yorug'", "🌙 Qorong'u", "📱 Tizim")
        val current = ThemeManager.load(this).ordinal

        AlertDialog.Builder(this, R.style.NeonDialogTheme)
            .setTitle("Tema tanlash")
            .setSingleChoiceItems(modes, current) { dialog, which ->
                val mode = ThemeManager.Mode.values()[which]
                ThemeManager.save(this, mode)
                ThemeManager.apply(mode)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Bekor", null)
            .show()
    }

    private fun resetSteps() {
        AlertDialog.Builder(this, R.style.NeonDialogTheme)
            .setTitle("Nollash")
            .setMessage("Bugungi qadamlarni noldan boshlaysizmi?")
            .setPositiveButton("Ha") { _, _ ->
                currentSteps = 0
                initialSteps = -1L
                getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putLong(KEY_INIT, -1L)
                    .putInt(KEY_DAILY, 0)
                    .apply()
                updateUI()
                Toast.makeText(this, "Qadamlar nollandi!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Yo'q", null)
            .show()
    }

    private fun saveInitial(v: Long) =
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(KEY_INIT, v).apply()

    private fun saveDaily(v: Int) {
        weekSteps[6] = v
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_DAILY, v)
            .putString(KEY_WEEK, weekSteps.joinToString(","))
            .apply()
    }

    override fun onResume() {
        super.onResume()
        stepCounterSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        stepDetectorSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
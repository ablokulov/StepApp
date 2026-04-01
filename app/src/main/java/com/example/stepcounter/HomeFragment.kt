package com.example.stepcounter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.stepcounter.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val PREFS = "StepPrefs"
    private val KEY_GOAL = "goal"
    private val KEY_WEEK = "weekData"

    var onGoalChanged: ((Int) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSetGoal.setOnClickListener { showGoalDialog() }
        binding.btnReset.setOnClickListener { showThemeDialog() }
        binding.btnNollash.setOnClickListener { showResetDialog() }
    }

    fun updateUI(steps: Int, goal: Int, weekSteps: IntArray, sensorActive: Boolean) {
        val pct = ((steps.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        val calories = (steps * 0.04).toInt()
        val distanceKm = steps * 0.762 / 1000.0
        val minutes = steps / 100

        binding.ringView.setProgress(pct)
        binding.tvStepCount.text = "%,d".format(steps)
        binding.tvPercent.text = "$pct%"
        binding.tvGoalSub.text = "/ %,d maqsad".format(goal)
        binding.tvCalories.text = "$calories"
        binding.tvDistance.text = "%.2f".format(distanceKm)
        binding.tvMinutes.text = "$minutes"

        binding.calRing.setProgress((calories.toFloat() / 800 * 100).toInt().coerceAtMost(100))
        binding.distRing.setProgress((distanceKm / 15 * 100).toInt().coerceAtMost(100))
        binding.timeRing.setProgress((minutes.toFloat() / 180 * 100).toInt().coerceAtMost(100))

        binding.tvSensorStatus.text = if (sensorActive) "● Faol" else "○ Sensor yo'q"

        binding.tvMotivation.text = when {
            steps == 0 -> "Yuring! Sog'lik – boylik!"
            steps < goal / 4 -> "Ajoyib boshladingiz!"
            steps < goal / 2 -> "Yarmi keldi! Davom eting!"
            steps < goal -> "Maqsadga yaqinlashyapsiz!"
            else -> "Maqsadga yetdingiz! Barakalla!"
        }

        binding.weekChart.setData(weekSteps, goal)
    }

    private fun showGoalDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            setText(prefs.getInt(KEY_GOAL, 10000).toString())
            setPadding(32, 24, 32, 24)
        }
        AlertDialog.Builder(requireContext(), R.style.NeonDialogTheme)
            .setTitle("Kunlik maqsad")
            .setView(input)
            .setPositiveButton("Saqlash") { _, _ ->
                val v = input.text.toString().toIntOrNull()
                if (v != null && v > 0) {
                    requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().putInt(KEY_GOAL, v).apply()
                    onGoalChanged?.invoke(v)
                }
            }
            .setNegativeButton("Bekor", null)
            .show()
    }

    private fun showThemeDialog() {
        val modes = arrayOf("☀️ Yorug'", "🌙 Qorong'u", "📱 Tizim")
        val current = ThemeManager.load(requireContext()).ordinal
        AlertDialog.Builder(requireContext(), R.style.NeonDialogTheme)
            .setTitle("Tema tanlash")
            .setSingleChoiceItems(modes, current) { dialog, which ->
                val mode = ThemeManager.Mode.values()[which]
                ThemeManager.save(requireContext(), mode)
                ThemeManager.apply(mode)
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton("Bekor", null)
            .show()
    }

    private fun showResetDialog() {
        AlertDialog.Builder(requireContext(), R.style.NeonDialogTheme)
            .setTitle("Nollash")
            .setMessage("Bugungi qadamlarni noldan boshlaysizmi?")
            .setPositiveButton("Ha") { _, _ ->
                (requireActivity() as? MainActivity)?.resetSteps()
            }
            .setNegativeButton("Yo'q", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
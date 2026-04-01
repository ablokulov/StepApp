package com.example.stepcounter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.stepcounter.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val PREFS = "StepPrefs"
    private val KEY_GOAL = "goal"
    private val KEY_WEIGHT = "weight"
    private val KEY_STRIDE = "stride"

    var onGoalChanged: ((Int) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadValues()
        setupClicks()
    }

    private fun loadValues() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = prefs.getInt(KEY_GOAL, 10000)
        val weight = prefs.getInt(KEY_WEIGHT, 70)
        val stride = prefs.getFloat(KEY_STRIDE, 0.762f)

        binding.tvGoalValue.text = "%,d qadam".format(goal)
        binding.tvWeightValue.text = "$weight kg"
        binding.tvStrideValue.text = "%.3f m".format(stride)

        val theme = ThemeManager.load(requireContext())
        binding.tvThemeValue.text = when (theme) {
            ThemeManager.Mode.LIGHT -> "☀️ Yorug'"
            ThemeManager.Mode.DARK -> "🌙 Qorong'u"
            ThemeManager.Mode.SYSTEM -> "📱 Tizim"
        }
    }

    private fun setupClicks() {
        binding.itemGoal.setOnClickListener { showGoalDialog() }
        binding.itemTheme.setOnClickListener { showThemeDialog() }
        binding.itemWeight.setOnClickListener { showWeightDialog() }
        binding.itemStride.setOnClickListener { showStrideDialog() }
    }

    private fun showGoalDialog() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt(KEY_GOAL, 10000).toString())
            setPadding(32, 24, 32, 24)
        }
        AlertDialog.Builder(requireContext(), R.style.NeonDialogTheme)
            .setTitle("Kunlik maqsad")
            .setView(input)
            .setPositiveButton("Saqlash") { _, _ ->
                val v = input.text.toString().toIntOrNull()
                if (v != null && v > 0) {
                    prefs.edit().putInt(KEY_GOAL, v).apply()
                    binding.tvGoalValue.text = "%,d qadam".format(v)
                    onGoalChanged?.invoke(v)
                }
            }
            .setNegativeButton("Bekor", null).show()
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
            .setNegativeButton("Bekor", null).show()
    }

    private fun showWeightDialog() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt(KEY_WEIGHT, 70).toString())
            setPadding(32, 24, 32, 24)
        }
        AlertDialog.Builder(requireContext(), R.style.NeonDialogTheme)
            .setTitle("Vazn (kg)")
            .setView(input)
            .setPositiveButton("Saqlash") { _, _ ->
                val v = input.text.toString().toIntOrNull()
                if (v != null && v > 0) {
                    prefs.edit().putInt(KEY_WEIGHT, v).apply()
                    binding.tvWeightValue.text = "$v kg"
                }
            }
            .setNegativeButton("Bekor", null).show()
    }

    private fun showStrideDialog() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(prefs.getFloat(KEY_STRIDE, 0.762f).toString())
            setPadding(32, 24, 32, 24)
        }
        AlertDialog.Builder(requireContext(), R.style.NeonDialogTheme)
            .setTitle("Qadam uzunligi (m)")
            .setView(input)
            .setPositiveButton("Saqlash") { _, _ ->
                val v = input.text.toString().toFloatOrNull()
                if (v != null && v > 0) {
                    prefs.edit().putFloat(KEY_STRIDE, v).apply()
                    binding.tvStrideValue.text = "%.3f m".format(v)
                }
            }
            .setNegativeButton("Bekor", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
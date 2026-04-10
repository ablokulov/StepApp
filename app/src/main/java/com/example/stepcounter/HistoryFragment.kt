package com.example.stepcounter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stepcounter.databinding.FragmentHistoryBinding
import com.example.stepcounter.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val PREFS = "StepPrefs"
    private val KEY_WEEK = "weekData"
    private val KEY_GOAL = "goal"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = prefs.getInt(KEY_GOAL, 10000)
        val weekStr = prefs.getString(KEY_WEEK, "0,0,0,0,0,0,0") ?: "0,0,0,0,0,0,0"
        val weekSteps = weekStr.split(",").map { it.toIntOrNull() ?: 0 }.toIntArray()

        // Bugungi qadamni MainActivity dan olish
        val todaySteps = (activity as? MainActivity)?.currentSteps ?: weekSteps[6]
        weekSteps[6] = todaySteps

        updateHistory(weekSteps, goal)
    }

    fun updateHistory(weekSteps: IntArray, goal: Int) {
        val maxSteps = weekSteps.maxOrNull()?.coerceAtLeast(goal) ?: goal
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMM", Locale("uz"))

        val items = weekSteps.mapIndexed { i, steps ->
            val daysAgo = 6 - i
            val label = when (daysAgo) {
                0 -> "Bugun"
                1 -> "Kecha"
                else -> {
                    cal.time = Date()
                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    val result = sdf.format(cal.time)
                    cal.time = Date()
                    result
                }
            }
            Triple(label, steps, goal)
        }.reversed()

        binding.rvHistory.adapter = HistoryAdapter(items, maxSteps)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HistoryAdapter(
    private val items: List<Triple<String, Int, Int>>,
    private val maxSteps: Int
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (label, steps, goal) = items[position]
        holder.binding.tvHistDate.text = label
        holder.binding.tvHistSteps.text = "%,d".format(steps)
        holder.binding.pbHistBar.progress =
            if (maxSteps > 0) (steps.toFloat() / maxSteps * 100).toInt().coerceAtMost(100) else 0

        // Bugun bo'lsa rang farqli
        if (label == "Bugun") {
            holder.binding.tvHistSteps.setTextColor(
                android.graphics.Color.parseColor("#00B4FF")
            )
        } else {
            holder.binding.tvHistSteps.setTextColor(
                android.graphics.Color.WHITE
            )
        }

        // Maqsadga yetgan bo'lsa yashil
        if (steps >= goal) {
            holder.binding.pbHistBar.progressDrawable?.setTint(
                android.graphics.Color.parseColor("#00C878")
            )
        }
    }

    override fun getItemCount() = items.size
}
package com.example.stepcounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.stepcounter.databinding.FragmentStatsBinding

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updateStats(steps: Int, goal: Int, weekSteps: IntArray) {
        val avg = weekSteps.filter { it > 0 }.let { if (it.isEmpty()) 0 else it.average().toInt() }
        val total = weekSteps.sum()
        val calories = (steps * 0.04).toInt()
        val distKm = steps * 0.762 / 1000.0
        val minutes = steps / 100

        binding.tvToday.text = "%,d".format(steps)
        binding.tvAverage.text = "%,d".format(avg)
        binding.tvTotal.text = "%,d".format(total)

        binding.statsChart.setData(weekSteps, goal)

        binding.tvCalStat.text = "$calories / 320 kkal"
        binding.pbCal.progress = (calories.toFloat() / 320 * 100).toInt().coerceAtMost(100)

        binding.tvDistStat.text = "%.2f / 8 km".format(distKm)
        binding.pbDist.progress = (distKm / 8 * 100).toInt().coerceAtMost(100)

        binding.tvTimeStat.text = "$minutes / 60 daq"
        binding.pbTime.progress = (minutes.toFloat() / 60 * 100).toInt().coerceAtMost(100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.stepcounter

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    fun updateHistory(weekSteps: IntArray, goal: Int) {
        val maxSteps = weekSteps.maxOrNull()?.coerceAtLeast(1) ?: goal
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
                    sdf.format(cal.time)
                }
            }
            Pair(label, steps)
        }.reversed()

        binding.rvHistory.adapter = HistoryAdapter(items, maxSteps)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HistoryAdapter(
    private val items: List<Pair<String, Int>>,
    private val maxSteps: Int
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (label, steps) = items[position]
        holder.binding.tvDate.text = label
        holder.binding.tvSteps.text = "%,d".format(steps)
        holder.binding.pbDay.progress = if (maxSteps > 0) (steps.toFloat() / maxSteps * 100).toInt() else 0
    }

    override fun getItemCount() = items.size
}
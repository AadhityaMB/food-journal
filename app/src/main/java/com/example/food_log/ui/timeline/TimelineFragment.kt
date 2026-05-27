package com.example.food_log.ui.timeline

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.food_log.FoodLogApplication
import com.example.food_log.R
import kotlinx.coroutines.launch

class TimelineFragment : Fragment(R.layout.fragment_timeline) {

    private lateinit var adapter: TimelineAdapter
    private lateinit var viewModel: TimelineViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TimelineAdapter(emptyList())
        recyclerView.adapter = adapter

        val app = requireActivity().application as FoodLogApplication

        viewModel = ViewModelProvider(
            this,
            TimelineViewModelFactory(app.foodEntryRepository)
        )[TimelineViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                adapter.updateData(entries)
            }
        }
    }
}
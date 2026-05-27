package com.example.food_log.ui.timeline

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.food_log.FoodLogApplication
import com.example.food_log.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class TimelineFragment : Fragment(R.layout.fragment_timeline) {

    private lateinit var adapter: TimelineAdapter
    private lateinit var viewModel: TimelineViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyStateView = view.findViewById<View>(R.id.emptyStateView)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Pass two click handlers to the adapter.
        adapter = TimelineAdapter(
            onEditClick = { entry ->
                // When the card body is tapped, navigate to Edit Entry
                val bundle = android.os.Bundle().apply {
                    putLong("entryId", entry.foodEntry.id)
                }
                findNavController().navigate(
                    R.id.action_timelineFragment_to_editEntry,
                    bundle
                )
            },
            onRestaurantClick = { restaurantId ->
                // When the restaurant name is tapped, navigate to Restaurant Profile
                val bundle = android.os.Bundle().apply {
                    putLong("restaurantId", restaurantId)
                }
                findNavController().navigate(
                    R.id.action_timelineFragment_to_restaurantProfile,
                    bundle
                )
            }
        )
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_timelineFragment_to_addEntryFragment)
        }

        val app = requireActivity().application as FoodLogApplication

        viewModel = ViewModelProvider(
            this,
            TimelineViewModelFactory(app.foodEntryRepository)
        )[TimelineViewModel::class.java]

        // --- Search Bar ---
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                adapter.submitList(entries)
                // Show empty state when there are no food entries yet
                emptyStateView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
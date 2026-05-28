package com.example.food_log.ui.timeline

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.food_log.FoodLogApplication
import com.example.food_log.R
import com.example.food_log.data.model.enums.DiningMode
import com.example.food_log.data.model.enums.Platform
import com.example.food_log.data.model.enums.WouldOrderAgain
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TimelineFragment : Fragment(R.layout.fragment_timeline) {

    private lateinit var viewModel: TimelineViewModel
    private lateinit var adapter: TimelineAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as FoodLogApplication

        viewModel = ViewModelProvider(
            this,
            TimelineViewModelFactory(app.foodEntryRepository)
        )[TimelineViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyStateView = view.findViewById<View>(R.id.emptyStateView)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        val btnFilter = view.findViewById<ImageButton>(R.id.btnFilter)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        adapter = TimelineAdapter(
            onEditClick = { entry ->
                val bundle = Bundle().apply { putLong("entryId", entry.foodEntry.id) }
                findNavController().navigate(
                    R.id.action_timelineFragment_to_editEntry,
                    bundle
                )
            },
            onRestaurantClick = { restaurantId ->
                val bundle = Bundle().apply { putLong("restaurantId", restaurantId) }
                findNavController().navigate(
                    R.id.action_timelineFragment_to_restaurantProfile,
                    bundle
                )
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
        })

        btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                adapter.submitList(entries)
                if (entries.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyStateView.visibility = View.GONE
                }
            }
        }

        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_timelineFragment_to_addEntryFragment)
        }
    }

    private fun showFilterBottomSheet() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        bottomSheet.setContentView(view)

        val spinnerMode = view.findViewById<Spinner>(R.id.spinnerFilterDiningMode)
        val spinnerPlatform = view.findViewById<Spinner>(R.id.spinnerFilterPlatform)
        val spinnerAgain = view.findViewById<Spinner>(R.id.spinnerFilterWouldOrderAgain)
        val spinnerMonth = view.findViewById<Spinner>(R.id.spinnerFilterMonth)
        val spinnerWeek = view.findViewById<Spinner>(R.id.spinnerFilterWeek)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilters)
        val btnClear = view.findViewById<Button>(R.id.btnClearFilters)

        val modeValues = listOf("Any") + DiningMode.entries.map { it.name.replace("_", " ") }
        spinnerMode.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, modeValues)

        val platformValues = listOf("Any") + Platform.entries.map { it.name.replace("_", " ") }
        spinnerPlatform.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, platformValues)

        val againValues = listOf("Any") + WouldOrderAgain.entries.map { it.name }
        spinnerAgain.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, againValues)

        val months = listOf("Any", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        spinnerMonth.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)

        val weeks = listOf("Any", "Week 1", "Week 2", "Week 3", "Week 4", "Week 5")
        spinnerWeek.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, weeks)

        viewModel.filterDiningMode.value?.let { mode ->
            spinnerMode.setSelection(DiningMode.entries.indexOf(mode) + 1)
        }
        viewModel.filterPlatform.value?.let { platform ->
            spinnerPlatform.setSelection(Platform.entries.indexOf(platform) + 1)
        }
        viewModel.filterWouldOrderAgain.value?.let { again ->
            spinnerAgain.setSelection(WouldOrderAgain.entries.indexOf(again) + 1)
        }
        spinnerMonth.setSelection(viewModel.filterMonth.value + 1)
        spinnerWeek.setSelection(viewModel.filterWeek.value + 1)

        btnClear.setOnClickListener {
            viewModel.clearFilters()
            bottomSheet.dismiss()
        }

        btnApply.setOnClickListener {
            val selectedMode = if (spinnerMode.selectedItemPosition > 0) DiningMode.entries[spinnerMode.selectedItemPosition - 1] else null
            val selectedPlatform = if (spinnerPlatform.selectedItemPosition > 0) Platform.entries[spinnerPlatform.selectedItemPosition - 1] else null
            val selectedAgain = if (spinnerAgain.selectedItemPosition > 0) WouldOrderAgain.entries[spinnerAgain.selectedItemPosition - 1] else null
            val selectedMonth = spinnerMonth.selectedItemPosition - 1
            val selectedWeek = spinnerWeek.selectedItemPosition - 1

            viewModel.applyFilters(selectedMode, selectedPlatform, selectedAgain, selectedMonth, selectedWeek)
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }
}
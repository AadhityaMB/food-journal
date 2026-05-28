package com.example.food_log.ui.restaurant

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.food_log.FoodLogApplication
import com.example.food_log.R
import com.example.food_log.ui.timeline.TimelineAdapter
import kotlinx.coroutines.launch

class RestaurantProfileFragment : Fragment(R.layout.fragment_restaurant_profile) {

    private lateinit var viewModel: RestaurantProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the restaurantId passed via Navigation Component arguments
        // This is safe because the nav_graph declares it as a required Long argument
        val restaurantId = arguments?.getLong("restaurantId")
            ?: throw IllegalArgumentException("RestaurantProfileFragment requires a restaurantId argument")

        val tvRestaurantName = view.findViewById<TextView>(R.id.tvRestaurantName)
        val tvVisitCount = view.findViewById<TextView>(R.id.tvVisitCount)
        val tvAvgRating = view.findViewById<TextView>(R.id.tvAvgRating)
        val tvTotalSpent = view.findViewById<TextView>(R.id.tvTotalSpent)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)

        val app = requireActivity().application as FoodLogApplication

        viewModel = ViewModelProvider(
            this,
            RestaurantProfileViewModelFactory(
                restaurantId,
                app.restaurantRepository,
                app.foodEntryRepository
            )
        )[RestaurantProfileViewModel::class.java]

        // Set up the RecyclerView — reuse TimelineAdapter for the visit history list
        val adapter = TimelineAdapter(
            onEditClick = { id ->
                val bundle = Bundle().apply { putLong("entryId", id) }
                findNavController().navigate(
                    R.id.action_restaurantProfileFragment_to_addEntryFragment,
                    bundle
                )
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // --- Observe stats ---

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.restaurant.collect { restaurant ->
                tvRestaurantName.text = restaurant.name
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.visitCount.collect { count ->
                tvVisitCount.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.averageRating.collect { avg ->
                tvAvgRating.text = avg?.let { "%.1f ★".format(it) } ?: "–"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalAmountSpent.collect { total ->
                tvTotalSpent.text = total?.let { "₹%.0f".format(it) } ?: "–"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                adapter.submitList(entries)
            }
        }
    }
}

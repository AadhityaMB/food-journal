package com.example.food_log.ui.entry

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.food_log.FoodLogApplication
import com.example.food_log.R
import com.example.food_log.data.model.enums.DiningMode
import com.example.food_log.data.model.enums.WouldOrderAgain
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEntryFragment : Fragment(R.layout.fragment_add_entry) {

    private lateinit var viewModel: AddEntryViewModel

    // Tracks the date the user has selected. Starts as today.
    private val selectedDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read the entryId argument passed from the nav graph.
        // -1L means "no entry" → add mode. Anything else → edit mode.
        val entryId = arguments?.getLong("entryId") ?: -1L

        // Get view references
        val tvTitle = view.findViewById<TextView>(R.id.tvFormTitle)
        val etRestaurant = view.findViewById<AutoCompleteTextView>(R.id.etRestaurant)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val etReview = view.findViewById<EditText>(R.id.etReview)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val btnPickDate = view.findViewById<Button>(R.id.btnPickDate)
        val spinnerDiningMode = view.findViewById<Spinner>(R.id.spinnerDiningMode)
        val radioGroupOrderAgain = view.findViewById<RadioGroup>(R.id.radioGroupOrderAgain)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // New Views for Dishes
        val llDishesContainer = view.findViewById<LinearLayout>(R.id.llDishesContainer)
        val btnAddDish = view.findViewById<Button>(R.id.btnAddDish)

        val app = requireActivity().application as FoodLogApplication

        viewModel = ViewModelProvider(
            this,
            AddEntryViewModelFactory(entryId, app.restaurantRepository, app.foodEntryRepository, app.orderedItemDao)
        )[AddEntryViewModel::class.java]

        // --- Helper function to add a new Dish row dynamically ---
        fun addDishRow(name: String = "", price: Double? = null) {
            val dishView = layoutInflater.inflate(R.layout.item_add_dish, llDishesContainer, false)
            val etDishName = dishView.findViewById<EditText>(R.id.etDishName)
            val etDishPrice = dishView.findViewById<EditText>(R.id.etDishPrice)
            val btnRemoveDish = dishView.findViewById<ImageButton>(R.id.btnRemoveDish)

            etDishName.setText(name)
            if (price != null) etDishPrice.setText(price.toString())

            btnRemoveDish.setOnClickListener {
                llDishesContainer.removeView(dishView)
            }

            llDishesContainer.addView(dishView)
        }

        // When user taps "Add Dish", append a blank row
        btnAddDish.setOnClickListener {
            addDishRow()
        }

        // --- Adjust UI for Add vs Edit mode ---
        if (viewModel.isEditMode) {
            tvTitle.text = "Edit Entry"
            btnDelete.visibility = View.VISIBLE  // show Delete button only in edit mode
        }

        // --- Restaurant Autocomplete ---
        val restaurantNames = mutableListOf<String>()
        val restaurantAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            restaurantNames
        )
        etRestaurant.setAdapter(restaurantAdapter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.restaurantSuggestions.collect { restaurants ->
                restaurantNames.clear()
                restaurantNames.addAll(restaurants.map { it.name })
                restaurantAdapter.notifyDataSetChanged()
            }
        }

        // --- Date Picker ---
        btnPickDate.text = dateFormat.format(selectedDate.time)

        btnPickDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    btnPickDate.text = dateFormat.format(selectedDate.time)
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // --- Dining Mode Spinner ---
        val diningModes = DiningMode.values()
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            diningModes.map { it.name.replace("_", " ") }
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDiningMode.adapter = spinnerAdapter

        // --- Pre-fill fields in Edit mode ---
        // existingEntry is null at first and becomes non-null once the ViewModel
        // loads the entry from the database. We only pre-fill once (when it first
        // arrives) to avoid overwriting any changes the user has already made.
        var hasPreFilled = false
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.existingEntry.collect { entry ->
                if (entry != null && !hasPreFilled) {
                    hasPreFilled = true

                    // Fill in each form field with the existing data
                    etRestaurant.setText(entry.restaurant?.name ?: "")
                    etReview.setText(entry.foodEntry.review ?: "")
                    etAmount.setText(entry.foodEntry.amountSpent?.let { "%.0f".format(it) } ?: "")
                    ratingBar.rating = entry.foodEntry.rating?.toFloat() ?: 0f

                    // Set the date picker to show the entry's original date
                    selectedDate.timeInMillis = entry.foodEntry.date
                    btnPickDate.text = dateFormat.format(selectedDate.time)

                    // Set the spinner to the correct dining mode
                    entry.foodEntry.diningMode?.let { mode ->
                        val position = diningModes.indexOf(mode)
                        if (position >= 0) spinnerDiningMode.setSelection(position)
                    }

                    // Set the correct "would order again" radio button
                    when (entry.foodEntry.wouldOrderAgain) {
                        WouldOrderAgain.YES -> radioGroupOrderAgain.check(R.id.radioYes)
                        WouldOrderAgain.MAYBE -> radioGroupOrderAgain.check(R.id.radioMaybe)
                        WouldOrderAgain.NO -> radioGroupOrderAgain.check(R.id.radioNo)
                        null -> radioGroupOrderAgain.clearCheck()
                    }

                    // Populate dishes
                    llDishesContainer.removeAllViews()
                    entry.orderedItems.forEach { item ->
                        addDishRow(name = item.name, price = item.price)
                    }
                }
            }
        }

        // --- Observe navigate-back signal from ViewModel ---
        // After save OR delete, the ViewModel emits Unit here and we pop back to Timeline.
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateBack.collect {
                findNavController().popBackStack()
            }
        }

        // --- Delete Button ---
        btnDelete.setOnClickListener {
            viewModel.deleteEntry()
            // Navigation happens automatically via the navigateBack flow above
        }

        // --- Save Button ---
        btnSave.setOnClickListener {
            val restaurantName = etRestaurant.text.toString().trim()

            if (restaurantName.isBlank()) {
                etRestaurant.error = "Please enter a restaurant name"
                return@setOnClickListener
            }

            val review = etReview.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val rating = ratingBar.rating.toInt().takeIf { it > 0 }
            val diningMode = diningModes[spinnerDiningMode.selectedItemPosition]
            val wouldOrderAgain = when (radioGroupOrderAgain.checkedRadioButtonId) {
                R.id.radioYes -> WouldOrderAgain.YES
                R.id.radioMaybe -> WouldOrderAgain.MAYBE
                R.id.radioNo -> WouldOrderAgain.NO
                else -> null
            }

            // Gather all dishes from the dynamic container
            val dishes = mutableListOf<Pair<String, Double?>>()
            for (i in 0 until llDishesContainer.childCount) {
                val dishView = llDishesContainer.getChildAt(i)
                val dishName = dishView.findViewById<EditText>(R.id.etDishName).text.toString().trim()
                val dishPrice = dishView.findViewById<EditText>(R.id.etDishPrice).text.toString().toDoubleOrNull()
                
                // Only add if they at least typed a name
                if (dishName.isNotBlank()) {
                    dishes.add(Pair(dishName, dishPrice))
                }
            }

            viewModel.saveEntry(
                restaurantName = restaurantName,
                review = review,
                amountSpent = amount,
                rating = rating,
                diningMode = diningMode,
                wouldOrderAgain = wouldOrderAgain,
                date = selectedDate.timeInMillis,
                dishes = dishes
            )
            // Navigation happens automatically via the navigateBack flow above
        }
    }
}
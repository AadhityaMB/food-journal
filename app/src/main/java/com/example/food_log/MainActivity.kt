package com.example.food_log

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.Restaurant
import com.example.food_log.data.model.enums.DiningMode
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val app = application as FoodLogApplication

        lifecycleScope.launch {

            app.foodEntryRepository.insert(
                FoodEntry(
                    restaurantId = 1,
                    date = System.currentTimeMillis(),
                    //mode = DiningMode.DINE_IN,
                    amountSpent = 299.0,
                    rating = 5,
                    review = "Chicken biryani was amazing",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
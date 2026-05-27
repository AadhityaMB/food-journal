package com.example.food_log.ui.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.food_log.R
import com.example.food_log.data.model.FoodEntry
import com.example.food_log.data.model.relations.EntryWithRestaurant

class TimelineAdapter(
    private var entries: List<EntryWithRestaurant>
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRestaurant: TextView = itemView.findViewById(R.id.tvRestaurant)
        val tvReview: TextView = itemView.findViewById(R.id.tvReview)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_entry, parent, false)

        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {

        val entry = entries[position]

        holder.tvRestaurant.text = entry.restaurant.name
        holder.tvReview.text = entry.foodEntry.review ?: "No Review"
        holder.tvAmount.text = "₹${entry.foodEntry.amountSpent ?: 0}"
    }

    override fun getItemCount(): Int = entries.size

    fun updateData(newEntries: List<EntryWithRestaurant>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
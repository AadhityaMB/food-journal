package com.example.food_log.ui.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.food_log.R
import com.example.food_log.data.model.relations.EntryWithRestaurant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Two separate click callbacks:
//   onEditClick      → called with the full EntryWithRestaurant when the card body is tapped
//   onRestaurantClick → called with restaurantId when the restaurant name text is tapped
class TimelineAdapter(
    private val onEditClick: (EntryWithRestaurant) -> Unit,
    private val onRestaurantClick: (Long) -> Unit = {}
) : ListAdapter<EntryWithRestaurant, TimelineAdapter.TimelineViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<EntryWithRestaurant>() {
            override fun areItemsTheSame(oldItem: EntryWithRestaurant, newItem: EntryWithRestaurant): Boolean {
                // The entries are the same if their unique IDs match
                return oldItem.foodEntry.id == newItem.foodEntry.id
            }

            override fun areContentsTheSame(oldItem: EntryWithRestaurant, newItem: EntryWithRestaurant): Boolean {
                // Check if the actual data inside has changed
                return oldItem == newItem
            }
        }
    }

    class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRestaurant: TextView = itemView.findViewById(R.id.tvRestaurant)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val tvDishesPreview: TextView = itemView.findViewById(R.id.tvDishesPreview)
        val tvReview: TextView = itemView.findViewById(R.id.tvReview)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_entry, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val entry = getItem(position)

        holder.tvRestaurant.text = entry.restaurant?.name ?: "Unknown Restaurant"
        holder.tvDate.text = dateFormat.format(Date(entry.foodEntry.date))
        holder.ratingBar.rating = entry.foodEntry.rating?.toFloat() ?: 0f
        
        // Format the dishes into a comma-separated string (e.g., "Dish A, Dish B")
        if (entry.orderedItems.isNotEmpty()) {
            holder.tvDishesPreview.visibility = View.VISIBLE
            holder.tvDishesPreview.text = entry.orderedItems.joinToString(", ") { it.name }
        } else {
            holder.tvDishesPreview.visibility = View.GONE
        }

        holder.tvReview.text = entry.foodEntry.review?.takeIf { it.isNotBlank() }
            ?: "No review written"
        holder.tvAmount.text = entry.foodEntry.amountSpent
            ?.let { "₹%.0f".format(it) } ?: "–"

        // Tapping the whole card → opens Edit Entry screen
        holder.itemView.setOnClickListener {
            onEditClick(entry)
        }

        // Tapping just the restaurant name → opens Restaurant Profile
        // This lets the user do two different things from the same card.
        holder.tvRestaurant.setOnClickListener {
            entry.restaurant?.id?.let { restaurantId ->
                onRestaurantClick(restaurantId)
            }
        }
    }

    // ListAdapter handles getItemCount and the underlying list automatically,
    // so we don't need getItemCount() or a custom updateData() method.
    // Instead of updateData(entries), the Fragment will call adapter.submitList(entries)
}
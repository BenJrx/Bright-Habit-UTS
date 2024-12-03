package com.example.brighthabit

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brighthabit.databinding.ItemDateBinding

class DateAdapter(private val weekDates: List<String>) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var selectedPosition: Int = -1 // To track selected position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = weekDates[position]
        val (day, dateNum) = date.split(" ")
        holder.bind(day, dateNum, position == selectedPosition)

        // Set click listener to update selection
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition) // Refresh old selection
            notifyItemChanged(selectedPosition) // Refresh new selection
        }
    }

    override fun getItemCount(): Int = weekDates.size

    fun setSelectedDate(date: String) {
        val index = weekDates.indexOfFirst { it.contains(date) }
        if (index != -1) {
            selectedPosition = index
            notifyItemChanged(selectedPosition)
        }
    }

    inner class DateViewHolder(private val binding: ItemDateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: String, date: String, isSelected: Boolean) {
            binding.tvDay.text = day
            binding.tvDate.text = date

            // Highlight if selected
            if (isSelected) {
                binding.root.setBackgroundResource(R.drawable.selected_date_background) // Green box
                binding.tvDay.setTextColor(Color.WHITE)
                binding.tvDate.setTextColor(Color.WHITE)
            } else {
                binding.root.setBackgroundResource(android.R.color.transparent)
                binding.tvDay.setTextColor(Color.BLACK)
                binding.tvDate.setTextColor(Color.BLACK)
            }
        }
    }
}

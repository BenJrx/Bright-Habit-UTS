package com.example.brighthabit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private val month: Int,
    private val year: Int,
    private val onDateClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val daysInMonth: List<String>

    init {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        daysInMonth = (1..totalDays).map {
            String.format("%02d/%02d/%d", it, month + 1, year)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_day_item, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = daysInMonth[position]
        holder.day.text = date.split("/")[0]

        holder.itemView.setOnClickListener {
            onDateClick(date)
        }
    }

    override fun getItemCount(): Int = daysInMonth.size

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val day: TextView = view.findViewById(R.id.tvDay)
    }
}

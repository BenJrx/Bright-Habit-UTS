package com.example.brighthabit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(private val habitList: MutableList<Habit>) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitIcon: ImageView = itemView.findViewById(R.id.habit_icon)
        val habitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val habitDate: TextView = itemView.findViewById(R.id.tvHabitDate)
        val checkDone: CheckBox = itemView.findViewById(R.id.checkDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habitList[position]

        holder.habitName.text = habit.namaHabit
        holder.habitDate.text = habit.date
        holder.checkDone.isChecked = habit.isDone
        
        holder.checkDone.visibility = if (habit.isDone) {
            holder.habitName.paintFlags = holder.habitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            View.GONE
        } else {
            holder.habitName.paintFlags = holder.habitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            View.VISIBLE
        }

        holder.checkDone.setOnCheckedChangeListener(null)
        holder.checkDone.setOnCheckedChangeListener { _, isChecked ->
            habit.isDone = isChecked

            holder.itemView.post {
                if (isChecked) {
                    holder.habitName.paintFlags = holder.habitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    holder.checkDone.visibility = View.GONE
                } else {
                    holder.habitName.paintFlags = holder.habitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    holder.checkDone.visibility = View.VISIBLE
                }

                notifyItemChanged(position)
            }
        }
    }


    override fun getItemCount(): Int {
        return habitList.size
    }

    fun updateHabitList(newHabitList: MutableList<Habit>) {
        habitList.clear()
        habitList.addAll(newHabitList)
        notifyDataSetChanged()
    }
}

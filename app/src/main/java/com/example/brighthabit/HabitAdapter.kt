package com.example.brighthabit

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(val habitList: MutableList<Habit>) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

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
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        holder.habitName.text = habit.namaHabit
        holder.habitDate.text = habit.date

        db.collection("habits").document(habit.id).get().addOnSuccessListener { document ->
            val storedDoneDates = (document.get("doneDates") as? List<String>)?.toMutableList() ?: mutableListOf()
            val isDoneToday = storedDoneDates.contains(currentDate)

            holder.checkDone.isChecked = isDoneToday
            updateTextDecoration(holder, isDoneToday)

            holder.checkDone.setOnClickListener {
                if (!isDoneToday) {
                    storedDoneDates.add(currentDate)

                    db.collection("habits").document(habit.id)
                        .update("doneDates", storedDoneDates)
                        .addOnSuccessListener {
                            holder.checkDone.isChecked = true
                            updateTextDecoration(holder, true)
                            Log.d("HabitUpdate", "doneDates updated successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("HabitUpdate", "Failed to update doneDates: ${e.message}")
                        }
                } else {
                    holder.checkDone.isChecked = true
                    Log.d("HabitUpdate", "Unchecking not allowed.")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("HabitFetch", "Failed to fetch habit: ${e.message}")
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, HabitDetailActivity::class.java)
            intent.putExtra("HABIT_ID", habit.id)
            context.startActivity(intent)
        }
    }

    private fun updateTextDecoration(holder: HabitViewHolder, isDone: Boolean) {
        if (isDone) {
            holder.habitName.paintFlags = holder.habitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.habitName.paintFlags = holder.habitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int = habitList.size

    fun updateHabitList(newHabitList: List<Habit>) {
        habitList.clear()
        habitList.addAll(newHabitList)
        notifyDataSetChanged()
    }
}

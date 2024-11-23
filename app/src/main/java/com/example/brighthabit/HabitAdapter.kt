package com.example.brighthabit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

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

        // Hapus listener sebelumnya untuk menghindari konflik
        holder.checkDone.setOnCheckedChangeListener(null)

        // Atur tampilan berdasarkan data Habit
        holder.habitName.text = habit.namaHabit
        holder.habitDate.text = habit.date
        holder.checkDone.isChecked = habit.isDone

        // Perbarui dekorasi teks (coretan)
        updateTextDecoration(holder, habit.isDone)

        // Tambahkan listener baru pada CheckBox
        holder.checkDone.setOnCheckedChangeListener { _, isChecked ->
            // Perbarui data objek Habit
            habit.isDone = isChecked
            updateTextDecoration(holder, isChecked)

            // Simpan perubahan ke Firestore
            db.collection("habits").document(habit.id)
                .update("isDone", isChecked)
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private fun updateTextDecoration(holder: HabitViewHolder, isDone: Boolean) {
        if (isDone) {
            holder.habitName.paintFlags =
                holder.habitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.habitName.paintFlags =
                holder.habitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount(): Int = habitList.size

    fun updateHabitList(newHabitList: List<Habit>) {
        habitList.clear()
        habitList.addAll(newHabitList)
        notifyDataSetChanged()
    }
}

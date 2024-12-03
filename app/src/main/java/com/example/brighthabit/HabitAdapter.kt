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

        // Atur tampilan berdasarkan data Habit
        holder.habitName.text = habit.namaHabit
        holder.habitDate.text = habit.date

        // Logika untuk memeriksa apakah habit selesai hari ini
        val isDoneToday = habit.doneDates.contains(currentDate)
        holder.checkDone.isChecked = isDoneToday
        updateTextDecoration(holder, isDoneToday)

        // Navigasi ke HabitDetailActivity saat item diklik
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, HabitDetailActivity::class.java)
            intent.putExtra("HABIT_ID", habit.id) // Pastikan key sesuai dengan yang digunakan di HabitDetailActivity
            context.startActivity(intent)
        }

        // Listener CheckBox untuk memperbarui status habit
        holder.checkDone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!habit.doneDates.contains(currentDate)) {
                    habit.doneDates.add(currentDate)
                }
            } else {
                habit.doneDates.remove(currentDate)
            }
            habit.isDone = isChecked
            updateTextDecoration(holder, isChecked)

            // Simpan status terbaru ke Firestore
            db.collection("habits").document(habit.id)
                .update("isDone", habit.isDone, "doneDates", habit.doneDates)
                .addOnFailureListener { e -> e.printStackTrace() }
        }

        // Memastikan habit status ter-reset jika belum selesai hari ini
        resetHabitStatusIfNeeded(habit, currentDate)
    }

    // Fungsi untuk memperbarui dekorasi teks (efek strikethrough)
    private fun updateTextDecoration(holder: HabitViewHolder, isDone: Boolean) {
        if (isDone) {
            holder.habitName.paintFlags = holder.habitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.habitName.paintFlags = holder.habitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    // Fungsi untuk mereset status habit jika tanggal hari ini belum tercatat
    private fun resetHabitStatusIfNeeded(habit: Habit, currentDate: String) {
        if (!habit.doneDates.contains(currentDate)) {
            habit.isDone = false // Reset status isDone ke false jika habit belum selesai hari ini
            db.collection("habits").document(habit.id)
                .update("isDone", habit.isDone)  // Update Firestore
                .addOnFailureListener { e -> e.printStackTrace() }
        }
    }

    override fun getItemCount(): Int = habitList.size

    // Fungsi untuk memperbarui list habit di adapter
    fun updateHabitList(newHabitList: List<Habit>) {
        habitList.clear()
        habitList.addAll(newHabitList)
        notifyDataSetChanged()
    }
}


package com.example.brighthabit

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.brighthabit.databinding.ItemReplyBinding
import java.text.SimpleDateFormat
import java.util.*

class ReplyAdapter : ListAdapter<Reply, ReplyAdapter.ReplyViewHolder>(ReplyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val binding = ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = getItem(position)
        holder.bind(reply)
    }

    class ReplyViewHolder(private val binding: ItemReplyBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: Reply) {
            Log.d("ReplyAdapter", "Binding reply: username=${reply.username}, photoUrl=${reply.photoUrl}")

            // Menampilkan username
            binding.username.text = reply.username

            // Menampilkan tanggal dengan format
            binding.textViewDate.text = formatTimestamp(reply.date)

            // Menampilkan pesan
            binding.textViewMessage.text = reply.text

            // Menampilkan foto profil dengan Glide
            if (reply.photoUrl.isNotEmpty()) {
                Glide.with(binding.profileImage.context)
                    .load(reply.photoUrl)
                    .circleCrop()
                    .into(binding.profileImage)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = Date(timestamp.toLong())
                sdf.format(date)
            } catch (e: Exception) {
                Log.e("ReplyAdapter", "Error parsing timestamp: $timestamp", e)
                timestamp // Kembalikan string asli jika parsing gagal
            }
        }
    }

    class ReplyDiffCallback : DiffUtil.ItemCallback<Reply>() {
        override fun areItemsTheSame(oldItem: Reply, newItem: Reply): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reply, newItem: Reply): Boolean {
            return oldItem == newItem
        }
    }
}

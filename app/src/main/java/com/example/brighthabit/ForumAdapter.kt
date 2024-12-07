package com.example.brighthabit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.brighthabit.databinding.ItemMessageBinding

class ForumAdapter(
    private val onReplyClick: (Message) -> Unit,
    private val onItemClick: (Message) -> Unit
) : ListAdapter<Message, ForumAdapter.ForumViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return ForumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class ForumViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.username.text = message.username
            binding.textViewDate.text = message.date
            binding.textViewMessageContent.text = message.text

            // Memuat gambar profil jika URL tersedia
            if (message.photoUrl.isNotEmpty()) {
                Glide.with(binding.profileImage.context)
                    .load(message.photoUrl)
                    .circleCrop()
                    .into(binding.profileImage)
            }

            binding.replyIcon.setOnClickListener {
                onReplyClick(message)
            }

            itemView.setOnClickListener {
                onItemClick(message)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}

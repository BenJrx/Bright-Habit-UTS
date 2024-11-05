package com.example.brighthabit

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.brighthabit.databinding.ItemMessageBinding

class ForumAdapter : ListAdapter<Message, ForumAdapter.MessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)

        holder.binding.profileImage.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MessageDetailActivity::class.java)
            intent.putExtra("username", message.username)
            intent.putExtra("date", message.date)
            intent.putExtra("message", message.text)
            context.startActivity(intent)
        }
    }

    class MessageViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.username.text = message.username
            binding.textViewDate.text = message.date
            binding.textViewMessageContent.text = message.text
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.username == newItem.username
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}

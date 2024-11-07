package com.example.alapon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private var userIdSelf: String) : ListAdapter<TextMessage, ChatViewHolder>(ChatDiffCallback()) {

    companion object {
        const val VIEW_TYPE_RIGHT = 1
        const val VIEW_TYPE_LEFT = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = if (viewType == VIEW_TYPE_RIGHT) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.send_message_item, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.receive_message_item, parent, false)
        }
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderID == userIdSelf) {
            VIEW_TYPE_RIGHT
        } else {
            VIEW_TYPE_LEFT
        }
    }
}

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textMessage: TextView = itemView.findViewById(R.id.textMessage)

    fun bind(textMessage: TextMessage) {
        this.textMessage.text = textMessage.textMessage
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<TextMessage>() {
    override fun areItemsTheSame(oldItem: TextMessage, newItem: TextMessage): Boolean {
        return oldItem.messageID == newItem.messageID
    }

    override fun areContentsTheSame(oldItem: TextMessage, newItem: TextMessage): Boolean {
        return oldItem == newItem
    }
}

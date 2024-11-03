package com.example.alapon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private var userIdSelf: String, private val chatList: MutableList<TextMessage>) :
    RecyclerView.Adapter<chatViewHolder>(){


    var right = 1
    var left = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): chatViewHolder {
        if (viewType == right) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.send_message_item, parent, false)
            return chatViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.receive_message_item, parent, false)
            return chatViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: chatViewHolder, position: Int) {
        holder.textMessage.text = chatList[position].textMessage
    }

    override fun getItemViewType(position: Int): Int {
        if (chatList[position].senderID == userIdSelf) {
            return right
        } else {
            return left
        }
    }

    override fun getItemCount(): Int = chatList.size

}


class chatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textMessage: TextView = itemView.findViewById(R.id.textMessage)
}
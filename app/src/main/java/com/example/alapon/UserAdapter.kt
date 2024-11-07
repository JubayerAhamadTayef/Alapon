package com.example.alapon

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alapon.databinding.UserItemDesignBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserAdapter(private val itemClick: ItemClick) : ListAdapter<User, UserViewHolder>(comparator) {

    private lateinit var context: Context

    interface ItemClick {
        fun onItemClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        context = parent.context
        return UserViewHolder(UserItemDesignBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)

        holder.binding.apply {
            userName.text = user.userName
            userEmail.text = user.userEmail
            userBio.text = user.userBio

            // Using coroutines for image loading
            CoroutineScope(Dispatchers.Main).launch {
                loadImage(user.userImage, holder)
            }

            holder.itemView.setOnClickListener {
                itemClick.onItemClick(user)
            }
        }
    }

    private suspend fun loadImage(imageUrl: String, holder: UserViewHolder) {
        withContext(Dispatchers.Main) {
            context.let {
                Glide.with(it)
                    .load(imageUrl)
                    .placeholder(R.drawable.image_place_holder)
                    .into(holder.binding.userImage)
            }
        }
    }

    companion object {
        val comparator = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class UserViewHolder(val binding: UserItemDesignBinding) : RecyclerView.ViewHolder(binding.root)
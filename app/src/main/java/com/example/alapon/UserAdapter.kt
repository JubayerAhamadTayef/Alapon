package com.example.alapon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alapon.databinding.UserItemDesignBinding

class UserAdapter(val itemClick: ItemClick): ListAdapter<User, UserViewHolder> (comparator) {

    interface ItemClick{

        fun onItemClick(user: User)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(UserItemDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position).let {

            holder.binding.apply {

                userName.text = it.userName
                userEmail.text = it.userEmail
                userBio.text = it.userBio

            }

            holder.itemView.setOnClickListener { _ ->

                itemClick.onItemClick(it)

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

class UserViewHolder(val binding: UserItemDesignBinding): RecyclerView.ViewHolder(binding.root)
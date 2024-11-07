package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.alapon.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var userDB: DatabaseReference
    private lateinit var chatDB: DatabaseReference
    private lateinit var userIdSelf: String
    private lateinit var userIdRemote: String

    // Initialize adapter
    private val adapter by lazy { ChatAdapter(userIdSelf) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        chatDB = FirebaseDatabase.getInstance().reference

        requireArguments().getString(USERID)?.let {
            userIdRemote = it
        }

        FirebaseAuth.getInstance().currentUser?.let {
            userIdSelf = it.uid
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerView.adapter = adapter

        getUserDetails()
        listenForMessages()

        binding.sendBtn.setOnClickListener {
            val textMessage = TextMessage(
                binding.messageEditText.text.toString(),
                "",
                userIdSelf,
                userIdRemote
            )
            sendMessage(textMessage)
        }

        return binding.root
    }

    companion object {
        private const val USERID = "id"
    }

    private fun getUserDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userSnapshot = withContext(Dispatchers.IO) {
                    chatDB.child(DBNODES.USER).child(userIdRemote).get().await()
                }

                if (!isAdded) return@launch

                userSnapshot.getValue(User::class.java)?.let {
                    binding.apply {
                        Glide.with(this@ChatFragment).load(it.userImage)
                            .placeholder(R.drawable.image_place_holder)
                            .into(userImage)
                        userName.text = it.userName
                        userEmail.text = it.userEmail
                    }
                } ?: run {
                    showToast("User data not available")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to load user details. Please try again.")
            }
        }
    }

    private fun listenForMessages() {
        chatDB.child(DBNODES.CHAT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val chatList = mutableListOf<TextMessage>()
                    snapshot.children.forEach { its ->
                        its.getValue(TextMessage::class.java)?.let {
                            if ((it.senderID == userIdSelf && it.receiverID == userIdRemote) ||
                                (it.senderID == userIdRemote && it.receiverID == userIdSelf)
                            ) {
                                chatList.add(it)
                            }
                        }
                    }
                    adapter.submitList(chatList)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("Failed to load messages. Please try again.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load messages. Please try again.")
            }
        })
    }

    private fun sendMessage(textMessage: TextMessage) {
        val messageId = chatDB.push().key ?: UUID.randomUUID().toString()
        textMessage.messageID = messageId

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    chatDB.child(DBNODES.CHAT).child(messageId).setValue(textMessage).await()
                }

                if (!isAdded) return@launch

                showToast("Message Sent Successfully!")
                binding.messageEditText.setText("")
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to send message. Please try again.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

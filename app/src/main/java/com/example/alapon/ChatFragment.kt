package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.alapon.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var userDB: DatabaseReference
    private lateinit var chatDB: DatabaseReference
    private lateinit var userIdSelf: String
    private lateinit var userIdRemote: String

    private val chatList = mutableListOf<TextMessage>()

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

        chatDB.child(DBNODES.USER).child(userIdRemote)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let {
                        binding.apply {
                            Glide.with(requireContext()).load(it.userImage)
                                .placeholder(R.drawable.image_place_holder)
                                .into(userImage)
                            userName.text = it.userName
                            userEmail.text = it.userEmail
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        messageToShow()

        return binding.root
    }

    private fun messageToShow() {
        chatDB.child(DBNODES.CHAT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                snapshot.children.forEach { its ->
                    its.getValue(TextMessage::class.java)?.let {
                        if (it.senderID == userIdSelf && it.receiverID == userIdRemote || it.senderID == userIdRemote && it.receiverID == userIdSelf) {
                            chatList.add(it)
                        }
                    }
                }

                val adapter = ChatAdapter(userIdSelf, chatList)

                val layoutManager = LinearLayoutManager(requireContext())
                layoutManager.stackFromEnd = true
                binding.recyclerView.layoutManager = layoutManager
                binding.recyclerView.adapter = adapter

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    companion object {
        private val USERID = "id"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendBtn.setOnClickListener {
            val textMessage =
                TextMessage(binding.messageEditText.text.toString(), "", userIdSelf, userIdRemote)
            sendMessage(textMessage)
        }

    }

    private fun sendMessage(textMessage: TextMessage) {
        val messageId = chatDB.push().key ?: UUID.randomUUID().toString()
        textMessage.messageID = messageId

        chatDB.child(DBNODES.CHAT).child(messageId).setValue(textMessage).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(requireContext(), "Message Sent Successfully!", Toast.LENGTH_SHORT)
                    .show()
                binding.messageEditText.setText("")
            } else {
                Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun getUserById(it: String) {

        userDB.child(DBNODES.USER).child(it).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let {
                        binding.apply {
                            userName.setText(it.userName)
                            userEmail.setText(it.userEmail)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )

    }

}
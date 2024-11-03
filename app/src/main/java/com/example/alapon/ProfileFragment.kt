package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.alapon.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var userDB: DatabaseReference

    private var userId = ""
    private var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        requireArguments().getString("id")?.let {

            userId = it
            getUserById(it)

        }

        FirebaseAuth.getInstance().currentUser?.let {
            if (it.uid == userId) {
                binding.letsChatBtn.text = EDIT
            } else {
                binding.letsChatBtn.text = CHAT
            }
        }

        binding.letsChatBtn.setOnClickListener {

            if (binding.letsChatBtn.text == EDIT) {
                bundle.putString(USERID, userId)
                findNavController().navigate(
                    R.id.action_profileFragment_to_profileEditFragment,
                    bundle
                )
            } else {
                bundle.putString(USERID, userId)
                findNavController().navigate(
                    R.id.action_profileFragment_to_chatFragment, bundle
                )
            }

        }

        return binding.root
    }

    companion object {
        private var EDIT = "Let's Edit"
        private var CHAT = "Let's Chat"
        private var USERID = "id"
    }

    private fun getUserById(it: String) {
        userDB.child(DBNODES.USER).child(it).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let {

                        binding.apply {

                            userName.text = it.userName
                            userEmail.text = it.userEmail
                            userBio.text = it.userBio
                            Glide.with(requireContext()).load(it.userImage)
                                .placeholder(R.drawable.image_place_holder).into(userImage)
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
        )
    }

}
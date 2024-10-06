package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.alapon.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var userDB: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        requireArguments().getString("id")?.let {

            getUserById(it)

        }

        return binding.root
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

                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
        )
    }

}
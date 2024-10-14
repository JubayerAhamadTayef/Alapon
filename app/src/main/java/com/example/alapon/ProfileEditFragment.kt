package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.alapon.databinding.FragmentProfileEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var userDB: DatabaseReference
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        requireArguments().getString("id")?.let {

            userId = it
            getUserById(it)

        }

        binding.saveBtn.setOnClickListener {

            var userMap: MutableMap<String, Any> = mutableMapOf()

            userMap["userName"] = binding.userName.text.toString().trim()
            userMap["userBio"] = binding.userBio.text.toString().trim()

            userDB.child(DBNODES.USER).child(userId).updateChildren(userMap).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Successfully Updated!", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack(R.id.profileFragment, false)
                } else {
                    Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

        return binding.root
    }

    private fun getUserById(it: String) {

        userDB.child(DBNODES.USER).child(it).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let {
                        binding.apply {
                            userName.setText(it.userName)
                            userEmail.setText(it.userEmail)
                            userBio.setText(it.userBio)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )

    }

}
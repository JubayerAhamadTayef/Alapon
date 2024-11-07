package com.example.alapon

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.alapon.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
            bundle.putString(USERID, userId)
            findNavController().navigate(
                if (binding.letsChatBtn.text == EDIT)
                    R.id.action_profileFragment_to_profileEditFragment
                else
                    R.id.action_profileFragment_to_chatFragment,
                bundle
            )
        }

        return binding.root
    }

    companion object {
        private const val EDIT = "Let's Edit"
        private const val CHAT = "Let's Chat"
        private const val USERID = "id"
    }

    @SuppressLint("SetTextI18n")
    private fun getUserById(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userSnapshot = withContext(Dispatchers.IO) {
                    userDB.child(DBNODES.USER).child(userId).get().await()
                }

                if (!isAdded) return@launch

                userSnapshot.getValue(User::class.java)?.let {
                    binding.apply {
                        userName.text = it.userName
                        userEmail.text = it.userEmail
                        userBio.text = it.userBio
                        context?.let { ctx ->
                            Glide.with(ctx).load(it.userImage)
                                .placeholder(R.drawable.image_place_holder)
                                .into(userImage)
                        }
                    }
                } ?: run {
                    // Handle case where user data is null
                    binding.userName.text = "Unknown User"
                }
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
                // Show error message or toast
                context?.let {
                    Toast.makeText(
                        it,
                        "Failed to load user data. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

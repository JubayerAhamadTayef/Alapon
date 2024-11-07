package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.alapon.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(), UserAdapter.ItemClick {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userDB: DatabaseReference
    private lateinit var adapter: UserAdapter

    private var currentUser: User? = null
    private val auth = FirebaseAuth.getInstance()
    private lateinit var firebaseUser: FirebaseUser
    private val bundle = Bundle()
    private val userList: MutableList<User> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        binding.logout.setOnClickListener {
            context?.let {
                AlertDialog.Builder(it, R.style.CustomAlertDialogTheme)
                    .setTitle("Logout")
                    .setMessage("Do you really want to Logout?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.dismiss()
                        auth.signOut()
                        findNavController().navigate(R.id.action_homeFragment_to_welcomeFragment)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

        auth.currentUser?.let {
            firebaseUser = it
        }

        binding.profileBtn.setOnClickListener {
            currentUser?.let {
                bundle.putString("id", it.userId)
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment, bundle)
            }
        }

        adapter = UserAdapter(this@HomeFragment)
        binding.recyclerView.adapter = adapter

        getAvailableUser()

        handleBackPress()

        return binding.root
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    context?.let {
                        AlertDialog.Builder(it, R.style.CustomAlertDialogTheme)
                            .setTitle("Exit")
                            .setMessage("Do you really want to exit?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                dialog.dismiss()
                                requireActivity().finish()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }
            })
    }

    private fun getAvailableUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    userDB.child(DBNODES.USER).get().await()
                }

                if (!isAdded) return@launch

                userList.clear()
                snapshot.children.forEach {
                    val user: User? = it.getValue(User::class.java)
                    user?.let {
                        if (firebaseUser.uid != user.userId) {
                            userList.add(user)
                        } else {
                            currentUser = user
                            setProfile()
                        }
                    }
                }
                adapter.submitList(userList)
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to load users. Please try again.")
            }
        }
    }

    private fun setProfile() {
        currentUser?.let {
            Glide.with(this).load(it.userImage).placeholder(R.drawable.image_place_holder)
                .into(binding.profileBtn)
        }
    }

    override fun onItemClick(user: User) {
        val bundle = Bundle()
        bundle.putString("id", user.userId)
        findNavController().navigate(R.id.action_homeFragment_to_profileFragment, bundle)
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}

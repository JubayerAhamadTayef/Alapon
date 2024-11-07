package com.example.alapon

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.alapon.databinding.FragmentProfileEditBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var userDB: DatabaseReference
    private var userId = ""

    private var userProfileUri: Uri? = null
    private lateinit var userStorage: StorageReference
    private var isProfileClicked = false
    private var imageLink: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        userDB = FirebaseDatabase.getInstance().reference
        userStorage = FirebaseStorage.getInstance().reference

        requireArguments().getString("id")?.let {
            userId = it
            getUserById(it)
        }

        binding.saveBtn.setOnClickListener {
            if (isProfileClicked && userProfileUri != null) {
                uploadImage(userProfileUri!!)
            } else {
                updateUserProfile()
            }
        }

        binding.userImage.setOnClickListener {
            isProfileClicked = true
            pickProfileImage()
        }

        return binding.root
    }

    private fun uploadImage(userProfileUri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val profileStorage = userStorage.child("Images").child(userId).child("Profile-Images")
                profileStorage.putFile(userProfileUri).await()
                val downloadUrl = profileStorage.downloadUrl.await()
                imageLink = downloadUrl.toString()
                profileUpdateWithImage(imageLink)
                showToast("Profile Picture Uploaded")
            } catch (e: Exception) {
                showError("Failed to upload profile picture: ${e.message}")
            }
        }
    }

    private fun profileUpdateWithImage(imageLink: String) {
        val userMap = mutableMapOf<String, Any>(
            "userName" to binding.userName.text.toString().trim(),
            "userBio" to binding.userBio.text.toString().trim(),
            "userImage" to imageLink
        )

        updateUserProfile(userMap)
    }

    private fun pickProfileImage() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private val startForProfileImageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.data?.let {
                    userProfileUri = it
                    binding.userImage.setImageURI(it)
                }
            }
            ImagePicker.RESULT_ERROR -> {
                showError(ImagePicker.getError(result.data))
            }
            else -> {
                showError("Task Cancelled")
            }
        }
    }

    private fun getUserById(userId: String) {
        userDB.child(DBNODES.USER).child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(User::class.java)?.let {
                    binding.apply {
                        userName.setText(it.userName)
                        userEmail.setText(it.userEmail)
                        userBio.setText(it.userBio)
                        context?.let { ctx ->
                            Glide.with(ctx).load(it.userImage)
                                .placeholder(R.drawable.image_place_holder).into(userImage)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Failed to retrieve user data: ${error.message}")
            }
        })
    }

    private fun updateUserProfile(userMap: Map<String, Any> = mapOf(
        "userName" to binding.userName.text.toString().trim(),
        "userBio" to binding.userBio.text.toString().trim()
    )) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userDB.child(DBNODES.USER).child(userId).updateChildren(userMap).await()
                }
                showToast("Successfully Updated!")
                findNavController().popBackStack(R.id.profileFragment, false)
            } catch (e: Exception) {
                showError("Failed to update profile: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showError(message: String) {
        if (isAdded) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

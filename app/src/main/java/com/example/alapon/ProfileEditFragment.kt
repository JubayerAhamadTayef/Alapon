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

class ProfileEditFragment : Fragment() {

    private lateinit var context: Context
    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var userDB: DatabaseReference
    private var userId = ""

    private lateinit var userProfileUri: Uri
    private lateinit var userStorage: StorageReference
    private var isProfileClicked = false
    private var imageLink: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isAdded) {
            context = requireContext()
        }
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        userStorage = FirebaseStorage.getInstance().reference

        requireArguments().getString("id")?.let {

            userId = it
            getUserById(it)

        }

        binding.saveBtn.setOnClickListener {

            if (isProfileClicked && userProfileUri != null) {
                uploadImage(userProfileUri)
            }

            var userMap: MutableMap<String, Any> = mutableMapOf()

            userMap["userName"] = binding.userName.text.toString().trim()
            userMap["userBio"] = binding.userBio.text.toString().trim()

            userDB.child(DBNODES.USER).child(userId).updateChildren(userMap).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Successfully Updated!", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack(R.id.profileFragment, false)
                } else {
                    Toast.makeText(context, "${it.exception?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

        binding.userImage.setOnClickListener {
            isProfileClicked = true
            pickProfileImage()
        }

        return binding.root
    }

    private fun uploadImage(userProfileUri: Uri) {
        var profileStorage: StorageReference =
            userStorage.child("Images").child(userId).child("Profile-Images")

        profileStorage.putFile(userProfileUri).addOnCompleteListener {
            if (it.isSuccessful) {
                profileStorage.downloadUrl.addOnSuccessListener { data ->
                    imageLink = data.toString()
                    profileUpdateWithImage(imageLink)
                    Toast.makeText(context, "Profile Picture Uploaded", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(context, "${it.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun profileUpdateWithImage(imageLink: String) {
        var userMap: MutableMap<String, Any> = mutableMapOf()
        userMap["userName"] = binding.userName.text.toString().trim()
        userMap["userBio"] = binding.userBio.text.toString().trim()
        userMap["userImage"] = imageLink

        userDB.child(DBNODES.USER).child(userId).updateChildren(userMap).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Successfully Updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "${it.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun pickProfileImage() {
        ImagePicker.with(this)
            .crop()                    //Crop image(Optional), Check Customization for more option
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080, 1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    data?.data.let {
                        if (it != null) {
                            userProfileUri = it
                        }
                        binding.userImage.setImageURI(it)
                    }
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
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
                            userBio.setText(it.userBio)
                            Glide.with(context).load(it.userImage)
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
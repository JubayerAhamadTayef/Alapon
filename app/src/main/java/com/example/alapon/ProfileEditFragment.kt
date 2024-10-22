package com.example.alapon

import android.app.Activity
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
import coil.load
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
                    Toast.makeText(requireContext(), "Successfully Updated!", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack(R.id.profileFragment, false)
                } else {
                    Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
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
        val profileStorage: StorageReference =
            userStorage.child("Images").child(userId).child("Profile Images")

        profileStorage.putFile(userProfileUri).addOnCompleteListener {
            if (it.isSuccessful) {
                profileStorage.downloadUrl.addOnSuccessListener { data ->
                    imageLink = data.toString()
                    profileUpdateWithImage(imageLink)
                    Toast.makeText(requireContext(), "Profile Picture Uploaded", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
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
                Toast.makeText(requireContext(), "Successfully Updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
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
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
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
                            if (it.userImage == "User Image" || it.userImage == "") {
                                userImage.setImageResource(R.drawable.baseline_person_24)
                            } else {
                                userImage.load(it.userImage)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )

    }

}
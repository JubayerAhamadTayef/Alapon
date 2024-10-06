package com.example.alapon

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.alapon.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding

    private lateinit var userDB: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        binding.apply {

            signUpButton.setOnClickListener {

                if (userEditText.text?.isNotEmpty() == true && emailEditText.text?.isNotEmpty() == true && passwordEditText.text?.isNotEmpty() == true) {

                    val userName = userEditText.text.toString().trim()
                    val email = emailEditText.text.toString().trim()
                    val password = passwordEditText.text.toString().trim()

                    if (isEmailValid(email) && isPasswordValid(password)) {

                        signUpUser(userName, email, password)

                    } else {

                        Toast.makeText(
                            requireContext(),
                            "Invalid Email or Password!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }


                } else {

                    Toast.makeText(
                        requireContext(),
                        "Please, provide needed Information, then click again Sign Up Button.",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

            alreadyHaveAccount.setOnClickListener {

                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)

            }

        }

        return binding.root
    }

    private fun isEmailValid(email: String): Boolean {

        return Patterns.EMAIL_ADDRESS.matcher(email).matches()

    }

    private fun isPasswordValid(password: String): Boolean {

        val passRegex = Regex("^(?=.*[A-Za-z])(?=.*[@\$!%*#?&])[A-Za-z@\$!%*#?&\\d]{6,}\$")
        return password.matches(passRegex)

    }

    private fun signUpUser(userName: String, email: String, password: String) {

        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {

                saveUserToDatabase(auth.currentUser?.uid, email, userName)

            } else {

                Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
                    .show()

            }

        }

    }

    private fun saveUserToDatabase(uid: String?, email: String, userName: String) {

        uid?.let {

            val user = User(userId = uid, userName = userName, userEmail = email)
            userDB.child(DBNODES.USER).child(it).setValue(user).addOnCompleteListener {

                if (it.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        "Sign Up Successfully, $email",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)

                } else {

                    Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
                        .show()

                }

            }

        }

    }
}
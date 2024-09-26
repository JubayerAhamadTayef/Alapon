package com.example.alapon

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.alapon.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.apply {

            createNewAccount.setOnClickListener {

                findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)

            }

            loginButton.setOnClickListener {

                if (emailEditText.text?.isNotEmpty() == true && passwordEditText.text?.isNotEmpty() == true) {

                    val email = emailEditText.text.toString().trim()
                    val password = passwordEditText.text.toString().trim()

                    if (isEmailValid(email) && isPasswordValid(password)) {

                        loginUser(email, password)

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
                        "Please, provide needed Information. Then, click again Login Button.",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }

        return binding.root
    }

    private fun isEmailValid(email: String): Boolean {

        return Patterns.EMAIL_ADDRESS.matcher(email).matches()

    }

    private fun isPasswordValid(password: String): Boolean {

        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"

        val pattern = Pattern.compile(passwordPattern)
        val matcher = pattern.matcher(password)

        return matcher.matches()

    }

    private fun loginUser(email: String, password: String) {

        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {

                val user = auth.currentUser

                Toast.makeText(
                    requireContext(),
                    "Login Successfully, ${user?.email}",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

            } else {

                Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
                    .show()

            }

        }

    }

}
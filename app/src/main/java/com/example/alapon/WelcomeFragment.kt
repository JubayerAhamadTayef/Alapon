package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.alapon.databinding.FragmentWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            checkCurrentUser()
        }

        binding.apply {
            loginButton.setOnClickListener {
                findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
            }
            signUpButton.setOnClickListener {
                findNavController().navigate(R.id.action_welcomeFragment_to_signUpFragment)
            }
        }

        return binding.root
    }

    private suspend fun checkCurrentUser() {
        withContext(Dispatchers.IO) {
            try {
                FirebaseAuth.getInstance().currentUser?.let {
                    firebaseUser = it
                    if (firebaseUser!!.isEmailVerified) {
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showVerificationDialog(firebaseUser!!)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to check current user: ${e.message}")
            }
        }
    }

    private fun showVerificationDialog(user: FirebaseUser) {
        context?.let {
            AlertDialog.Builder(it, R.style.CustomAlertDialogTheme)
                .setTitle("Email Verification Required")
                .setMessage("Your email is not verified. Please check your inbox to verify your email.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton("Resend Email") { dialog, _ ->
                    lifecycleScope.launch {
                        sendVerificationEmail(user)
                    }
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private suspend fun sendVerificationEmail(user: FirebaseUser) {
        try {
            withContext(Dispatchers.IO) {
                user.sendEmailVerification().await()
            }
            showToast("Verification email sent to ${user.email}. Please verify your email.")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Failed to send verification email: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

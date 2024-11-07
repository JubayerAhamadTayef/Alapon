package com.example.alapon

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.alapon.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        emailFocusChangeListener()
        passwordFocusChangeListener()

        binding.apply {
            createNewAccount.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
            }

            loginButton.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    if (isEmailValid() == null && isPasswordValid() == null) {
                        loginUser(email, password)
                    } else {
                        showError("Invalid Email or Password!")
                    }
                } else {
                    binding.emailEditTextLayout.helperText = isEmailValid()
                    binding.emailEditTextLayout.error = isEmailValid()
                    binding.passwordEditTextLayout.helperText = isPasswordValid()
                    showError("Please, provide needed Information. Then, click again Login Button.")
                }
            }

            forgotPassword.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                if (isEmailValid() == null) {
                    binding.emailEditTextLayout.helperText = isEmailValid()
                    binding.emailEditTextLayout.error = isEmailValid()
                    resetPassword(email)
                } else {
                    binding.emailEditTextLayout.helperText = isEmailValid()
                    binding.emailEditTextLayout.error = isEmailValid()
                    showError("Please enter a valid email address to reset your password.")
                }
            }
        }

        return binding.root
    }

    private fun passwordFocusChangeListener() {
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.passwordEditTextLayout.helperText = isPasswordValid()
            }
            passwordTextChangeListener()
        }
    }

    private fun passwordTextChangeListener() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.passwordEditTextLayout.helperText = isPasswordValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isPasswordValid(): CharSequence? {
        val password = binding.passwordEditText.text.toString().trim()
        return when {
            password.isBlank() -> "Password is required."
            password.length < 6 -> "Password must be at least 6 characters long."
            !password.matches(".*[A-Z].*".toRegex()) -> "Password must include at least one uppercase letter."
            !password.matches(".*[a-z].*".toRegex()) -> "Password must include at least one lowercase letter."
            !password.matches(".*[0-9].*".toRegex()) -> "Password must include at least one number."
            !password.matches(".*[@#\$%^&*+=].*".toRegex()) -> "Password must include at least one special character (e.g., @#\$%^&*)."
            else -> null
        }
    }

    private fun emailFocusChangeListener() {
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.emailEditTextLayout.helperText = isEmailValid()
                binding.emailEditTextLayout.error = isEmailValid()
            }
            emailTextChangeListener()
        }
    }

    private fun emailTextChangeListener() {
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailEditTextLayout.helperText = isEmailValid()
                binding.emailEditTextLayout.error = isEmailValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isEmailValid(): String? {
        val email = binding.emailEditText.text.toString().trim()
        return when {
            email.isBlank() -> "Email address is required."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() || !Regex(".*\\.[a-z]{3,}$").matches(
                email
            ) -> "Please, enter a valid email address."

            else -> null
        }
    }

    private fun loginUser(email: String, password: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val authResult = withContext(Dispatchers.IO) {
                    auth.signInWithEmailAndPassword(email, password).await()
                }
                val user = authResult.user
                user?.let {
                    if (it.isEmailVerified) {
                        showToast("Login Successfully, ${it.email}")
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        showVerificationDialog(it)
                    }
                }
            } catch (e: Exception) {
                showError(e.message ?: "Login failed. Please try again.")
            }
        }
    }

    private fun showVerificationDialog(user: FirebaseUser) {
        context?.let {
            AlertDialog.Builder(it, R.style.CustomAlertDialogTheme)
                .setTitle("Email Verification Required")
                .setMessage("We have sent a verification email to your email address. Please check your inbox and verify your email before logging in.")
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
            showError("Failed to send verification email: ${e.message}")
        }
    }

    private fun resetPassword(email: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    auth.sendPasswordResetEmail(email).await()
                }
                showToast("Password reset email sent to $email")
            } catch (e: Exception) {
                showError(e.message ?: "Failed to send reset email. Please try again.")
            }
        }
    }

    private fun showError(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

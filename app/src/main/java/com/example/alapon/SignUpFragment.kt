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
import com.example.alapon.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var userDB: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        userDB = FirebaseDatabase.getInstance().reference

        userNameFocusChangeListener()
        userEmailFocusChangeListener()
        userPasswordFocusChangeListener()

        binding.apply {
            signUpButton.setOnClickListener {
                validateForm()
            }

            alreadyHaveAccount.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            }
        }

        return binding.root
    }

    private fun validateForm() {
        val isUserNameValid = isUserNameValid()
        val isEmailValid = isEmailValid()
        val isPasswordStrong = isPasswordStrong()

        if (isUserNameValid == null && isEmailValid == null && isPasswordStrong == null) {
            signUpUser(
                binding.userNameEditText.text.toString().trim(),
                binding.emailEditText.text.toString().trim(),
                binding.passwordEditText.text.toString().trim()
            )
        } else {
            showFormErrors(isUserNameValid, isEmailValid, isPasswordStrong)
        }
    }

    private fun showFormErrors(
        userNameError: CharSequence?,
        emailError: CharSequence?,
        passwordError: CharSequence?
    ) {
        binding.apply {
            userNameEditTextLayout.helperText = userNameError
            userNameEditTextLayout.error = userNameError
            emailEditTextLayout.helperText = emailError
            emailEditTextLayout.error = emailError
            passwordEditTextLayout.helperText = passwordError
            Toast.makeText(
                requireContext(),
                "Please provide valid information and try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun userPasswordFocusChangeListener() {
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.passwordEditTextLayout.helperText = isPasswordStrong()
            }
            userPasswordTextChangedListener()
        }
    }

    private fun userPasswordTextChangedListener() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.passwordEditTextLayout.helperText = isPasswordStrong()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isPasswordStrong(): String? {
        val password = binding.passwordEditText.text.toString().trim()
        return when {
            password.isBlank() -> "Password is required."
            password.length < 6 -> "Password must be at least 6 characters long."
            !password.matches(".*[A-Z].*".toRegex()) -> "Password must include at least one uppercase letter."
            !password.matches(".*[a-z].*".toRegex()) -> "Password must include at least one lowercase letter."
            !password.matches(".*[0-9].*".toRegex()) -> "Password must include at least one number."
            !password.matches(".*[@#\$%^&+=].*".toRegex()) -> "Password must include at least one special character (e.g., @#\$%^&*)."
            else -> null
        }
    }

    private fun userEmailFocusChangeListener() {
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.emailEditTextLayout.helperText = isEmailValid()
                binding.emailEditTextLayout.error = isEmailValid()
            }
            userEmailTextChangeListener()
        }
    }

    private fun userEmailTextChangeListener() {
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailEditTextLayout.helperText = isEmailValid()
                binding.emailEditTextLayout.error = isEmailValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isEmailValid(): CharSequence? {
        val email = binding.emailEditText.text.toString().trim()
        return when {
            email.isBlank() -> "Email address is required."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() || !Regex(".*\\.[a-z]{3,}$").matches(
                email
            ) -> "Please enter a valid email address."

            else -> null
        }
    }

    private fun userNameFocusChangeListener() {
        binding.userNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.userNameEditTextLayout.helperText = isUserNameValid()
                binding.userNameEditTextLayout.error = isUserNameValid()
            }
            userNameTextChangeListener()
        }
    }

    private fun userNameTextChangeListener() {
        binding.userNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.userNameEditTextLayout.helperText = isUserNameValid()
                binding.userNameEditTextLayout.error = isUserNameValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isUserNameValid(): CharSequence? {
        val userName = binding.userNameEditText.text.toString().trim()
        return when {
            userName.isBlank() -> "User name is required."
            userName.matches(".*[a-z].*".toRegex()) || userName.matches(".*[A-Z].*".toRegex()) -> null
            else -> "User name must contain at least one uppercase or lowercase letter."
        }
    }

    private fun signUpUser(userName: String, email: String, password: String) {
        val auth = FirebaseAuth.getInstance()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val authResult = withContext(Dispatchers.IO) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                }

                auth.currentUser?.let {
                    sendVerificationEmail(it)
                    saveUserToDatabase(it.uid, email, userName)
                } ?: run {
                    showError("Failed to retrieve user data after registration.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Sign up failed: ${e.message}")
            }
        }
    }

    private suspend fun sendVerificationEmail(user: FirebaseUser) {
        try {
            withContext(Dispatchers.IO) {
                user.sendEmailVerification().await()
            }
            showVerificationDialog(user)
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Failed to send verification email: ${e.message}")
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

    private fun saveUserToDatabase(uid: String, email: String, userName: String) {
        val user = User(userId = uid, userName = userName, userEmail = email)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userDB.child(DBNODES.USER).child(uid).setValue(user).await()
                }
                showToast("Sign Up Successfully, $email")
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Failed to save user data: ${e.message}")
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


package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.alapon.databinding.FragmentWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        FirebaseAuth.getInstance().currentUser?.let {

            firebaseUser = it
            findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)

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

}
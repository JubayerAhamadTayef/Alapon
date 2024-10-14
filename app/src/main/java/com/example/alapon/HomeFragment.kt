package com.example.alapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.alapon.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(), UserAdapter.ItemClick {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userDB: DatabaseReference
    private lateinit var adapter: UserAdapter

    private var currentUser: User? = null
    private var auth = FirebaseAuth.getInstance()
    lateinit var firebaseUser: FirebaseUser
    private val bundle = Bundle()

    private val userList: MutableList<User> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        binding.logout.setOnClickListener {

            auth = FirebaseAuth.getInstance()

            auth.signOut().apply {

                findNavController().navigate(R.id.action_homeFragment_to_welcomeFragment)

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

        return binding.root
    }

    private fun getAvailableUser() {

        userDB.child(DBNODES.USER).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                snapshot.children.forEach {

                    val user: User = it.getValue(User::class.java)!!

                    if (firebaseUser.uid != user.userId){
                    userList.add(user)
                    } else {
                        currentUser = user
                        setProfile()
                    }
                }
                adapter.submitList(userList)
            }

            override fun onCancelled(error: DatabaseError) {

            }


        })

    }

    private fun setProfile() {
        currentUser?.let {
            binding.profileBtn.load("https://media.licdn.com/dms/image/v2/D5603AQHS0TTHVz36vg/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1725544473563?e=1733356800&v=beta&t=M6aiG2uqLh2jvq-Fav0pQ8HOWm5qy5gZ5Ay8TxG6an8")
        }
    }

    override fun onItemClick(user: User) {
        val bundle = Bundle()
        bundle.putString("id", user.userId)
        findNavController().navigate(R.id.action_homeFragment_to_profileFragment, bundle)
    }

}
package com.yucox.splitwise.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.R.R.model.SendFriendRequest
import com.R.R.model.UserInfo
import com.yucox.splitwise.adapter.FriendAdapter
import com.yucox.splitwise.databinding.FriendsActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FriendsActivity : AppCompatActivity() {
    private lateinit var adapter: FriendAdapter
    private val userInfos = ArrayList<UserInfo>()
    private var mailOfFriends = HashSet<String>()
    private val ref = Firebase.database.getReference("FriendRequest")
    private val refUsersData = Firebase.database.getReference("UsersData")
    private lateinit var binding: FriendsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FriendsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.friendStatusTv.visibility = View.GONE
        binding.swipeRefreshFr.isRefreshing = false

        getMailOfFriends()

        refreshData()

        goToPreviousActivity()

    }

    private fun goToPreviousActivity() {
        binding.backToMainActivity.setOnClickListener {
            finish()
        }
    }

    private fun refreshData() {
        binding.swipeRefreshFr.setOnRefreshListener {
            updateMailOfFriends()
        }
    }

    private fun updateMailOfFriends() {
        val newMailOfFriends = HashSet<String>()
        newMailOfFriends.clear()

        fun updateAfterGetMails() {
            refUsersData.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            var temp = snap.getValue(UserInfo::class.java)
                            if (temp?.mail in newMailOfFriends) {
                                if (temp in userInfos) {
                                    continue
                                } else {
                                    userInfos.add(temp!!)
                                }
                            }
                        }
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        adapter.notifyDataSetChanged()
                        binding.swipeRefreshFr.isRefreshing = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            var counter = 0
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(SendFriendRequest::class.java)
                        if (Firebase.auth.currentUser?.email == temp?.whoSentFriendRequest) {
                            if (temp?.status == 1) {
                                newMailOfFriends.add(temp?.whoGetFriendRequest.toString())
                                counter++
                            }
                        } else if (Firebase.auth.currentUser?.email == temp?.whoGetFriendRequest) {
                            if (temp?.status == 1) {
                                newMailOfFriends.add(temp?.whoSentFriendRequest.toString())
                                counter++
                            }
                        }
                    }
                }
                if (counter == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.friendStatusTv.visibility = View.VISIBLE
                        binding.friendStatusTv.text = "Ekli hiçbir arkadaşınız yok."
                    }
                }
                if (newMailOfFriends.size != mailOfFriends.size) {
                    userInfos.clear()
                    mailOfFriends.clear()
                    mailOfFriends = newMailOfFriends
                    updateAfterGetMails()
                } else {
                    binding.swipeRefreshFr.isRefreshing = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initAdapter() {
        adapter = FriendAdapter(this@FriendsActivity, userInfos)
        binding.recyclerFriend.layoutManager =
            LinearLayoutManager(this@FriendsActivity, RecyclerView.VERTICAL, false)
        binding.recyclerFriend.adapter = adapter
    }

    private fun getInfoFromMail() {
        refUsersData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(UserInfo::class.java)
                        if (temp?.mail in mailOfFriends) {
                            if (temp in userInfos) {
                                continue
                            } else {
                                userInfos.add(temp!!)
                            }
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    initAdapter()
                    binding.swipeRefreshFr.isRefreshing = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getMailOfFriends() {
        binding.swipeRefreshFr.isRefreshing = true
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            var counter = 0
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(SendFriendRequest::class.java)
                        if (Firebase.auth.currentUser?.email == temp?.whoSentFriendRequest) {
                            if (temp?.status == 1) {
                                mailOfFriends.add(temp?.whoGetFriendRequest.toString())
                                counter++
                            }
                        } else if (Firebase.auth.currentUser?.email == temp?.whoGetFriendRequest) {
                            if (temp?.status == 1) {
                                mailOfFriends.add(temp?.whoSentFriendRequest.toString())
                                counter++
                            }
                        }
                    }
                }
                if (counter == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.friendStatusTv.visibility = View.VISIBLE
                        binding.friendStatusTv.text = "Ekli hiçbir arkadaşınız yok."
                    }
                }
                getInfoFromMail()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onRestart() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            binding.swipeRefreshFr.isRefreshing = true
            updateMailOfFriends()
        }
        super.onRestart()
    }

    override fun onBackPressed() {
        finish()
    }
}
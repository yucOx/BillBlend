package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Friend
import com.R.R.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class FriendsViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private var mailList = hashSetOf<String>()
    private val friendsList = ArrayList<User>()

    fun areYouAlone(): Boolean {
        return mailList.size < 1
    }

    fun getMyFriends(): ArrayList<User> {
        return friendsList
    }

    fun fetchMails(): Task<Boolean> {
        val refFriends = database.getReference("FriendRequest")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refFriends.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(Friend::class.java)
                        val sentRequest = temp?.whoSentFriendRequest
                        val recentRequest = temp?.whoGetFriendRequest
                        if (Firebase.auth.currentUser?.email == sentRequest) {
                            if (temp?.status == 1) {
                                mailList.add(recentRequest.toString())
                            }
                        } else if (Firebase.auth.currentUser?.email == recentRequest) {
                            if (temp?.status == 1) {
                                mailList.add(sentRequest.toString())
                            }
                        }
                    }
                }
                taskCompletionSource.setResult(true)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun fetchFriends(): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refUsersData = database.getReference("UsersData")

        refUsersData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(User::class.java)
                        if (temp?.mail in mailList) {
                            if (temp in friendsList) {
                                continue
                            }
                            friendsList.add(temp!!)
                        }
                    }
                }
                taskCompletionSource.setResult(true)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun newFriendCheck(): Task<Boolean> {
        val refFriends = database.getReference("FriendRequest")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refFriends.addListenerForSingleValueEvent(object : ValueEventListener {
            val tempFriendList = hashSetOf<String>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(Friend::class.java)
                        val sentRequest = temp?.whoSentFriendRequest
                        val recentRequest = temp?.whoGetFriendRequest
                        if (Firebase.auth.currentUser?.email == sentRequest) {
                            if (temp?.status == 1) {
                                tempFriendList.add(recentRequest.toString())
                            }
                        } else if (Firebase.auth.currentUser?.email == recentRequest) {
                            if (temp?.status == 1) {
                                tempFriendList.add(sentRequest.toString())
                            }
                        }
                    }
                }
                if (tempFriendList.size != mailList.size) {
                    mailList.clear()
                    friendsList.clear()
                    mailList = tempFriendList
                    taskCompletionSource.setResult(true)
                } else {
                    taskCompletionSource.setResult(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }
}
package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Friend
import com.R.R.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendRequestViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersMail = ArrayList<String>()
    private val friendInfo = ArrayList<User>()
    private var counter = 0

    fun fetchFriendRequest(): Task<Boolean> {
        val friendRequestRef = database.getReference("FriendRequest")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    usersMail.clear()
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(Friend::class.java)
                        if (auth.currentUser?.email == temp?.whoGetFriendRequest) {
                            if (temp?.status == 0) {
                                counter++
                                usersMail.add(temp.whoSentFriendRequest.toString())
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

    fun fetchUsersDetail(): Task<Boolean> {
        val usersDataRef = database.getReference("UsersData")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        usersDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val getTempValue = snap.getValue(User::class.java)
                        for (userMail in usersMail) {
                            if (getTempValue?.mail != userMail)
                                continue

                            friendInfo.add(
                                User(
                                    getTempValue.name,
                                    getTempValue.surname,
                                    getTempValue.mail,
                                    getTempValue.pfpUri
                                )
                            )
                        }
                    }
                    taskCompletionSource.setResult(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun getFriendRequestCount(): Int {
        return counter
    }

    fun getRequestInfo(): ArrayList<User> {
        return friendInfo
    }

    fun resetValues() {
        usersMail.clear()
        friendInfo.clear()
    }
}
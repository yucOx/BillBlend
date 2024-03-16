package com.R.R.model

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class Friend(
    var whoSentFriendRequest: String? = "",
    var whoGetFriendRequest: String? = "",
    var status: Int? = null
)

class FriendRepository {
    fun newFriendCheck(
        database: FirebaseDatabase,
        mailList: ArrayList<String>,
        friendsList: ArrayList<User>
    ): Task<Boolean> {
        val refFriends = database.getReference("FriendRequest")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refFriends.addListenerForSingleValueEvent(object : ValueEventListener {
            val tempFriendList = ArrayList<String>()
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
                    for (temp in tempFriendList) {
                        mailList.add(temp)
                    }
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

    fun fetchRequestMail(
        database: FirebaseDatabase,
        usersMail: ArrayList<String>,
    ): Task<Boolean> {
        val friendRequestRef = database.getReference("FriendRequest")
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val mainUserMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    usersMail.clear()
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(Friend::class.java)
                        if (mainUserMail == temp?.whoGetFriendRequest) {
                            if (temp?.status == 0) {
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

    fun fetchFriendsMail(
        mailsOfFriend: ArrayList<String>,
        database: FirebaseDatabase,
        mainMail: String
    ): Task<Boolean> {
        val ref = database.getReference("FriendRequest")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                    return

                for (snap in snapshot.children) {
                    val tempControl = snap.getValue(Friend::class.java)
                    if (tempControl?.whoGetFriendRequest == mainMail) {
                        if (tempControl.whoSentFriendRequest !in mailsOfFriend) {
                            if (tempControl.status == 1) {
                                mailsOfFriend.add(tempControl.whoSentFriendRequest.toString())
                            }
                        }
                    } else if (tempControl?.whoSentFriendRequest == mainMail) {
                        if (tempControl.whoGetFriendRequest !in mailsOfFriend) {
                            if (tempControl.status == 1) {
                                mailsOfFriend.add(tempControl.whoGetFriendRequest.toString())
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
}
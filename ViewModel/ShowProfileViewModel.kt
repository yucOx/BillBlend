package com.yucox.splitwise.ViewModel

import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.R.R.model.Friend
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowProfileViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val mainUserMail = auth.currentUser?.email.toString()
    private val friendRequestRef = database.getReference("FriendRequest")
    private var _status = 0

    fun checkFriendStatus(mail: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        if (mail == mainUserMail) {
            _status = 2
            taskCompletionSource.setResult(true)
            return taskCompletionSource.task
        }
        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var checker = 0
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(Friend::class.java)
                        val receiver = temp?.whoGetFriendRequest
                        val sender = temp?.whoSentFriendRequest
                        val status = temp?.status

                        if (!((sender == mail && mainUserMail == receiver)
                                    || (receiver == mail && mainUserMail == sender))
                        )
                            continue

                        if (status == 1) {
                            _status = 1
                            checker = 1
                        }

                        if(status == 0)
                            checker = 1
                    }
                }
                if (checker == 0) {
                    if (mail != mainUserMail) {
                        _status = -1
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

    fun unFriend(mail: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(Friend::class.java)
                        val receiver = temp?.whoGetFriendRequest
                        val sender = temp?.whoSentFriendRequest

                        if (!((receiver == mail && sender == mainUserMail)
                                    || (receiver == mainUserMail && sender == mail))
                        )
                            continue

                        val key = snap.key.toString()
                        friendRequestRef.child(key)
                            .removeValue()

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

    fun sendRequest(mail: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        if (_status == -1) {
            val friend = Friend()
            friend.whoSentFriendRequest = mainUserMail
            friend.whoGetFriendRequest = mail
            friend.status = 0

            friendRequestRef.push().setValue(friend)
                .addOnSuccessListener {
                    taskCompletionSource.setResult(true)
                }
                .addOnFailureListener {
                    taskCompletionSource.setResult(false)
                }
        }
        return taskCompletionSource.task
    }

    fun setStatus(status: Int) {
        _status = status
    }

    fun getStatus(): Int {
        return _status
    }
}
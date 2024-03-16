package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Friend
import com.R.R.model.FriendRepository
import com.R.R.model.User
import com.R.R.model.UserDataRepository
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
    private var _mailList = ArrayList<String>()
    private val _friendsList = ArrayList<User>()
    
    private val friendRepository : FriendRepository = FriendRepository()
    private val userDataRepository : UserDataRepository = UserDataRepository()

    fun areYouAlone(): Boolean {
        return _mailList.size < 1
    }

    fun getMyFriends(): ArrayList<User> {
        return userDataRepository.getUsersInfo()
    }

    fun fetchMails(): Task<Boolean> {
        return friendRepository.fetchFriendsMail(
            _mailList,
            database,
            Firebase.auth.currentUser?.email.toString()
        )

    }

    fun fetchFriends(): Task<Boolean> {
        return userDataRepository.fetchFriendsInfo(
            database,
            _mailList
        )
    }

    fun newFriendCheck(): Task<Boolean> {
        return friendRepository.newFriendCheck(
            database,
            _mailList,
            _friendsList
        )
    }
}
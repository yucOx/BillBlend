package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Friend
import com.R.R.model.FriendRepository
import com.R.R.model.User
import com.R.R.model.UserDataRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendRequestViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val _usersMail = ArrayList<String>()
    private var _counter = 0

    private val userDataRepository: UserDataRepository = UserDataRepository()
    private val friendRepository : FriendRepository = FriendRepository()

    fun fetchFriendRequest(): Task<Boolean> {
        return friendRepository.fetchRequestMail(
            database,
            _usersMail,
        )
    }

    fun fetchUsersDetail(): Task<Boolean> {
        return userDataRepository.fetchFriendsInfo(
            database,
            _usersMail
        )
    }

    fun getFriendRequestCount(): Int {
        return _counter
    }

    fun getRequestInfo(): ArrayList<User> {
        return userDataRepository.getUsersInfo()
    }

    fun resetValues() {
        _usersMail.clear()
        userDataRepository.resetUsersInfo()
    }
}
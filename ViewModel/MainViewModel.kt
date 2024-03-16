package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Group
import com.R.R.model.GroupRepository
import com.R.R.model.User
import com.R.R.model.UserRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val _keyAndNameMap = hashMapOf<String, String>()
    private val _groupList = ArrayList<Group>()
    private val _groupNames = HashSet<String>()

    private val userRepository: UserRepository = UserRepository()
    private val groupRepository: GroupRepository = GroupRepository()

    fun getMainUser(): User {
        return userRepository.getUser()
    }

    fun fetchMainUserProfile(mail: String) {
        userRepository.fetchMainUserProfile(
            mail,
            database
        )
    }

    fun fetchGroups(mainUserMail: String): Task<Boolean> {
        return groupRepository.fetchGroups(
            database,
            mainUserMail,
            _groupList,
            _groupNames,
            _keyAndNameMap
        )
    }

    fun reFetchGroups(mainUserMail: String): Task<Boolean> {
       return groupRepository.reFetchGroups(
            _groupNames,
            mainUserMail,
            database
            )
    }

    fun getGroupList(): ArrayList<Group> {
        return _groupList
    }

    fun getGroupNames(): HashSet<String> {
        return _groupNames
    }

    fun getKeyAndNameMap(): HashMap<String, String> {
        return _keyAndNameMap
    }

    fun resetVariables() {
        _groupList.clear()
        _groupNames.clear()
        _keyAndNameMap.clear()
    }
}
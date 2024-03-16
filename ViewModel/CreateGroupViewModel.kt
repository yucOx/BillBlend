package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Group
import com.R.R.model.FriendRepository
import com.R.R.model.GroupRepository
import com.R.R.model.User
import com.R.R.model.UserDataRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateGroupViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _mailsOfFriend = ArrayList<String>()
    private val _nameAndSurnameList = mutableListOf<String>()
    private val _nameAndSurnameHashMap = hashMapOf<String, String>()
    private val _groupList = ArrayList<Group>()
    private val _groupRef = database.getReference("Groups")
    private val _snapKey = _groupRef.push().key.toString()
    private var _groupOwnerInfo = User()

    private val friendRepository: FriendRepository = FriendRepository()
    private val userDataRepository: UserDataRepository = UserDataRepository()
    private val groupRepository: GroupRepository = GroupRepository()

    fun fetchFriendsMail(): Task<Boolean> {
        return friendRepository.fetchFriendsMail(
            _mailsOfFriend,
            database,
            auth.currentUser?.email.toString()
        )
    }

    fun fetchUsersInfoToCreateGroup(): Task<Boolean> {
        return userDataRepository.fetchUsersInfoToCreateGroup(
            database,
            _groupOwnerInfo,
            auth.currentUser?.email.toString(),
            _mailsOfFriend,
            _nameAndSurnameList,
            _nameAndSurnameHashMap
        )
    }

    fun handleGroup(groupName: String, selectedUsers: MutableList<String>): Boolean {
        val groupUsersInfoArray = userDataRepository.getUsersInfo()

        _groupList.add(
            Group(
                _groupOwnerInfo.mail,
                groupName,
                _groupOwnerInfo.name,
                _groupOwnerInfo.surname,
                _groupOwnerInfo.mail,
                _snapKey
            )
        )
        for (userInfo in groupUsersInfoArray) {
            for (selectedUser in selectedUsers) {
                if (_nameAndSurnameHashMap[selectedUser] == userInfo.mail)
                    _groupList.add(
                        Group(
                            _groupOwnerInfo.mail,
                            groupName,
                            userInfo.name,
                            userInfo.surname,
                            userInfo.mail,
                            _snapKey
                        )
                    )
            }
        }
        return _groupList.size > 0
    }

    fun handleGroupForSingle(groupName: String): Boolean {
        _groupList.add(
            Group(
                _groupOwnerInfo.mail,
                groupName,
                _groupOwnerInfo.name,
                _groupOwnerInfo.surname,
                _groupOwnerInfo.mail,
                _snapKey
            )
        )
        return _groupList.size > 0
    }

    fun checkGroupName(groupName: String): Task<Boolean> {
        return groupRepository.checkGroupName(
            groupName,
            auth.currentUser?.email.toString(),
            _groupRef
        )
    }

    fun saveGroup(): Task<Boolean> {
        return groupRepository.saveGroup(
            _groupRef,
            _snapKey,
            _groupList
        )
    }

    fun getNameAndSurnameList() = _nameAndSurnameList

    fun getFriendInfoList() = userDataRepository.getUsersInfo()


}
package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Group
import com.R.R.model.User
import com.R.R.model.BillInfo
import com.R.R.model.BillRepository
import com.R.R.model.GroupRepository
import com.R.R.model.UserDataRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsOfGroupViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val _groupMembersArray = ArrayList<Group>()
    private var _groupName: String? = null
    private var _snapKey: String? = null
    private val _billNamesHash = hashSetOf<String>()
    private val _billsArray = ArrayList<BillInfo>()
    private val _photoLocationHashMap = HashMap<String, String>()

    private val groupRepository: GroupRepository = GroupRepository()
    private val userDataRepository: UserDataRepository = UserDataRepository()
    private val billRepository: BillRepository = BillRepository()

    fun fetchGroupMembers(): Task<Boolean> {
        return groupRepository.fetchGroupUsers(
            _groupMembersArray,
            _snapKey!!,
            database
        )
    }

    fun fetchUsersInfo(): Task<Boolean> {
        return userDataRepository.fetchGroupUsersInfo(
            auth.currentUser?.email.toString(),
            _snapKey.toString(),
            mutableListOf(),
            _groupMembersArray,
            User(),
            database
        )
    }

    fun fetchBills(): Task<Boolean> {
        return billRepository.fetchBills(
            database,
            _groupName,
            _photoLocationHashMap,
            _billsArray,
            _billNamesHash,
            _snapKey
        )
    }

    fun checkGroupOwner(): Boolean {
        return _groupMembersArray[0]
            .groupOwner.equals(auth.currentUser?.email.toString())
    }

    fun deleteToGroup(): Task<Boolean> {
        return groupRepository.deleteToGroup(
            database,
            _snapKey
        )
    }

    fun divideAndConquerToBills(): Task<Boolean> {
        return billRepository.divideAndConquerToBills(
            database,
            _snapKey
        )
    }

    fun leaveFromGroup(): Task<Boolean> {
        return groupRepository.leaveFromGroup(
            database,
            auth.currentUser?.email.toString(),
            _groupName
        )
    }

    fun getBills(): ArrayList<BillInfo> {
        return _billsArray
    }

    fun getPhotoLocationHashMap(): HashMap<String, String> {
        return _photoLocationHashMap
    }

    fun resetValues(): Boolean {
        _billNamesHash.clear()
        _billsArray.clear()
        _photoLocationHashMap.clear()

        return true
    }

    fun setGroupName(groupName: String) {
        _groupName = groupName
    }

    fun setSnapKey(snapKey: String) {
        _snapKey = snapKey
    }

    fun getGroupName() = _groupName

    fun getSnapKey() = _snapKey

    fun getGroupMembers() = _groupMembersArray

    fun getGroupMembersDetail() = userDataRepository.getUsersInfo()
}
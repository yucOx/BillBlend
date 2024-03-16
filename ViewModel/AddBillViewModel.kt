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
import java.util.Date

class AddBillViewModel : ViewModel() {
    private val userDataRepository: UserDataRepository = UserDataRepository()
    private val groupRepository: GroupRepository = GroupRepository()
    private val billRepository: BillRepository = BillRepository()

    private val _groupUsersArray = ArrayList<Group>()
    private val _database = FirebaseDatabase.getInstance()
    private val _mainUserInfo = User()
    private val _nameAndSurnameList = mutableListOf<String>()
    private val auth = FirebaseAuth.getInstance()
    private var _snapKey = ""
    private val _whoWillPayArray = ArrayList<BillInfo>()
    private var _whoWillPaySeperateCount = 0
    private var _groupName = ""

    fun setSnapKey(key: String) {
        _snapKey = key
    }

    fun setWhoWillPay(billInfo : BillInfo){
        _whoWillPayArray.add(
            billInfo
        )
    }

    fun isPriceValid(price : Int): Int {
        if (price < 0) {
            return 0
        }
        return 1
    }

    fun checkEmptyAreas(billName : String, price : String): Int {
        if (billName.isBlank() || price.isBlank()
        ) {
            return 1
        }
        return 0
    }

    fun isBillPriceEmpty(price : String): Int {
        if (price.isBlank()) {
            return 0
        }
        return 1
    }

    fun getNames() = _nameAndSurnameList

    fun setGroupName(groupName: String) {
        _groupName = groupName
    }

    fun fetchGroupUsers(): Task<Boolean> {
        return groupRepository.fetchGroupUsers(
            _groupUsersArray,
            _snapKey,
            _database
        )
    }

    fun fetchUsersInfo(): Task<Boolean> {
        val result = userDataRepository.fetchGroupUsersInfo(
            auth.currentUser?.email,
            _snapKey,
            _nameAndSurnameList,
            _groupUsersArray,
            _mainUserInfo,
            _database
        )
        return result
    }

    fun checkBillName(billName: String, onComplete: (Boolean) -> Unit) {
        return billRepository.checkBillName(
            billName,
            onComplete,
            _groupName,
            _database
        )
    }

    fun saveTheBill(selectedImage: String, billName: String, time: Date): Task<Boolean> {
        return billRepository.saveTheBill(
            selectedImage,
            billName,
            time,
            _database,
            _whoWillPayArray,
            _snapKey,
            _groupName
        )
    }

    fun saveTheBillForOne(
        selectedImage: String,
        billName: String,
        price: String,
        time: Date
    ): Task<Boolean> {
        return billRepository.saveTheBillForOne(
            selectedImage,
            billName,
            price,
            time,
            _database,
            _whoWillPayArray,
            _mainUserInfo,
            _snapKey,
            _groupName
        )
    }

    fun getWhoWillPay() = _whoWillPayArray

    fun getGroupUsers() = _groupUsersArray

    fun getUsersInfo() = userDataRepository.getUsersInfo()

    fun getWhoWillPaySeperate() = _whoWillPaySeperateCount

    fun incWhoWillPaySeperate() {
        _whoWillPaySeperateCount++
    }

    fun cleanWhoWillPaySeperate() {
        _whoWillPaySeperateCount = 0
    }
}
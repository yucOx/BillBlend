package com.yucox.splitwise.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.R.R.model.BillInfo
import com.R.R.model.BillRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase

class BillViewModel : ViewModel() {
    private val billRepository: BillRepository = BillRepository()
    private val _billDetails = ArrayList<BillInfo>()
    private val database = FirebaseDatabase.getInstance()

    private var _theBill = BillInfo()

    fun fetchBillDetails(): Task<Boolean> {
        return billRepository.fetchBillDetails(
            database,
            _theBill,
            _billDetails
        )
    }

    fun setIntentData(billName: String, groupName: String, snapKey: String, photoLocation: String) {
        _theBill.billname = billName
        _theBill.groupName = groupName
        _theBill.snapKeyOfGroup = snapKey
        _theBill.photoLocation = photoLocation
    }

    fun getBillDetails(): ArrayList<BillInfo> {
        return _billDetails
    }

    fun deleteToBill(): Task<Boolean> {
        return billRepository.deleteToBill(
            database,
            _theBill,
            _billDetails
        )
    }

    fun uploadPhoto(photo: Uri): Task<Boolean> {
        return billRepository.uploadPhoto(
            photo,
            _theBill.photoLocation ?: ""
        )
    }
}
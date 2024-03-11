package com.yucox.splitwise.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.R.R.model.BillInfo
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class BillViewModel : ViewModel() {
    private val billDetails = ArrayList<BillInfo>()
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("Bills")
    private val storage = FirebaseStorage.getInstance()

    private var _billName = ""
    private var _groupName = ""
    private var _snapKey = ""
    private var _photoLocation = ""

    fun fetchBillDetails(): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (!snap.exists())
                            continue
                        for (rSnap in snap.children) {
                            val temp = rSnap.getValue(BillInfo::class.java)
                            if (temp?.billname == _billName && temp?.groupName == _groupName && temp.snapKeyOfGroup == _snapKey)
                                billDetails.add(temp!!)
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

    fun setIntentData(billName: String, groupName: String, snapKey: String, photoLocation: String) {
        _billName = billName
        _groupName = groupName
        _snapKey = snapKey
        _photoLocation = photoLocation
    }

    fun getBillDetails(): ArrayList<BillInfo> {
        return billDetails
    }

    fun deleteBills(): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deleteTasks = mutableListOf<Task<Void>>()
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        for (rsnap in snap.children) {
                            val temp = rsnap.getValue(BillInfo::class.java)
                            if (temp?.billname == billDetails[0].billname && temp?.groupName == _groupName && temp.snapKeyOfGroup == _snapKey) {
                                val uniqueId = snap.key.toString()
                                val deleteTask = ref.child(uniqueId).removeValue()
                                deleteTasks.add(deleteTask)
                                if (_photoLocation.isNotEmpty()) {
                                    val photoDeleteTask = storage.getReference(_photoLocation)
                                        .delete()
                                            deleteTasks.add(photoDeleteTask)
                                }
                            }
                        }
                    }
                }
                Tasks.whenAll()
                    .addOnSuccessListener {
                        taskCompletionSource.setResult(true)
                    }
                    .addOnFailureListener{
                        taskCompletionSource.setResult(false)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun uploadPhoto(photo : Uri) : Task<Boolean>{
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        storage.getReference(_photoLocation)
            .putFile(photo)
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)

            }
            .addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }
}
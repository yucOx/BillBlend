package com.yucox.splitwise.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.R.R.model.Group
import com.R.R.model.User
import com.R.R.model.BillInfo
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Date

class AddBillViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val groupUsersArray = ArrayList<Group>()
    private val groupUsersInfoArray = ArrayList<User>()
    private val nameAndSurnameList = mutableListOf<String>()
    private val auth = FirebaseAuth.getInstance()
    private var snapKey = ""
    private val whoWillPayArray = ArrayList<BillInfo>()
    private var whoWillPaySeperateCount = 0
    private var _groupName = ""
    private var billKey = ""

    fun setSnapKey(key: String) {
        snapKey = key
    }

    fun getNames(): MutableList<String> {
        return nameAndSurnameList
    }

    fun setGroupName(groupName: String) {
        _groupName = groupName
    }

    fun fetchGroupUsers(): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val groupRef = database.getReference("Groups")
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realsnap in snap.children) {
                                var temp = realsnap.getValue(Group::class.java)
                                if (temp?.snapKeyOfGroup == snapKey) {
                                    groupUsersArray.add(temp!!)
                                }
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

    fun fetchUsersInfo(): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refUsersData = database.getReference("UsersData")
        refUsersData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var i = 0
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(User::class.java)
                        for (user in groupUsersArray.distinct()) {
                            if (user?.email == temp?.mail && user.snapKeyOfGroup == snapKey) {
                                if (user.email != auth.currentUser?.email) {
                                    nameAndSurnameList.add(i, "${user.name} ${user.surname}")
                                    i++
                                }
                                groupUsersInfoArray.add(temp!!)
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

    fun checkBillName(billName: String, onComplete: (Boolean) -> Unit) {
        val refOfBills = database.getReference("Bills")
        var check = false
        refOfBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (!snap.exists())
                            continue

                        for (rsnap in snap.children) {
                            if (rsnap.child("groupName").getValue().toString()
                                    .equals(_groupName) && rsnap.child("billname").getValue()
                                    .toString() == billName
                            ) {
                                check = true
                                break
                            }
                        }

                    }
                }
                onComplete(check)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun saveTheBill(selectedImage: String, billName: String, time: Date): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refOfBills = database.getReference("Bills")
        billKey = refOfBills.push().key.toString()

        setWhoWillPay(
            BillInfo(
                "",
                _groupName,
                2,
                "",
                0.0,
                0.0,
                billName,
                billKey,
                snapKey,
                time
            )
        )
        refOfBills.child(billKey).setValue(getWhoWillPay())
            .addOnSuccessListener {
                if (!selectedImage.isNullOrBlank())
                    taskCompletionSource.setResult(true)
                else {
                    Firebase.storage.getReference(billKey).putFile(Uri.parse(selectedImage))
                        .addOnSuccessListener {
                            taskCompletionSource.setResult(true)
                        }
                        .addOnFailureListener {
                            taskCompletionSource.setResult(true)
                        }
                }
            }
        return taskCompletionSource.task
    }

    fun saveTheBillForOne(selectedImage: String, billName: String, time: Date): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refOfBills = database.getReference("Bills")

        billKey = refOfBills.push().key.toString()
        setWhoWillPay(
            BillInfo(
                "",
                _groupName,
                2,
                "",
                0.0,
                0.0,
                billName,
                billKey,
                snapKey,
                time
            )
        )
        refOfBills.child(billKey).setValue(getWhoWillPay())
            .addOnSuccessListener {
                if (!selectedImage.isBlank()) {
                    val storageRef = Firebase.storage.getReference(billKey)
                    storageRef.putFile(Uri.parse(selectedImage))
                        .addOnSuccessListener {
                            taskCompletionSource.setResult(true)
                        }
                        .addOnFailureListener {
                            taskCompletionSource.setResult(true)
                        }
                } else
                    taskCompletionSource.setResult(true)
            }
        return taskCompletionSource.task
    }

    fun setWhoWillPay(whoHowmuch: BillInfo) {
        whoWillPayArray.add(whoHowmuch)
    }

    fun getWhoWillPay(): ArrayList<BillInfo> {
        return whoWillPayArray
    }

    fun getGroupUsers(): ArrayList<Group> {
        return groupUsersArray
    }

    fun getUsersInfo(): ArrayList<User> {
        return groupUsersInfoArray
    }

    fun incWhoWillPaySeperate() {
        whoWillPaySeperateCount++
    }

    fun getWhoWillPaySeperate(): Int {
        return whoWillPaySeperateCount
    }

    fun cleanWhoWillPaySeperate() {
        whoWillPaySeperateCount = 0
    }

}
package com.yucox.splitwise.ViewModel

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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DetailsOfGroupViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val groupMembers = ArrayList<Group>()
    private var groupMembersDetail = ArrayList<User>()
    private var _groupName: String? = null
    private var _snapKey: String? = null
    private val billNamesHash = hashSetOf<String>()
    private val billsArray = ArrayList<BillInfo>()
    private val photoLocationHashMap = HashMap<String, String>()


    fun setGroupName(groupName: String) {
        _groupName = groupName
    }

    fun setSnapKey(snapKey: String) {
        _snapKey = snapKey
    }

    fun getGroupName(): String? {
        return _groupName
    }

    fun getSnapKey(): String? {
        return _snapKey
    }

    fun getGroupMembers(): ArrayList<Group> {
        return groupMembers
    }

    fun getGroupMembersDetail(): ArrayList<User> {
        return groupMembersDetail
    }

    fun fetchGroupMembers(): Task<Boolean> {
        val refGroup = database.getReference("Groups")
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        refGroup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.key != _snapKey) {
                            continue
                        }
                        for (realSnap in snap.children) {
                            val temp = realSnap.getValue(Group::class.java)
                            groupMembers.add(
                                Group(
                                    temp?.groupOwner,
                                    temp?.groupName,
                                    temp?.name,
                                    temp?.surname,
                                    temp?.email
                                )
                            )
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

    fun fetchMembersDetail(): Task<Boolean> {
        val refUsersData = database.getReference("UsersData")
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        refUsersData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(User::class.java)
                        for (user in groupMembers) {
                            if (temp?.name == user.name && temp?.mail == user?.email) {
                                groupMembersDetail.distinct()
                                groupMembersDetail.add(temp!!)
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

    fun fetchBills(): Task<Boolean> {
        val refBills = database.getReference("Bills")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (!snap.exists())
                            continue

                        for (realSnap in snap.children) {
                            val temp = realSnap.getValue(BillInfo::class.java)
                            if (temp?.groupName == _groupName && temp?.snapKeyOfGroup == _snapKey) {
                                if (temp?.whohasPaid == 2) {
                                    photoLocationHashMap.put(
                                        temp.billname.toString(),
                                        temp.photoLocation.toString()
                                    )
                                }
                                if (temp?.billname in billNamesHash == false) {
                                    billsArray.add(temp!!)
                                    billNamesHash.add(temp?.billname.toString())
                                }
                            }
                        }
                    }
                }
                if (billsArray.size == 0) {
                    taskCompletionSource.setResult(false)
                } else {
                    taskCompletionSource.setResult(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun checkGroupOwner(): Boolean {
        return groupMembers[0]
            .groupOwner.equals(auth.currentUser?.email.toString())
    }

    fun deleteToGroup(): Task<Boolean> {
        val refGroup = database.getReference("Groups")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refGroup.child(_snapKey.toString()).removeValue()
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)
            }.addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

    fun divideAndConquer(): Task<Boolean> {
        val refBills = database.getReference("Bills")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        for (rsnap in snap.children) {
                            val checkSnapKey = rsnap.child("snapKeyOfGroup").getValue()
                            if (checkSnapKey?.equals(_snapKey) == false)
                                continue

                            val checkPhotoLocation =
                                rsnap.child("photoLocation").getValue().toString()
                            if (checkPhotoLocation.isEmpty())
                                continue

                            deleteBillsAndPhotos(checkPhotoLocation, snap.key.toString())
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

    fun deleteBillsAndPhotos(photoLocation: String, snapKeyBill: String) {
        Firebase.storage.getReference(photoLocation)
            .delete()
        Firebase.database.getReference("Bills")
            .child(snapKeyBill)
            .removeValue()
    }

    fun leaveFromGroup(): Task<Boolean> {
        val refGroup = database.getReference("Groups")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refGroup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    if (!snap.exists()) {
                        continue
                    }
                    for (superSnap in snap.children) {
                        val checkMail = superSnap.child("email").value
                        val checkName = superSnap.child("groupName").value
                        if (!(checkMail == auth.currentUser?.email && checkName == _groupName))
                            continue

                        refGroup.child(snap.key.toString())
                            .child(superSnap.key.toString())
                            .removeValue()
                            .addOnSuccessListener {
                                taskCompletionSource.setResult(true)
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun getBills(): ArrayList<BillInfo> {
        return billsArray
    }

    fun getPhotoLocationHashMap(): HashMap<String, String> {
        return photoLocationHashMap
    }

    fun resetValues(): Boolean {
        billNamesHash.clear()
        billsArray.clear()
        photoLocationHashMap.clear()

        return true
    }
}
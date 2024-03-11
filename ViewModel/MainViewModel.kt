package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Group
import com.R.R.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MainViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val mainUser = User()
    private val keyAndNameMap = hashMapOf<String, String>()
    private val groupList = ArrayList<Group>()
    private val groupNames = HashSet<String>()


    fun getMainUser(): User {
        return mainUser
    }

    fun fetchMainUserProfile(mail: String) {
        val storage = Firebase.storage.getReference(mail)

        storage.downloadUrl.addOnSuccessListener { uri ->
            mainUser.pfpUri = uri.toString()
        }
        val userListRef = database.getReference("UsersData")
        userListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue<User>()
                        if (temp?.mail == mail) {
                            mainUser.name = temp.name
                            mainUser.surname = temp.surname
                            mainUser.mail = temp.mail
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchGroups(mainUserMail: String): Task<Boolean> {
        val groupRef = database.getReference("Groups")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (!snap.exists())
                            continue
                        for (realSnap in snap.children) {
                            val temp = realSnap.getValue(Group::class.java)
                            if (mainUserMail == temp?.email) {
                                groupList.add(temp)
                                groupNames.add(temp.groupName.toString())
                                keyAndNameMap.put(
                                    temp?.groupName!!,
                                    temp.snapKeyOfGroup!!
                                )
                                if (snap.key !in keyAndNameMap.values)
                                    continue

                                for (rsnap in snap.children) {
                                    groupList.add(rsnap.getValue<Group>()!!)
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

    fun reFetchGroups(mainUserMail: String): Task<Boolean> {
        val groupRef = database.getReference("Groups")
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val tempGroupNames = mutableListOf<String>()

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (!snap.exists())
                            continue
                        for (realSnap in snap.children) {
                            val temp = realSnap.getValue(Group::class.java)
                            if (mainUserMail != temp?.email)
                                continue

                            tempGroupNames.add(temp.groupName.toString())
                        }
                    }
                }
                if (tempGroupNames.size != groupNames.size)
                    taskCompletionSource.setResult(true)
                else
                    taskCompletionSource.setResult(false)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun getGroupList(): ArrayList<Group> {
        return groupList
    }

    fun getGroupNames(): HashSet<String> {
        return groupNames
    }

    fun getKeyAndNameMap(): HashMap<String, String> {
        return keyAndNameMap
    }

    fun resetVariables() {
        groupList.clear()
        groupNames.clear()
        keyAndNameMap.clear()
    }
}
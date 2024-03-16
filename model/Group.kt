package com.R.R.model

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.io.Serializable

data class Group(
    var groupOwner: String? = "",
    var groupName: String? = "",
    var name: String? = "",
    var surname: String? = "",
    var email: String? = "",
    var snapKeyOfGroup: String? = ""
) : Serializable

class GroupRepository {
    fun fetchGroups(
        database: FirebaseDatabase,
        mainUserMail: String,
        groupList: ArrayList<Group>,
        groupNames: HashSet<String>,
        keyAndNameMap: HashMap<String, String>
    ): Task<Boolean> {
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
                                groupList.add(temp!!)
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

    fun reFetchGroups(
        groupNames: HashSet<String>,
        mainUserMail: String,
        database: FirebaseDatabase
    ): Task<Boolean> {
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

    fun fetchGroupUsers(
        groupUsersArray: ArrayList<Group>,
        snapKey: String,
        database: FirebaseDatabase
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val groupRef = database.getReference("Groups")
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realsnap in snap.children) {
                                val temp = realsnap.getValue(Group::class.java)
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

    fun checkGroupName(
        groupName: String,
        auth: String,
        groupRef: DatabaseReference
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var check = 0
                if (!snapshot.exists())
                    return
                for (snap in snapshot.children) {
                    if (!snap.exists())
                        return
                    for (rSnap in snap.children) {
                        val tempGroupName = rSnap.child("groupName").getValue()?.toString()
                        val tempGroupOwner = rSnap.child("groupOwner").getValue()?.toString()
                        if (tempGroupName.equals(groupName) && tempGroupOwner.equals(auth))
                            check = 1
                    }
                }
                if (check == 1)
                    taskCompletionSource.setResult(true)
                else if (check == 0)
                    taskCompletionSource.setResult(false)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return taskCompletionSource.task
    }

    fun saveGroup(
        groupRef: DatabaseReference,
        snapKey: String,
        groupList: ArrayList<Group>
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        groupRef.child(snapKey).setValue(groupList)
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)
            }
            .addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

    fun deleteToGroup(database: FirebaseDatabase, snapKey: String?): Task<Boolean> {
        val refGroup = database.getReference("Groups")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refGroup.child(snapKey.toString()).removeValue()
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)
            }.addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

    fun leaveFromGroup(
        database: FirebaseDatabase,
        auth: String,
        groupName: String?
    ): Task<Boolean> {
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
                        if (!(checkMail == auth && checkName == groupName))
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
}
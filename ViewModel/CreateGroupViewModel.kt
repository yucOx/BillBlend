package com.yucox.splitwise.ViewModel

import androidx.lifecycle.ViewModel
import com.R.R.model.Group
import com.R.R.model.Friend
import com.R.R.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CreateGroupViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val mails = ArrayList<String>()
    private val nameAndSurnameList = mutableListOf<String>()
    private val nameAndSurnameHashMap = hashMapOf<String,String>()
    private val friendInfoList = ArrayList<User>()
    private val groupList = ArrayList<Group>()
    private val groupRef = database.getReference("Groups")
    private val snapKey = groupRef.push().key.toString()
    private var groupOwnerInfo = User()


    fun fetchFriendsMail(): Task<Boolean> {
        val ref = database.getReference("FriendRequest")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val tempControl = snap.getValue(Friend::class.java)
                        if (tempControl?.whoGetFriendRequest == auth.currentUser?.email) {
                            if (tempControl?.whoSentFriendRequest !in mails) {
                                if (tempControl?.status == 1) {
                                    mails.add(tempControl?.whoSentFriendRequest.toString())
                                }
                            }
                        } else if (tempControl?.whoSentFriendRequest == auth.currentUser?.email) {
                            if (tempControl?.whoGetFriendRequest !in mails) {
                                if (tempControl?.status == 1) {
                                    mails.add(tempControl?.whoGetFriendRequest.toString())
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

    fun fetchUsersInfo() : Task<Boolean>{
        val usersDataRef = database.getReference("UsersData")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        usersDataRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var i = 0
                    for(snap in snapshot.children){
                        var temp = snap.getValue(User::class.java)
                        if(temp?.mail == auth.currentUser?.email.toString())
                            groupOwnerInfo = temp
                        for(userMail in mails){
                            if(userMail == temp?.mail){
                                nameAndSurnameHashMap.put("${temp.name} ${temp.surname}",temp.mail.toString())
                                nameAndSurnameList.add(i,"${temp.name.toString()} ${temp.surname.toString()}")
                                friendInfoList.add(User(temp.name, temp.surname, temp.mail,
                                    temp.pfpUri
                                ))
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

    fun createGroup(groupName : String,selectedUsers : MutableList<String>) : Boolean{
        groupList.add(Group(auth.currentUser?.email.toString(),
            groupName,
            groupOwnerInfo.name,
            groupOwnerInfo.surname,
            groupOwnerInfo.mail,
            snapKey))
        for(userInfo in friendInfoList){
            for(selectedUser in selectedUsers){
                if(nameAndSurnameHashMap[selectedUser] == userInfo.mail)
                    groupList.add(Group(auth.currentUser?.email.toString(),
                        groupName,
                        userInfo.name,
                        userInfo.surname,
                        userInfo.mail,
                        snapKey))
            }
        }
        return groupList.size > 0
    }

    fun createEmptyGroup(groupName : String) : Boolean{
        groupList.add(Group(groupOwnerInfo.mail,
            groupName,
            groupOwnerInfo.name,
            groupOwnerInfo.surname,
            groupOwnerInfo.mail,
            snapKey
        ))
        return groupList.size > 0
    }

    fun checkGroupName(groupName : String) : Task<Boolean>{
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var check = 0
                if(!snapshot.exists())
                    return
                for(snap in snapshot.children){
                    if(!snap.exists())
                        return
                    for(rSnap in snap.children){
                        if(rSnap.child("groupName").getValue()?.toString().equals(groupName)
                            && rSnap.child("groupOwner").getValue()?.toString().equals(auth.currentUser?.email.toString()))
                            check = 1
                    }
                }
                if(check == 1)
                    taskCompletionSource.setResult(true)
                else if(check == 0)
                    taskCompletionSource.setResult(false)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        return taskCompletionSource.task
    }

    fun saveCreatedData() : Task<Boolean>{
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        groupRef.child(snapKey).setValue(groupList)
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)
            }
            .addOnFailureListener{
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

    fun getNameAndSurnameList() : MutableList<String>{
        return nameAndSurnameList
    }

    fun getFriendInfoList() : ArrayList<User>{
        return friendInfoList
    }


}
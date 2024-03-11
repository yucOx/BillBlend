package com.yucox.splitwise.ViewModel

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.R.R.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.Model.PhotoAndMail
import kotlin.coroutines.Continuation

class SearchViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("UsersData")
    private val queryList = ArrayList<User>()
    private val allUsersList = ArrayList<User>()
    private val photoAndMails = ArrayList<PhotoAndMail>()

    fun fetchUsers() {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                queryList.clear()
                if (!snapshot.exists())
                    return

                for (snap in snapshot.children) {
                    val tempUser = snap.getValue(User::class.java)
                    if (tempUser?.mail != Firebase.auth.currentUser?.email)
                        allUsersList.add(tempUser!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun searchUser(nameQuery: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val trimmedQuery = nameQuery.trim().lowercase()

        queryList.clear()
        photoAndMails.clear()
        if (trimmedQuery.isNotBlank()) {
            for (a in allUsersList) {
                val b = a.name?.lowercase()

                if (b != null && b.contains(trimmedQuery)) {
                    queryList.add(a)
                }
            }
            taskCompletionSource.setResult(true)
        } else
            taskCompletionSource.setResult(false)

        return taskCompletionSource.task
    }

    fun getUserPhoto(): Task<Boolean> {
        val tasks = mutableListOf<Task<*>>()
        for (user in queryList) {
            val mail = user.mail.toString()
            val task = Firebase.storage.getReference(mail).downloadUrl
                .addOnSuccessListener { uri ->
                    photoAndMails.add(PhotoAndMail(uri.toString(), mail))
                }
                .addOnFailureListener {
                    photoAndMails.add(PhotoAndMail(null, mail))
                }
            tasks.add(task)
        }

        return Tasks.whenAll(tasks).continueWith { continuation ->
            true
        }
    }

    fun getQueryList() : ArrayList<User>{
        return queryList
    }

    fun getPhotoAndMails() : ArrayList<PhotoAndMail>{
        return photoAndMails
    }
}
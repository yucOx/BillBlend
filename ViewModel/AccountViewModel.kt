package com.yucox.splitwise.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.R.R.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class AccountViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private var _user = User()


    fun signIn(mail: String, password: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        auth.signInWithEmailAndPassword(mail, password)
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)
            }
            .addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

    fun createAccount(user: User, password: String, pfp: String): Task<Boolean> {
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageRef = firebaseStorage.getReference(user.mail.toString())
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        auth.createUserWithEmailAndPassword(user.mail.toString(), password)
            .addOnSuccessListener {
                if (pfp.isBlank())
                    taskCompletionSource.setResult(true)
                storageRef.putFile(Uri.parse(pfp))
                    .addOnSuccessListener {
                        taskCompletionSource.setResult(true)
                    }
                    .addOnFailureListener {
                        taskCompletionSource.setResult(true)
                    }
            }
            .addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

    fun isAnyoneIn(): Boolean {
        return auth.currentUser != null
    }

    fun cleanAndSetUserInfo(name: String, surname: String, mail: String): User {
        if (name.startsWith(" ")) {
            val _name = name.replace(" ", "")
            _user.name = _name
        } else
            _user.name = name

        if (surname.startsWith(" ") || surname.endsWith("")) {
            val _surname = surname.replace(" ", "")
            _user.surname = _surname
        } else
            _user.surname = surname

        if (mail.startsWith(" ") || mail.endsWith("")) {
            val _mail = mail.replace(" ", "")
            _user.mail = _mail
        } else
            _user.mail = mail

        _user.pfpUri = ""
        return _user
    }

    fun checkBlankArea(user: User): Boolean {
        if (user.name?.isBlank() == true)
            return true
        if (user.surname?.isBlank() == true)
            return true
        return user.mail?.isBlank() == true
    }

    fun getUser(): User {
        return _user
    }
}
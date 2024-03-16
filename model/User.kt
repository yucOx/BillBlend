package com.R.R.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.Model.PhotoAndMail

data class User(
    var name: String? = "",
    var surname: String? = "",
    var mail: String? = "",
    var pfpUri: String? = ""
)

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private var user = User()

    fun getUser() = user

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

    fun fetchMainUserProfile(mail: String, database: FirebaseDatabase) {
        val storage = Firebase.storage.getReference(mail)
        storage.downloadUrl.addOnSuccessListener { uri ->
            user.pfpUri = uri.toString()
        }
        val userListRef = database.getReference("UsersData")
        userListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue<User>()
                        if (temp?.mail == mail) {
                            user.name = temp.name
                            user.surname = temp.surname
                            user.mail = temp.mail
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

class UserDataRepository {
    private val usersInfoArray = ArrayList<User>()

    fun getUserPhoto(
        queryList: ArrayList<User>,
        photoAndMails: ArrayList<PhotoAndMail>
    ): Task<Boolean> {
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

    fun fetchAllUsers(queryList: ArrayList<User>) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("UsersData")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                queryList.clear()
                if (!snapshot.exists())
                    return

                for (snap in snapshot.children) {
                    val tempUser = snap.getValue(User::class.java)
                    if (tempUser?.mail != Firebase.auth.currentUser?.email)
                        usersInfoArray.add(tempUser!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchFriendsInfo(
        database: FirebaseDatabase,
        usersMail: ArrayList<String>
    ): Task<Boolean> {
        val usersDataRef = database.getReference("UsersData")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        usersDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val getTempValue = snap.getValue(User::class.java)
                        for (userMail in usersMail) {
                            if (getTempValue?.mail != userMail)
                                continue

                            usersInfoArray.add(
                                getTempValue
                            )
                        }
                    }
                    taskCompletionSource.setResult(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun fetchGroupUsersInfo(
        auth: String?,
        snapKey: String,
        nameAndSurnameList: MutableList<String>,
        groupUsersArray: ArrayList<Group>,
        mainUserInfo: User,
        database: FirebaseDatabase
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refUsersData = database.getReference("UsersData")

        refUsersData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var i = 0
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        val temp = snap.getValue(User::class.java)
                        if (temp?.mail == auth) {
                            mainUserInfo.name = temp?.name
                            mainUserInfo.surname = temp?.surname
                            mainUserInfo.mail = auth
                        }
                        for (user in groupUsersArray.distinct()) {
                            if (user?.email == temp?.mail &&
                                user.snapKeyOfGroup == snapKey
                            ) {
                                if (user.email != auth) {
                                    nameAndSurnameList.add(i, "${user.name} ${user.surname}")
                                    i++
                                }
                                usersInfoArray.add(temp!!)
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

    fun fetchUsersInfoToCreateGroup(
        database: FirebaseDatabase,
        groupOwnerInfo: User,
        auth: String?,
        mailsOfFriend: ArrayList<String>,
        nameAndSurnameList: MutableList<String>,
        nameAndSurnameHashMap: HashMap<String, String>
    ): Task<Boolean> {
        val usersDataRef = database.getReference("UsersData")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        usersDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    return
                }

                val i = 0
                for (snap in snapshot.children) {
                    val temp = snap.getValue(User::class.java)
                    if (temp?.mail == auth) {
                        groupOwnerInfo.name = temp?.name
                        groupOwnerInfo.surname = temp?.surname
                        groupOwnerInfo.mail = auth
                    }
                    for (userMail in mailsOfFriend) {
                        if (userMail != temp?.mail)
                            continue

                        nameAndSurnameHashMap.put(
                            "${temp.name} ${temp.surname}",
                            temp.mail.toString()
                        )
                        nameAndSurnameList.add(
                            i,
                            "${temp.name.toString()} ${temp.surname.toString()}"
                        )
                        usersInfoArray.add(
                            User(
                                temp.name, temp.surname, temp.mail,
                                temp.pfpUri
                            )
                        )
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

    fun getUsersInfo() = usersInfoArray

    fun resetUsersInfo() = usersInfoArray.clear()

}
package com.yucox.splitwise.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.SendFriendRequest
import com.yucox.splitwise.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileDetailActivity : AppCompatActivity() {
    private lateinit var nameText: TextView
    private lateinit var surnameText: TextView
    private lateinit var mailText: TextView
    private lateinit var profilePic: CircleImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_detail_activity)
        var name = intent.getStringExtra("name")
        var surname = intent.getStringExtra("surname")
        var mail = intent.getStringExtra("mail")

        nameText = findViewById(R.id.name)
        surnameText = findViewById(R.id.surname)
        mailText = findViewById(R.id.mail)
        profilePic = findViewById(R.id.profilepic)
        var goToBackBtn = findViewById<ImageView>(R.id.backToBack)
        var deleteFriend = findViewById<ImageView>(R.id.deleteFriend)
        var addFriend = findViewById<ImageView>(R.id.addFriend)
        deleteFriend.visibility = View.GONE
        addFriend.visibility = View.GONE

        setHaveGottenData(name, surname, mail)
        setProfilePhoto(mail)
        checkFriendStatus(mail)
        deleteFriend.setOnClickListener {
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Arkadaşlıktan çıkarmak istediğinize emin misiniz?")
                .setNegativeButton("Evet") { dialog, which ->
                    deleteFriendFromData(mail)
                }
                .setPositiveButton("Hayır") { dialog, which -> }.show()
        }
        addFriend.setOnClickListener {
            addOrWaitFriendRequest(mail)
        }
        goToBackBtn.setOnClickListener {
            backButton()
        }
    }

    private fun addOrWaitFriendRequest(mail : String?) {
        var ref = Firebase.database.getReference("FriendRequest")
        var sendFriendRequest = SendFriendRequest()
        sendFriendRequest.whoSentFriendRequest = Firebase.auth.currentUser?.email
        sendFriendRequest.whoGetFriendRequest = mail
        sendFriendRequest.status = 0
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var check = 0
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var a = snap.getValue(SendFriendRequest::class.java)
                        if (a != null) {
                            if (sendFriendRequest.whoSentFriendRequest == a.whoSentFriendRequest.toString() && sendFriendRequest.whoGetFriendRequest
                                == a.whoGetFriendRequest.toString() && a.status == 0
                            ) {
                                check = 1
                            } else if (sendFriendRequest.whoSentFriendRequest == a.whoSentFriendRequest.toString() && sendFriendRequest.whoGetFriendRequest
                                == a.whoGetFriendRequest.toString() && a.status == 1
                            ) {
                                check = 2
                            } else if (sendFriendRequest.whoSentFriendRequest != a.whoSentFriendRequest && sendFriendRequest.whoGetFriendRequest != a.whoGetFriendRequest) {
                                check = 0
                            }
                        }
                    }
                }
                if (check == 0) {
                    ref.push().setValue(sendFriendRequest)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    this@ProfileDetailActivity,
                                    "Başarıyla istek gönderildi!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                } else if (check == 1) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@ProfileDetailActivity,
                            "Daha önceden istek gönderildi.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun deleteFriendFromData(mail: String?) {
        var ref = Firebase.database.getReference("FriendRequest")
        var deleteFriend = findViewById<ImageView>(R.id.deleteFriend)
        var isFriend = findViewById<TextView>(R.id.isFriend)
        var addFriend = findViewById<ImageView>(R.id.addFriend)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(SendFriendRequest::class.java)
                        if (temp?.whoGetFriendRequest == mail || temp?.whoSentFriendRequest == mail) {
                            var key = snap.key
                            ref.child(key.toString()).removeValue()
                                .addOnSuccessListener {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(
                                            this@ProfileDetailActivity,
                                            "Arkadaş başarıyla silindi.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        deleteFriend.visibility = View.GONE
                                        addFriend.visibility = View.VISIBLE
                                        isFriend.text = "Arkadaş değilsiniz."
                                    }
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun checkFriendStatus(mail: String?) {
        var ref = Firebase.database.getReference("FriendRequest")
        var deleteFriend = findViewById<ImageView>(R.id.deleteFriend)
        var isFriend = findViewById<TextView>(R.id.isFriend)
        var addFriend = findViewById<ImageView>(R.id.addFriend)

        var controler = 0
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(SendFriendRequest::class.java)
                        if ((temp?.whoSentFriendRequest == mail && temp?.status == 1 && Firebase.auth.currentUser?.email
                                    == temp?.whoGetFriendRequest) || (temp?.whoGetFriendRequest == mail && temp?.status == 1 &&
                                    Firebase.auth.currentUser?.email == temp?.whoSentFriendRequest)
                        ) {
                            if (Firebase.auth.currentUser?.email != mail) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    deleteFriend.visibility = View.VISIBLE
                                    isFriend.text = "Arkadaşsınız"
                                }
                                controler = 1
                            }
                            if (Firebase.auth.currentUser?.email == mail) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    isFriend.text = "Siz"
                                }
                                controler = 1
                            }
                        }
                    }
                }
                if (controler == 0) {
                    if (mail != Firebase.auth.currentUser?.email) {
                        CoroutineScope(Dispatchers.Main).launch {
                            addFriend.visibility = View.VISIBLE
                            isFriend.text = "Arkadaş değilsiniz."
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            isFriend.text = "Siz"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setProfilePhoto(mail: String?) {
        mailText = findViewById(R.id.mail)
        profilePic = findViewById(R.id.profilepic)
        Firebase.storage.getReference(mail.toString()).downloadUrl
            .addOnSuccessListener { uri ->
                if (!this.isFinishing) {
                    Glide.with(this@ProfileDetailActivity).load(uri).into(profilePic)
                }
            }
            .addOnFailureListener {
                if (!this.isFinishing) {
                    Glide.with(this@ProfileDetailActivity).load(R.drawable.splitwisecat)
                        .into(profilePic)
                }
            }
    }

    private fun setHaveGottenData(name: String?, surname: String?, mail: String?) {
        nameText = findViewById(R.id.name)
        surnameText = findViewById(R.id.surname)
        mailText = findViewById(R.id.mail)

        if (name?.isNotEmpty() == true) {
            nameText.text = name
        }
        if (surname?.isNotEmpty() == true) {
            surnameText.text = surname
        }
        if (mail?.isNotEmpty() == true) {
            mailText.text = mail
        }
    }
    private fun backButton() {
        finish()
    }
}
package com.yucox.splitwise.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.R.R.model.SendFriendRequest
import com.R.R.model.UserInfo
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.ShowRequestAdapter


class AnswerFriendshipRequest : AppCompatActivity() {
    private lateinit var runnable: Runnable
    private lateinit var adapter : ShowRequestAdapter
    private lateinit var swipeRefreshLayout : SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.answer_friendship_request_activity)

        var database = Firebase.database
        var ref = database.getReference("FriendRequest")
        var auth = FirebaseAuth.getInstance()

        var handler = Handler()
        var requestCounter = findViewById<TextView>(R.id.howmuchRequestAnswerFriendship)
        var counter = 0
        var userInfo = ArrayList<UserInfo>()
        var listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userInfo.clear()
                    counter = 0
                    for (snap in snapshot.children) {
                        var a = snap.getValue(SendFriendRequest::class.java)
                        println(a?.whoGetFriendRequest)
                        if (auth.currentUser?.email == a?.whoGetFriendRequest) {
                            if (a?.status == 0) {
                                counter++
                                userInfo.add(UserInfo("","",a.whoSentFriendRequest,""))
                            }
                        }
                    }
                }
                addUserDetail(userInfo)
                if(counter > 0) {
                    requestCounter.text = "$counter yeni arkadaşlık isteği"
                }else{
                    requestCounter.text = "Arkadaşlık isteğiniz yok."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        listener
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeAnswerFriendship)
        swipeRefreshLayout.setOnRefreshListener {
            userInfo.clear()
            ref.addValueEventListener(listener)
            adapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun getData(tempUserInfo : ArrayList<UserInfo>){
       var userInfo = ArrayList<UserInfo>()
        for(user in tempUserInfo){
            if(user.name?.isBlank() == true){

            }else{
                userInfo.add(user)
            }
        }

        var randomPfp = ArrayList<Int>()
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.tesla)

        adapter = ShowRequestAdapter(this@AnswerFriendshipRequest,userInfo,randomPfp)
        var recyclerView = findViewById<RecyclerView>(R.id.FriendshipRequestRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this@AnswerFriendshipRequest,RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapter

    }
    private fun addUserDetail(tempUserInfo: ArrayList<UserInfo>) {
        var database = Firebase.database
        var ref2 = database.getReference("UsersData")
        var getRealUserInfo = ArrayList<UserInfo>()
        ref2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var getTempValue = snap.getValue(UserInfo::class.java)
                        for(user in tempUserInfo){
                            if(getTempValue?.mail == user.mail){
                                println("veriler eşleşti")
                                getRealUserInfo.add(UserInfo(getTempValue?.name,getTempValue?.surname,getTempValue?.mail,getTempValue?.pfpUri))
                            }
                        }
                    }
                    getData(getRealUserInfo)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        var back_to_mainactivity = findViewById<ImageView>(R.id.backToMainFromRequest)
        back_to_mainactivity.setOnClickListener {
            finish()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
package com.yucox.splitwise.activity



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.R.R.model.SendFriendRequest
import com.R.R.model.UserInfo
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.FriendAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendsActivity : AppCompatActivity() {
    private lateinit var adapter : FriendAdapter
    private lateinit var listener : ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friends_activity)

        var doyouHaveFriend = findViewById<TextView>(R.id.doyouHave)
        doyouHaveFriend.visibility = View.GONE

        var mailOfFriends = HashSet<String>()

        var refUsersData = Firebase.database.getReference("UsersData")
        var ref = Firebase.database.getReference("FriendRequest")


        var userInfos = ArrayList<UserInfo>()
        var hashControl = HashSet<String>()
        listener = (object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var userList = ArrayList(mailOfFriends)
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue(UserInfo::class.java)
                        if(temp?.mail in userList){
                            if(temp?.mail in hashControl){
                                continue
                            }else{
                                userInfos.add(temp!!)
                            }
                            hashControl.add(temp?.mail.toString())
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            var counter = 0
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue(SendFriendRequest::class.java)
                        if( Firebase.auth.currentUser?.email == temp?.whoSentFriendRequest){
                            if(temp?.status == 1){
                                mailOfFriends.add(temp?.whoGetFriendRequest.toString())
                                counter++
                            }
                        }else if(Firebase.auth.currentUser?.email == temp?.whoGetFriendRequest ){
                            if(temp?.status == 1) {
                                mailOfFriends.add(temp?.whoSentFriendRequest.toString())
                                counter++
                            }
                        }
                    }
                }
                if(counter == 0){
                    CoroutineScope(Dispatchers.Main).launch {
                        doyouHaveFriend.visibility = View.VISIBLE
                        doyouHaveFriend.text = "Ekli hiçbir arkadaşınız yok."
                    }
                }
                refUsersData.addListenerForSingleValueEvent(listener)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        adapter = FriendAdapter(this@FriendsActivity,mailOfFriends,userInfos)
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerFriend)
        recyclerView.layoutManager = LinearLayoutManager(this@FriendsActivity,RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapter

        var backToMainActivity = findViewById<ImageView>(R.id.backToMainActivity)
        backToMainActivity.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
package com.yucox.splitwise.adapter

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.R.R.model.SendFriendRequest
import com.R.R.model.UserInfo
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.R
import com.yucox.splitwise.model.GetUserPhotoWithName
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchAdapter(private val context: Context, private var userList: ArrayList<UserInfo>, var getUserPhotoWNameArray : ArrayList<GetUserPhotoWithName>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pfp = view.findViewById<CircleImageView>(R.id.profileUserItemr)
        var name =  view.findViewById<TextView>(R.id.nameUserItem)
        var surname = view.findViewById<TextView>(R.id.surnameUserItem)
        var mail = view.findViewById<TextView>(R.id.mailUserItem)
        var addFriend = view.findViewById<ImageView>(R.id.addFriendBtnUserItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = userList[position]
        var database = Firebase.database
        var ref = database.getReference("FriendRequest")

        holder.name.text = item.name.toString()
        holder.surname.text = item.surname.toString()
        holder.mail.text = item.mail.toString()
            for(a in getUserPhotoWNameArray){
                if(a.mail == item.mail ) {
                    if (!a.photo.isNullOrEmpty())
                        Glide.with(context).load(a.photo).into(holder.pfp)
                    else
                        Glide.with(context).load(R.drawable.splitwisecat).into(holder.pfp)
                }
            }

        holder.addFriend.setOnClickListener {
            var auth = FirebaseAuth.getInstance()
            var senderMail = auth.currentUser?.email
            if(senderMail?.isBlank() == false && item?.mail?.isBlank() == false){
                var sendFriendRequest = SendFriendRequest(senderMail,item.mail,0)
                ref.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var check = 0
                        if(snapshot.exists()){
                            for(snap in snapshot.children){
                                var a = snap.getValue(SendFriendRequest::class.java)
                                if (a != null) {
                                    if(sendFriendRequest.whoSentFriendRequest == a.whoSentFriendRequest.toString() && sendFriendRequest.whoGetFriendRequest
                                        == a.whoGetFriendRequest.toString() && a.status == 0){
                                        check = 1
                                    }else if(sendFriendRequest.whoSentFriendRequest == a.whoSentFriendRequest.toString() && sendFriendRequest.whoGetFriendRequest
                                        == a.whoGetFriendRequest.toString() && a.status == 1) {
                                        check = 2
                                    }
                                    else if(sendFriendRequest.whoSentFriendRequest != a.whoSentFriendRequest && sendFriendRequest.whoGetFriendRequest != a.whoGetFriendRequest){
                                        check = 0
                                    }

                                }
                            }
                            if(check == 0){
                                ref.push().setValue(sendFriendRequest)
                                    .addOnSuccessListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(context,"Başarıyla istek gönderildi!",Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }else if(check == 1){
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context,"Daha önceden istek gönderildi.",Toast.LENGTH_LONG).show()
                                }
                            }else if(check == 2){
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context,"Zaten ${item.name}  ${item.surname} ile arkadaşsınız",Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }

        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
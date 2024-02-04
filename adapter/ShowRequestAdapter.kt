package com.yucox.splitwise.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.SendFriendRequest
import com.R.R.model.UserInfo
import com.google.firebase.auth.ktx.auth
import com.yucox.splitwise.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowRequestAdapter(private val context: Context, private var userList: ArrayList<UserInfo>, var randomImg : ArrayList<Int>) :
    RecyclerView.Adapter<ShowRequestAdapter.ViewHolder>() {
    val database = Firebase.database
    val ref = database.getReference("FriendRequest")

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pfp = view.findViewById<CircleImageView>(R.id.profileShowRequestItem)
        var name =  view.findViewById<TextView>(R.id.nameShowRequestItem)
        var surname = view.findViewById<TextView>(R.id.surnameShowRequestItem)
        var mail = view.findViewById<TextView>(R.id.mailShowRequestItem)
        var acceptBtn = view.findViewById<ImageView>(R.id.acceptRequestShowRequestItem)
        var rejectBtn = view.findViewById<ImageView>(R.id.refuseRequestShowRequestItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.show_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = userList[position]
        holder.name.text = item.name.toString()
        holder.surname.text = item.surname.toString()
        holder.mail.text = item.mail.toString()

        acceptRequest(holder.acceptBtn,item)
        rejectRequest(holder.rejectBtn,item)

        getUsersPhotos(item,holder.pfp)

    }

    private fun getUsersPhotos(item: UserInfo, pfp: CircleImageView) {
        Firebase.storage.getReference(item.mail.toString()).downloadUrl
            .addOnSuccessListener {uri->
                Glide.with(context).load(uri).into(pfp)
            }.addOnFailureListener{
                Glide.with(context).load(randomImg.shuffled()[0]).into(pfp)
            }
    }

    private fun rejectRequest(rejectBtn: ImageView, item: UserInfo) {
        rejectBtn.setOnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Arkadaşlık isteğini reddetmek istediğine emin misin?")
            builder.setNegativeButton("Evet") { dialog, which ->
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                var a = snap.getValue(SendFriendRequest::class.java)
                                if (a?.whoSentFriendRequest == item.mail && a?.whoGetFriendRequest == Firebase.auth.currentUser?.email) {
                                    var uniqueId = snap.key.toString()
                                    ref.child(uniqueId).removeValue()
                                        .addOnCompleteListener{it ->
                                            if(it.isSuccessful){
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    userList.remove(item)
                                                    notifyDataSetChanged()
                                                }
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
            builder.setPositiveButton("Hayır"){dialog,which->}.show()
        }
    }

    private fun acceptRequest(acceptBtn: ImageView, item: UserInfo) {
        acceptBtn.setOnClickListener {
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(snap in snapshot.children){
                            var a = snap.getValue(SendFriendRequest::class.java)
                            if(a?.whoSentFriendRequest == item.mail && a?.whoGetFriendRequest == Firebase.auth.currentUser?.email){
                                var uniqueId = snap.key.toString()
                                ref.child(uniqueId).child("status").setValue(1)
                                    .addOnCompleteListener{it ->
                                        if(it.isSuccessful){
                                            CoroutineScope(Dispatchers.Main).launch {
                                                userList.remove(item)
                                                notifyDataSetChanged()
                                            }
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
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}


package com.yucox.splitwise.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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

class ShowRequestAdapter(private val context: Context, private var userList: ArrayList<UserInfo>, var randomImg : ArrayList<Int>) :
    RecyclerView.Adapter<ShowRequestAdapter.ViewHolder>() {
    lateinit var randomImgShuffled : List<Int>
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pfp = view.findViewById<CircleImageView>(R.id.profileShowRequestItem)
        var name =  view.findViewById<TextView>(R.id.nameShowRequestItem)
        var surname = view.findViewById<TextView>(R.id.surnameShowRequestItem)
        var mail = view.findViewById<TextView>(R.id.mailShowRequestItem)
        var linearLayout = view.findViewById<LinearLayout>(R.id.selectLinearShowRequest)
        var acceptBtn = view.findViewById<ImageView>(R.id.acceptRequestShowRequestItem)
        var rejectBtn = view.findViewById<ImageView>(R.id.refuseRequestShowRequestItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.show_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var database = Firebase.database
        var ref = database.getReference("FriendRequest")

        val item = userList[position]
        holder.name.text = item.name.toString()
        holder.surname.text = item.surname.toString()
        holder.mail.text = item.mail.toString()
        holder.acceptBtn.setOnClickListener {
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(snap in snapshot.children){
                            var a = snap.getValue(SendFriendRequest::class.java)
                            if(a?.whoSentFriendRequest == item.mail && a?.whoGetFriendRequest == Firebase.auth.currentUser?.email){
                                var uniqueId = snap.key.toString()
                                ref.child(uniqueId).child("status").setValue(1)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        holder.rejectBtn.setOnClickListener {
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
        Firebase.storage.getReference(item.mail.toString()).downloadUrl
            .addOnSuccessListener {uri->
                Glide.with(context).load(uri).into(holder.pfp)
            }.addOnFailureListener{
                Glide.with(context).load(randomImg.shuffled()[0]).into(holder.pfp)
            }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}


package com.yucox.splitwise.adapter


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.UserInfo
import com.google.firebase.auth.ktx.auth
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.ProfileDetailActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendAdapter(
    private val context: Context,
    private var friendsMail: HashSet<String>,
    var friendsInfo: ArrayList<UserInfo>
) :
    RecyclerView.Adapter<FriendAdapter.ViewHolder>() {
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pfp = view.findViewById<CircleImageView>(R.id.pfpUser)
        var name = view.findViewById<TextView>(R.id.nameUserItem)
        var surname = view.findViewById<TextView>(R.id.surnameUserItem)
        var mail = view.findViewById<TextView>(R.id.mailUserItem)
        var unfriendBtn = view.findViewById<ImageView>(R.id.unfriendBtn)
        var open_friend_detail = view.findViewById<ConstraintLayout>(R.id.open_friend_detail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.friendsitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (friendsInfo.isNotEmpty()) {
            holder.name.text = friendsInfo[position].name
            holder.surname.text = friendsInfo[position].surname
            holder.mail.text = friendsInfo[position].mail
        }
        if (friendsInfo.isNotEmpty()) {
            if (holder.mail.text == friendsInfo[position].mail) {
                if (!(context as Activity).isFinishing) {
                    Firebase.storage.getReference(friendsInfo[position].mail.toString()).downloadUrl
                        .addOnSuccessListener { uri ->
                            Glide.with(context).load(uri).into(holder.pfp)
                        }.addOnFailureListener {
                            Glide.with(context).load(R.drawable.dostoyevski).into(holder.pfp)
                        }
                }
            }
        }
        holder.open_friend_detail.setOnClickListener {
            var intent = Intent(context, ProfileDetailActivity::class.java)
            intent.putExtra("name", friendsInfo[position].name)
            intent.putExtra("surname", friendsInfo[position].surname)
            intent.putExtra("mail", friendsInfo[position].mail)
            context.startActivity(intent)
        }
        holder.unfriendBtn.setOnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Arkadaşlıktan çıkarmak istediğinize emin misiniz?")
            builder.setNegativeButton("Evet") { dialog, which ->
                var ref = Firebase.database.getReference("FriendRequest")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                if (snap.child("whoGetFriendRequest")
                                        .getValue() == friendsInfo[position].mail && snap.child("whoSentFriendRequest")
                                        .getValue() ==
                                    Firebase.auth.currentUser?.email
                                ) {
                                    var key = snap.key
                                    ref.child(key.toString()).removeValue()
                                        .addOnSuccessListener {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Toast.makeText(
                                                    context,
                                                    "Arkadaşlıktan çıkarıldı.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                (context as Activity).finish()
                                            }
                                        }
                                } else if (snap.child("whoSentFriendRequest")
                                        .getValue() == friendsInfo[position].mail && snap.child("whoGetFriendRequest")
                                        .getValue() ==
                                    Firebase.auth.currentUser?.email
                                ) {
                                    var key = snap.key
                                    ref.child(key.toString()).removeValue()
                                        .addOnSuccessListener {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Toast.makeText(
                                                    context,
                                                    "Arkadaşlıktan çıkarıldı.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                (context as Activity).finish()
                                            }
                                        }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }.setPositiveButton("Hayır") { dialog, which -> }.show()
        }
    }

    override fun getItemCount(): Int {
        return friendsInfo.size
    }
}
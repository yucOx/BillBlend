package com.yucox.splitwise.adapter


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.ProfileDetailActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendAdapter(
    private val context: Context,
    var friendsInfo: ArrayList<UserInfo>
) :
    RecyclerView.Adapter<FriendAdapter.ViewHolder>() {
    val mailAndPicHashMap = HashMap<String, Uri>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pfp = view.findViewById<CircleImageView>(R.id.pfpUser)
        var name = view.findViewById<TextView>(R.id.nameUserItem)
        var surname = view.findViewById<TextView>(R.id.surnameUserItem)
        var mail = view.findViewById<TextView>(R.id.mailUserItem)
        var unfriendBtn = view.findViewById<ImageView>(R.id.unfriendBtn)
        var frameConstView = view.findViewById<ConstraintLayout>(R.id.open_friend_detail)
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

        getAndSetProfilePics(holder.mail,position,holder.pfp)

        goToFriendDetail(holder.frameConstView,position)

        unfriend(holder.unfriendBtn,position)

    }

    private fun goToFriendDetail(openFriendDetail: ConstraintLayout, position: Int) {
         openFriendDetail.setOnClickListener {
            var intent = Intent(context, ProfileDetailActivity::class.java)
            intent.putExtra("name", friendsInfo[position].name)
            intent.putExtra("surname", friendsInfo[position].surname)
            intent.putExtra("mail", friendsInfo[position].mail)
            intent.putExtra("mailAndPicHashMap",mailAndPicHashMap)
            context.startActivity(intent)
        }
    }

    private fun unfriend(unfriendBtn: ImageView, position: Int) {
        unfriendBtn.setOnClickListener {
            var builder = MaterialAlertDialogBuilder(context)
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

    private fun getAndSetProfilePics(mail: TextView, position: Int, pfp: CircleImageView) {
        if (friendsInfo.isNotEmpty()) {
            if (mail.text == friendsInfo[position].mail) {
                if (!(context as Activity).isFinishing) {
                    Firebase.storage.getReference(friendsInfo[position].mail.toString()).downloadUrl
                        .addOnSuccessListener { uri ->
                            mailAndPicHashMap.put(friendsInfo[position].mail.toString(),uri)
                            Glide.with(context).load(uri).into(pfp)
                        }.addOnFailureListener {
                            mailAndPicHashMap.put(friendsInfo[position].mail.toString(),Uri.parse(R.drawable.splitwisecat.toString()))
                            Glide.with(context).load(R.drawable.dostoyevski).into(pfp)
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return friendsInfo.size
    }
}
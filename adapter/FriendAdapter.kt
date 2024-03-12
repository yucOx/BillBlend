package com.yucox.splitwise.Adapter


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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.yucox.splitwise.R
import com.yucox.splitwise.View.ShowProfileActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendAdapter(
    private val context: Context, var friendList: ArrayList<User>
) : RecyclerView.Adapter<FriendAdapter.ViewHolder>() {
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
        if (friendList.isNotEmpty()) {
            holder.name.text = friendList[position].name
            holder.surname.text = friendList[position].surname
            holder.mail.text = friendList[position].mail
        }

        fetchProfileAndSet(holder.mail, friendList[position], holder.pfp)

        holder.frameConstView.setOnClickListener {
            goToFriendDetail(position)
        }

        holder.unfriendBtn.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle("Arkadaşlıktan çıkarmak istediğinize emin misiniz?")
                .setNegativeButton("Evet") { dialog, which ->
                    unfriend(friendList[position])
                }
                .setPositiveButton("Hayır") { dialog, which ->
                }
                .show()
        }

    }

    private fun goToFriendDetail(position: Int) {
        val intent = Intent(context, ShowProfileActivity::class.java)
        intent.putExtra("name", friendList[position].name)
        intent.putExtra("surname", friendList[position].surname)
        intent.putExtra("mail", friendList[position].mail)
        intent.putExtra("mailAndPicHashMap", mailAndPicHashMap)
        context.startActivity(intent)
    }

    private fun unfriend(friend: User) {
        val mainUserMail = Firebase.auth.currentUser?.email
        val ref = Firebase.database.getReference("FriendRequest")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                    return
                for (snap in snapshot.children) {
                    val receiver = snap.child("whoGetFriendRequest").getValue()
                    val sender = snap.child("whoSentFriendRequest").getValue()

                    if (!((receiver == friend.mail && sender == mainUserMail) || (sender == friend.mail && receiver == mainUserMail)))
                        continue

                    val key = snap.key.toString()
                    ref.child(key).removeValue().addOnSuccessListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context, "Arkadaşlıktan çıkarıldı.", Toast.LENGTH_LONG
                            ).show()
                            (context as Activity).finish()
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun fetchProfileAndSet(mailTv: TextView, friend: User, pfp: CircleImageView) {
        if (friendList.isEmpty())
            return
        if (mailTv.text != friend.mail)
            return
        if ((context as Activity).isFinishing)
            return
        val mail = friend.mail.toString()
        Firebase.storage.getReference(mail).downloadUrl
            .addOnSuccessListener { uri ->
                mailAndPicHashMap.put(mail, uri)
                Glide.with(context).load(uri).into(pfp)
            }.addOnFailureListener {
                mailAndPicHashMap.put(
                    mail,
                    Uri.parse(R.drawable.splitwisecat.toString())
                )
                Glide.with(context).load(R.drawable.dostoyevski).into(pfp)
            }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}
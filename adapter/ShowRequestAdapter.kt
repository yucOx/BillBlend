package com.yucox.splitwise.Adapter


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
import com.R.R.model.Friend
import com.R.R.model.User
import com.google.firebase.auth.ktx.auth
import com.yucox.splitwise.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowRequestAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    private val randomImg: ArrayList<Int>
) :
    RecyclerView.Adapter<ShowRequestAdapter.ViewHolder>() {
    private val database = Firebase.database
    private val ref = database.getReference("FriendRequest")
    private val mainUserMail = Firebase.auth.currentUser?.email

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pfp = view.findViewById<CircleImageView>(R.id.profileShowRequestItem)
        val name = view.findViewById<TextView>(R.id.nameShowRequestItem)
        val surname = view.findViewById<TextView>(R.id.surnameShowRequestItem)
        val mail = view.findViewById<TextView>(R.id.mailShowRequestItem)
        val acceptBtn = view.findViewById<ImageView>(R.id.acceptRequestShowRequestItem)
        val rejectBtn = view.findViewById<ImageView>(R.id.refuseRequestShowRequestItem)
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

        fetchAndSetProfile(item, holder.pfp)

        holder.acceptBtn.setOnClickListener {
            acceptRequest(item)
        }
        holder.rejectBtn.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Arkadaşlık isteğini reddetmek istediğine emin misin?")
                .setNegativeButton("Evet") { dialog, which ->
                    rejectRequest(item)
                }
                .setPositiveButton("Hayır") { dialog, which ->
                }
                .show()
        }
    }

    private fun fetchAndSetProfile(item: User, pfp: CircleImageView) {
        Firebase.storage.getReference(item.mail.toString()).downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(context).load(uri).into(pfp)
            }.addOnFailureListener {
                Glide.with(context).load(randomImg.shuffled()[0]).into(pfp)
            }
    }

    private fun rejectRequest(item: User) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                    return
                for (snap in snapshot.children) {
                    val temp = snap.getValue(Friend::class.java)
                    val receiver = temp?.whoGetFriendRequest
                    val sender = temp?.whoSentFriendRequest
                    if (!(sender == item.mail && receiver == mainUserMail))
                        continue

                    val uniqueId = snap.key.toString()
                    ref.child(uniqueId).removeValue()
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    userList.remove(item)
                                    notifyDataSetChanged()
                                }
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun acceptRequest(item: User) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                    return

                for (snap in snapshot.children) {
                    val temp = snap.getValue(Friend::class.java)
                    val receiver = temp?.whoGetFriendRequest
                    val sender = temp?.whoSentFriendRequest
                    if (!(sender == item.mail && receiver == mainUserMail))
                        return

                    val uniqueId = snap.key.toString()
                    ref.child(uniqueId).child("status").setValue(1)
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    userList.remove(item)
                                    notifyDataSetChanged()
                                }
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}


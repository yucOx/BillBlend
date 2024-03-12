package com.yucox.splitwise.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.R.R.model.Friend
import com.R.R.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.FirebaseDatabase
import com.yucox.splitwise.R
import com.yucox.splitwise.Model.PhotoAndMail
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchAdapter(
    private val context: Context,
    private var userList: ArrayList<User>,
    private var photoAndMails: ArrayList<PhotoAndMail>
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private val auth = FirebaseAuth.getInstance()
    private val mainUserMail = auth.currentUser?.email
    private val database = FirebaseDatabase.getInstance()
    private val friendRequestRef = database.getReference("FriendRequest")


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pfp = view.findViewById<CircleImageView>(R.id.profileUserItemr)
        val name = view.findViewById<TextView>(R.id.nameUserItem)
        val surname = view.findViewById<TextView>(R.id.surnameUserItem)
        val mail = view.findViewById<TextView>(R.id.mailUserItem)
        val addFriend = view.findViewById<ImageView>(R.id.addFriendBtnUserItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = userList[position]
        holder.name.text = item.name.toString()
        holder.surname.text = item.surname.toString()
        holder.mail.text = item.mail.toString()

        if (item.mail?.isBlank() == true)
            holder.addFriend.visibility = View.GONE
        else if (item.mail?.isBlank() == false && holder.addFriend.visibility == View.GONE)
            holder.addFriend.visibility = View.VISIBLE

        for (a in photoAndMails) {
            if (a.mail == item.mail) {
                if (!a.photo.isNullOrEmpty())
                    Glide.with(context).load(a.photo).into(holder.pfp)
                else
                    Glide.with(context).load(R.drawable.splitwisecat).into(holder.pfp)
            }
        }

        holder.addFriend.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = checkStatus(userList[position]).await()

                when (result) {
                    0 -> {
                        val requestItem = Friend(mainUserMail, item.mail, 0)
                        sendRequest(requestItem)
                    }

                    1 -> {
                        Toast.makeText(
                            context,
                            "Zaten ${item.name} ${item.surname} ile arkadaşsınız",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    -1 -> {
                        Toast.makeText(
                            context, "Daha önceden istek gönderildi.", Toast.LENGTH_LONG
                        ).show()
                    }

                    -3 -> Toast.makeText(
                        context, R.string.try_later, Toast.LENGTH_LONG
                    ).show()

                }
            }
        }
    }

    private fun checkStatus(user: User): Task<Int> {
        val taskCompletionSource = TaskCompletionSource<Int>()
        friendRequestRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var _status = 0
                if (!snapshot.exists())
                    return

                for (snap in snapshot.children) {
                    val temp = snap.getValue(Friend::class.java)
                    val receiver = temp?.whoGetFriendRequest
                    val sender = temp?.whoSentFriendRequest
                    val status = temp?.status

                    if (!((sender == user.mail && mainUserMail == receiver) || (receiver == user.mail && mainUserMail == sender)))
                        continue

                    if (status == 1) {
                        _status = 1
                        break
                    }

                    if (status == 0)
                        _status = -1
                }
                taskCompletionSource.setResult(_status)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(-3)
            }
        })
        return taskCompletionSource.task
    }

    private fun sendRequest(requestItem: Friend) {
        friendRequestRef.push().setValue(requestItem)
            .addOnSuccessListener {
                Toast.makeText(
                    context, "İstek gönderildi.", Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
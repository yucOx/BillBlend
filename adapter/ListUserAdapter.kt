package com.yucox.splitwise.adapter


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.Group
import com.R.R.model.UserInfo
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.ProfileDetailActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListUserAdapter(
    private val context: Context,
    private val userList: ArrayList<UserInfo>,
    private val groupuserList: ArrayList<Group>
) : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {
    var makemeUp = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameInfo = view.findViewById<TextView>(R.id.showName_profileimageshow)
        var surnameInfo = view.findViewById<TextView>(R.id.showSurname_profileimageshow)
        var pfp = view.findViewById<CircleImageView>(R.id.pfp_profileimageshow)
        var founderofGroup = view.findViewById<ImageView>(R.id.founderofGroup_profileimageshow)
        var selectLinear = view.findViewById<LinearLayout>(R.id.linearLayout4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.listuser_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var cleanuserList = userList.distinct()
        var cleangroupuserList = groupuserList.distinct()
        val groupOwner = cleangroupuserList[0].groupOwner
        val userDetails = cleanuserList[position]

        var randomPfp = mutableListOf<Int>()
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)

        holder.selectLinear.setOnClickListener {
            val intent = Intent(context, ProfileDetailActivity::class.java)
            intent.putExtra("name", userDetails.name)
            intent.putExtra("surname", userDetails.surname)
            intent.putExtra("mail", userDetails.mail)
            context.startActivity(intent)
        }

        if (groupOwner == userDetails.mail) {
            holder.founderofGroup.setImageResource(R.drawable.theboss)
            holder.nameInfo.text = "${userDetails.name}"
            holder.surnameInfo.text = userDetails.surname
        } else {
            holder.nameInfo.text = "${userDetails.name}"
            holder.surnameInfo.text = userDetails.surname
        }

        if (userDetails.mail?.isNotEmpty() == true) {
            Firebase.storage.getReference(userDetails.mail.toString())
                .downloadUrl.addOnSuccessListener { uri ->
                    if (!(context as Activity).isFinishing) {
                        Glide.with(context).load(uri).into(holder.pfp)
                    }
                }.addOnFailureListener {
                    if (!(context as Activity).isFinishing) {
                        Glide.with(context).load(randomPfp.shuffled()[0]).into(holder.pfp)
                    }
                }
        }
    }

    override fun getItemCount(): Int {
        var cleanuserList = userList.distinct()
        return cleanuserList.size
    }

}
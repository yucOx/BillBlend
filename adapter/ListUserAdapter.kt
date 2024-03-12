package com.yucox.splitwise.Adapter


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.Group
import com.R.R.model.User
import com.yucox.splitwise.R
import com.yucox.splitwise.View.ShowProfileActivity
import de.hdodenhof.circleimageview.CircleImageView

class ListUserAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    private val groupuserList: ArrayList<Group>,
    private val randomPfp: MutableList<Int>
) : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {
    val mailAndPicHashMap = HashMap<String, Uri>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameInfo = view.findViewById<TextView>(R.id.showName_profileimageshow)
        val pfp = view.findViewById<CircleImageView>(R.id.pfp_profileimageshow)
        val selectLinear = view.findViewById<LinearLayout>(R.id.linearLayout4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.listuser_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cleanuserList = userList.distinct()
        val userDetails = cleanuserList[position]

        holder.nameInfo.text = "${userDetails.name}"
        fetchPfpAndSet(userDetails, mailAndPicHashMap, holder.pfp)

        holder.selectLinear.setOnClickListener {
            openProfile(userDetails, mailAndPicHashMap)
        }
    }

    private fun fetchPfpAndSet(
        userDetails: User, mailAndPicHashMap: HashMap<String, Uri>, pfp: CircleImageView
    ) {
        if (userDetails.mail?.isEmpty() == true)
            return

        Firebase.storage.getReference(userDetails.mail.toString()).downloadUrl.addOnSuccessListener { uri ->
            if ((context as Activity).isFinishing)
                return@addOnSuccessListener

            Glide.with(context).load(uri).into(pfp)
            mailAndPicHashMap.put(userDetails.mail.toString(), uri)

        }.addOnFailureListener {
            if ((context as Activity).isFinishing)
                return@addOnFailureListener

            Glide.with(context).load(randomPfp.shuffled()[0]).into(pfp)
            mailAndPicHashMap.put(
                userDetails.mail.toString(),
                Uri.parse(randomPfp.shuffled()[0].toString())
            )
        }
    }

    private fun openProfile(
        userDetails: User, mailAndPicHashMap: HashMap<String, Uri>
    ) {
        val intent = Intent(context, ShowProfileActivity::class.java)
        intent.putExtra("name", userDetails.name)
        intent.putExtra("surname", userDetails.surname)
        intent.putExtra("mail", userDetails.mail)

        if (mailAndPicHashMap.isEmpty()) {
            Toast.makeText(
                context,
                R.string.try_later,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        intent.putExtra("mailAndPicHashMap", mailAndPicHashMap)
        context.startActivity(intent)

    }

    override fun getItemCount(): Int {
        val cleanuserList = userList.distinct()
        return cleanuserList.size
    }

}
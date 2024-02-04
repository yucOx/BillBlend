package com.yucox.splitwise.adapter


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
import com.R.R.model.UserInfo
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.ProfileDetailActivity
import de.hdodenhof.circleimageview.CircleImageView

class ListUserAdapter(
    private val context: Context,
    private val userList: ArrayList<UserInfo>,
    private val groupuserList: ArrayList<Group>,
    private val randomPfp: MutableList<Int>
) : RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameInfo = view.findViewById<TextView>(R.id.showName_profileimageshow)
        var pfp = view.findViewById<CircleImageView>(R.id.pfp_profileimageshow)
        var selectLinear = view.findViewById<LinearLayout>(R.id.linearLayout4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.listuser_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cleanuserList = userList.distinct()
        val userDetails = cleanuserList[position]
        val mailAndPicHashMap = HashMap<String, Uri>()

        holder.nameInfo.text = "${userDetails.name}"

        getPfpAndSet(userDetails,mailAndPicHashMap,holder.pfp)

        openProfile(holder.selectLinear,userDetails,mailAndPicHashMap)

    }

    private fun getPfpAndSet(
        userDetails: UserInfo,
        mailAndPicHashMap: HashMap<String, Uri>,
        pfp: CircleImageView
    ) {
        if (userDetails.mail?.isNotEmpty() == true) {
            Firebase.storage.getReference(userDetails.mail.toString())
                .downloadUrl.addOnSuccessListener { uri ->
                    if (!(context as Activity).isFinishing) {
                        Glide.with(context).load(uri).into(pfp)
                        mailAndPicHashMap.put(userDetails.mail.toString(),uri)
                    }
                }.addOnFailureListener {
                    if (!(context as Activity).isFinishing) {
                        Glide.with(context).load(randomPfp.shuffled()[0]).into(pfp)
                        mailAndPicHashMap.put(userDetails.mail.toString(),Uri.parse(randomPfp.shuffled()[0].toString()))
                    }
                }
        }
    }

    private fun openProfile(
        selectLinear: LinearLayout,
        userDetails: UserInfo,
        mailAndPicHashMap: HashMap<String, Uri>
    ) {
        selectLinear.setOnClickListener {
            val intent = Intent(context, ProfileDetailActivity::class.java)
            intent.putExtra("name", userDetails.name)
            intent.putExtra("surname", userDetails.surname)
            intent.putExtra("mail", userDetails.mail)
            if(mailAndPicHashMap.isNullOrEmpty()){
                Toast.makeText(context,"Bir sorunla karşılaşıldı, lütfen daha sonra tekrar deneyin",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            intent.putExtra("mailAndPicHashMap",mailAndPicHashMap)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        var cleanuserList = userList.distinct()
        return cleanuserList.size
    }

}
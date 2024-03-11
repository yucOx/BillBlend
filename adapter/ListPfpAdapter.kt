package com.yucox.splitwise.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.yucox.splitwise.R
import de.hdodenhof.circleimageview.CircleImageView

class ListPfpAdapter(
    private val context: Context,
    private val usersMail : HashSet<String>,
    private val counter: Int
) :
    RecyclerView.Adapter<ListPfpAdapter.ViewHolder>() {
    private val storage = FirebaseStorage.getInstance()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_pfp_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return counter
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val convertedMails = ArrayList<String>(usersMail)
        val mail = convertedMails[position]
        storage.getReference(mail).downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(context).load(uri).into(holder.pfpCv)
            }
            .addOnFailureListener{
                Glide.with(context).load(R.drawable.luffy).into(holder.pfpCv)
            }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pfpCv = view.findViewById<CircleImageView>(R.id.pfpCv)
    }
}
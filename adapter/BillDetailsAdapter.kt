package com.yucox.splitwise.adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.R.R.model.UserInfo
import com.R.R.model.WhoHowmuch
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.ProfileDetailActivity

class BillDetailsAdapter(private val context: Context, private var whoMustPay: ArrayList<WhoHowmuch>) :
    RecyclerView.Adapter<BillDetailsAdapter.ViewHolder>() {
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userPfp = view.findViewById<ImageView>(R.id.userPfp_billdetails)
        var whoWillPay = view.findViewById<TextView>(R.id.whoWillPay)
        var paid = view.findViewById<ImageView>(R.id.hePaid)
        var hadntPaid = view.findViewById<ImageView>(R.id.hadntPaid)
        var isHePaidTextView = view.findViewById<TextView>(R.id.isHePaid)
        var howMuchWillPay = view.findViewById<TextView>(R.id.howMuchHeWillPay)
        var tickAsPayed = view.findViewById<ImageView>(R.id.tickAsPayed)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.bill_details_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(counter == 0){

        }else if (counter >= 0){
            whoMustPay.clear()
            counter = 0
        }

        var intentName = ""
        var intentSurname = ""
        var intentMail = ""
        holder.userPfp.setOnClickListener {
            var ref = Firebase.database.getReference("UsersData")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(snap in snapshot.children){
                            var temp = snap.getValue(UserInfo::class.java)
                            if("${temp?.name} ${temp?.surname}" == whoMustPay[position].whoWillPay){
                                intentName = temp?.name.toString()
                                intentSurname = temp?.surname.toString()
                                intentMail = temp?.mail.toString()
                            }
                        }
                    }
                    if(!intentName.isBlank() && !intentSurname.isBlank() && !intentMail.isBlank()){
                        var intent = Intent(context, ProfileDetailActivity::class.java)
                        intent.putExtra("name",intentName)
                        intent.putExtra("surname",intentSurname)
                        intent.putExtra("mail",intentMail)
                        context.startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        val item = whoMustPay[position]
        holder.whoWillPay.text = item.whoWillPay
        holder.hadntPaid.visibility = View.GONE
        holder.paid.visibility = View.GONE
        holder.tickAsPayed.visibility = View.GONE

        if(item.whohasPaid == 0){
            holder.hadntPaid.visibility = View.VISIBLE
            holder.isHePaidTextView.text = "Ödenmedi"
        }else{
            holder.paid.visibility = View.VISIBLE
            holder.isHePaidTextView.text = "Ödendi"
        }
        var realHowMuch = String.format("%.2f",item.howmuchWillpay)
        holder.howMuchWillPay.text = "$realHowMuch₺"


        var nameInUserList = mutableListOf<String>()
        for(name in whoMustPay){
            nameInUserList.add(name.whoWillPay.toString())
        }

        var database = FirebaseDatabase.getInstance()
        var ref = database.getReference("UsersData")
        var getUserDetail = ArrayList<UserInfo>()
        var getMainUserName : String? = ""
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue(UserInfo::class.java)
                        if("${temp?.name} ${temp?.surname}" in nameInUserList){
                            getUserDetail.add(temp!!)
                        }
                        if(temp?.mail == Firebase.auth.currentUser?.email){
                            getMainUserName = "${temp?.name} ${temp?.surname}"
                        }
                    }
                }
                if(item.whohasPaid == 0 && getMainUserName == item.whoWillPay){
                    holder.tickAsPayed.visibility = View.VISIBLE
                }else{
                    holder.tickAsPayed.visibility = View.GONE
                }
                var firebaseStorage = FirebaseStorage.getInstance()
                for(user in getUserDetail){
                    println("*******")
                    println(item.whoWillPay)
                    println(user.mail)
                    println("*******")
                    if(item.whoWillPay == "${user.name} ${user.surname}") {
                        firebaseStorage.getReference(user.mail.toString()).downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(context).load(uri.toString()).into(holder.userPfp)
                        }.addOnFailureListener{
                            Glide.with(context).load(R.drawable.luffy).into(holder.userPfp)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        holder.tickAsPayed.setOnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Faturayı ödediniz mi?")
            builder.setPositiveButton("Hayır"){dialog,which ->

            }.setNegativeButton("Evet") {dialog,which ->
                var databaseForBill = Firebase.database
                var refForBill = databaseForBill.getReference("Bills")
                refForBill.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                if (snap.exists()) {
                                    for (rsnap in snap.children) {
                                        var temp = rsnap.getValue(WhoHowmuch::class.java)
                                        var getRealRef = rsnap.ref
                                        if (temp?.whohasPaid == 0 && temp?.whoWillPay == getMainUserName && temp?.billname == item.billname) {
                                            getRealRef.child("whohasPaid").setValue(1)
                                            whoMustPay.clear()
                                        }
                                    }
                                }
                            }
                        }
                    }


                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }.show()
        }
    }

    override fun getItemCount(): Int {
        return whoMustPay.size
    }
}

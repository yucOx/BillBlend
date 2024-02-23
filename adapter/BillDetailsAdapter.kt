package com.yucox.splitwise.adapter


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.gms.ads.MobileAds
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.ProfileDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillDetailsAdapter(
    private val context: Context,
    private var whoMustPay: ArrayList<WhoHowmuch>
) :
    RecyclerView.Adapter<BillDetailsAdapter.ViewHolder>() {
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("UsersData")
    val getUserDetail = ArrayList<UserInfo>()
    var getMainUserName: String? = ""
    val nameInUserList = mutableListOf<String>()
    val mailAndPicHashMap = HashMap<String,Uri>()


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userPfp = view.findViewById<ImageView>(R.id.userPfp_billdetails)
        var whoWillPay = view.findViewById<TextView>(R.id.whoWillPay)
        var paymentStatus = view.findViewById<ImageView>(R.id.hadntPaid)
        var isHePaidTextView = view.findViewById<TextView>(R.id.isHePaid)
        var howMuchWillPay = view.findViewById<TextView>(R.id.howMuchHeWillPay)
        var showOrNot = view.findViewById<LinearLayout>(R.id.showOrNot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.bill_details_item, parent, false)
        MobileAds.initialize(context) {}
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = whoMustPay[position]
        println(whoMustPay.size)
        if (whoMustPay[position].whohasPaid == 2)
            holder.showOrNot.visibility = View.GONE

        holder.whoWillPay.text = item.whoWillPay

        for (name in whoMustPay) {
            nameInUserList.add(name.whoWillPay.toString())
        }

        setPaymentStatus(item, holder.paymentStatus, holder.isHePaidTextView,holder.howMuchWillPay)
        changePaymentStatus(holder.paymentStatus,holder.isHePaidTextView,position,item)

        getDetailsAndSetPfp(nameInUserList,holder.userPfp,item)

        goToProfileDetails(holder.userPfp, position)


    }

    private fun getDetailsAndSetPfp(
        nameInUserList: MutableList<String>,
        userPfp: ImageView,
        item: WhoHowmuch
    ) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(UserInfo::class.java)
                        if ("${temp?.name} ${temp?.surname}" in nameInUserList) {
                            getUserDetail.add(temp!!)
                        }
                        if (temp?.mail == Firebase.auth.currentUser?.email) {
                            getMainUserName = "${temp?.name} ${temp?.surname}"
                        }
                    }
                }
                for (user in getUserDetail) {
                    if (item.whoWillPay == "${user.name} ${user.surname}") {
                        firebaseStorage.getReference(user.mail.toString()).downloadUrl.addOnSuccessListener { uri ->
                            mailAndPicHashMap.put(user.mail.toString(),uri)
                            if (!(context as Activity).isFinishing)
                                Glide.with(context).load(uri.toString()).into(userPfp)
                        }.addOnFailureListener {
                            mailAndPicHashMap.put(user.mail.toString(),Uri.parse(R.drawable.splitwisecat.toString()))
                            if (!(context as Activity).isFinishing)
                                Glide.with(context).load(R.drawable.luffy).into(userPfp)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun changePaymentStatus(
        paymentStatus: ImageView,
        isHePaidTextView: TextView,
        position: Int,
        item: WhoHowmuch
    ) {
        paymentStatus.setOnClickListener {
            if (whoMustPay[position].whoWillPay != getMainUserName) {
                Toast.makeText(
                    context,
                    "Sadece kendi ödeme durumunuzu değiştirebilirsiniz",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                var databaseForBill = Firebase.database
                var refForBill = databaseForBill.getReference("Bills")
                refForBill.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                if (snap.exists()) {
                                    for (rsnap in snap.children) {
                                        var temp = rsnap.getValue(WhoHowmuch::class.java)
                                        var getRealRef = rsnap.ref
                                        if (temp?.whohasPaid == 0 && temp?.whoWillPay == getMainUserName && temp?.billname == item.billname) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                paymentStatus.setImageResource(R.drawable.checkedbg)
                                                isHePaidTextView.text = "Ödendi"

                                            }
                                            getRealRef.child("whohasPaid").setValue(1)
                                            whoMustPay[position].whohasPaid = 1
                                        } else if (temp?.whohasPaid == 1 && temp?.whoWillPay == getMainUserName && temp?.billname == item.billname) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                paymentStatus.setImageResource(R.drawable.uncheckedbg)
                                                isHePaidTextView.text = "Ödenmedi"

                                            }
                                            getRealRef.child("whohasPaid").setValue(0)
                                            whoMustPay[position].whohasPaid = 0
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
    }

    private fun setPaymentStatus(
        item: WhoHowmuch,
        paymentStatus: ImageView,
        isHePaidTextView: TextView,
        howMuchWillPay: TextView
    ) {
        if (item.whohasPaid == 0) {
            paymentStatus.setImageResource(R.drawable.uncheckedbg)
            isHePaidTextView.text = "Ödenmedi"
        } else {
            paymentStatus.setImageResource(R.drawable.checkedbg)
            isHePaidTextView.text = "Ödendi"
        }
        var realHowMuch = String.format("%.2f", item.howmuchWillpay)
        howMuchWillPay.text = "$realHowMuch₺"
    }

    private fun goToProfileDetails(userPfp: ImageView, position: Int) {
        var intentName = ""
        var intentSurname = ""
        var intentMail = ""
        userPfp.setOnClickListener {
            var ref = Firebase.database.getReference("UsersData")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            var temp = snap.getValue(UserInfo::class.java)
                            if ("${temp?.name} ${temp?.surname}" == whoMustPay[position].whoWillPay) {
                                intentName = temp?.name.toString()
                                intentSurname = temp?.surname.toString()
                                intentMail = temp?.mail.toString()
                            }
                        }
                    }
                    if (!intentName.isBlank() && !intentSurname.isBlank() && !intentMail.isBlank()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            var intent = Intent(context, ProfileDetailActivity::class.java)
                            intent.putExtra("name", intentName)
                            intent.putExtra("surname", intentSurname)
                            intent.putExtra("mail", intentMail)
                            intent.putExtra("mailAndPicHashMap",mailAndPicHashMap)
                            if(mailAndPicHashMap.isNullOrEmpty()){
                                Toast.makeText(context,"Bir hata ile karşılaşıldı, lütfen daha sonra tekrar deneyin",Toast.LENGTH_SHORT).show()
                            }else {
                                context.startActivity(intent)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return whoMustPay.size
    }
}

package com.yucox.splitwise.Adapter


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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.R.R.model.User
import com.R.R.model.BillInfo
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.yucox.splitwise.R
import com.yucox.splitwise.View.ShowProfileActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BillDetailsAdapter(
    private val context: Context,
    private val whoMustPay: ArrayList<BillInfo>
) :
    RecyclerView.Adapter<BillDetailsAdapter.ViewHolder>() {
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("UsersData")
    val userDetailsList = ArrayList<User>()
    var mainUserName: String? = ""
    val nameInUserList = mutableListOf<String>()
    val mailAndPicHashMap = HashMap<String, Uri>()


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userPfp = view.findViewById<ImageView>(R.id.userPfp_billdetails)
        var whoWillPay = view.findViewById<TextView>(R.id.whoWillPay)
        var paymentStatusImage = view.findViewById<ImageView>(R.id.hadntPaid)
        var paymentStatusTv = view.findViewById<TextView>(R.id.isHePaid)
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
        if (whoMustPay[position].whohasPaid == 2)
            holder.showOrNot.visibility = View.GONE

        holder.whoWillPay.text = item.whoWillPay

        for (name in whoMustPay) {
            nameInUserList.add(name.whoWillPay.toString())
        }

        CoroutineScope(Dispatchers.Main).launch {
            val result = fetchData(nameInUserList).await()
            if (!result)
                return@launch
            fetchAndSetPics(holder.userPfp, item.whoWillPay)
        }

        if (item.whohasPaid == 0) {
            holder.paymentStatusImage.setImageResource(R.drawable.uncheckedbg)
            holder.paymentStatusTv.text = "Ödenmedi"
        } else {
            holder.paymentStatusImage.setImageResource(R.drawable.checkedbg)
            holder.paymentStatusTv.text = "Ödendi"
        }
        var realHowMuch = String.format("%.2f", item.howmuchWillpay)
        holder.howMuchWillPay.text = "$realHowMuch₺"

        holder.paymentStatusImage.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = changePaymentStatus(item, position).await()
                when (result) {
                    -1 -> {
                        Toast.makeText(
                            context,
                            "Sadece kendi ödeme durumunuzu değiştirebilirsiniz",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    0 -> {
                        holder.paymentStatusImage.setImageResource(R.drawable.uncheckedbg)
                        holder.paymentStatusTv.text = "Ödenmedi"
                        whoMustPay[position].whohasPaid = 0
                    }

                    1 -> {
                        holder.paymentStatusImage.setImageResource(R.drawable.checkedbg)
                        holder.paymentStatusTv.text = "Ödendi"
                        whoMustPay[position].whohasPaid = 1
                    }

                    else -> Toast.makeText(context, R.string.try_later, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        holder.userPfp.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val user = fetchSelectedUserInfo(item).await()
                if (user.name!!.isBlank())
                    return@launch
                openProfile(user)
            }
        }
    }

    private fun fetchAndSetPics(userPfp: ImageView, whoWillPay: String?) {
        for (user in userDetailsList) {
            if (whoWillPay != "${user.name} ${user.surname}")
                continue

            val mail = user.mail.toString()
            firebaseStorage.getReference(mail).downloadUrl
                .addOnSuccessListener { uri ->
                    mailAndPicHashMap.put(user.mail.toString(), uri)
                    if ((context as Activity).isFinishing)
                        return@addOnSuccessListener
                    Glide.with(context).load(uri.toString()).into(userPfp)
                }
                .addOnFailureListener {
                    mailAndPicHashMap.put(
                        user.mail.toString(),
                        Uri.parse(R.drawable.splitwisecat.toString())
                    )
                    if ((context as Activity).isFinishing)
                        return@addOnFailureListener
                    Glide.with(context).load(R.drawable.luffy).into(userPfp)
                }
        }
    }

    private fun fetchData(
        nameInUserList: MutableList<String>,
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                    return

                for (snap in snapshot.children) {
                    val temp = snap.getValue(User::class.java)
                    if ("${temp?.name} ${temp?.surname}" in nameInUserList) {
                        userDetailsList.add(temp!!)
                    }
                    if (temp?.mail == Firebase.auth.currentUser?.email) {
                        mainUserName = "${temp?.name} ${temp?.surname}"
                    }
                }
                taskCompletionSource.setResult(true)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    private fun changePaymentStatus(item: BillInfo, position: Int): Task<Int> {
        val taskCompletionSource = TaskCompletionSource<Int>()
        val refForBill = database.getReference("Bills")

        refForBill.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (mainUserName != item.whoWillPay) {
                    taskCompletionSource.setResult(-1)
                    return
                }
                if (!snapshot.exists())
                    return
                for (snap in snapshot.children) {
                    if (!snap.exists())
                        continue

                    for (rsnap in snap.children) {
                        val temp = rsnap.getValue(BillInfo::class.java)
                        val snapKey = rsnap.ref
                        if (temp?.whohasPaid == 0 && temp?.whoWillPay == mainUserName && temp?.billname == item.billname) {
                            taskCompletionSource.setResult(1)
                            snapKey.child("whohasPaid").setValue(1)
                            break
                        } else if (temp?.whohasPaid == 1 && temp?.whoWillPay == mainUserName && temp?.billname == item.billname) {
                            taskCompletionSource.setResult(0)
                            snapKey.child("whohasPaid").setValue(0)
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(-2)
            }
        })
        return taskCompletionSource.task
    }

    private fun fetchSelectedUserInfo(item: BillInfo): Task<User> {
        val ref = database.getReference("UsersData")
        val user = User()
        val taskCompletionSource = TaskCompletionSource<User>()

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    return
                }
                for (snap in snapshot.children) {
                    val temp = snap.getValue(User::class.java)
                    if ("${temp?.name} ${temp?.surname}" == item.whoWillPay) {
                        user.name = temp?.name.toString()
                        user.surname = temp?.surname.toString()
                        user.mail = temp?.mail.toString()
                    }
                }
                if (!user.name!!.isBlank() && !user.surname!!.isBlank() && !user.mail!!.isBlank()) {
                    taskCompletionSource.setResult(user)
                } else
                    taskCompletionSource.setResult(null)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(null)
            }
        })
        return taskCompletionSource.task
    }

    private fun openProfile(user: User) {
        val intent = Intent(context, ShowProfileActivity::class.java)
        intent.putExtra("name", user.name)
        intent.putExtra("surname", user.surname)
        intent.putExtra("mail", user.mail)
        intent.putExtra("mailAndPicHashMap", mailAndPicHashMap)
        if (mailAndPicHashMap.isEmpty()) {
            Toast.makeText(
                context,
                R.string.try_later,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return whoMustPay.size
    }
}

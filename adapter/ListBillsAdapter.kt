package com.yucox.splitwise.adapter


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.R.R.model.WhoHowmuch
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.BillDetailsActivity
import com.yucox.splitwise.model.PhotoLocationBillName
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListBillsAdapter(
    private val context: Context,
    private var billNames: ArrayList<String>,
    var groupName: String? = "",
    var setPhotoWLocation: ArrayList<PhotoLocationBillName>,
    var snapKeyOfGroup : String? = ""

) :
    RecyclerView.Adapter<ListBillsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var smallPhotoOfBill = view.findViewById<CircleImageView>(R.id.smallPhotoOfBill)
        var billName = view.findViewById<TextView>(R.id.billNameForRecycler)
        var selectLinear = view.findViewById<LinearLayout>(R.id.selectLinear)
        var price = view.findViewById<TextView>(R.id.priceOfBill)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listbills_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = billNames[position]
        holder.billName.text = item

        var priceAndBillname = ArrayList<WhoHowmuch>()
        var billsRef = Firebase.database.getReference("Bills")

        for (a in setPhotoWLocation) {
            if (a.billName == item) {
                println(a.photo + " " + a.billName)
                if (a.photo?.isBlank() == true || a.photo == null) {
                    holder.smallPhotoOfBill.setImageResource(R.drawable.nouploadedphoto)
                } else {
                    Glide.with(context).load(a.photo).into(holder.smallPhotoOfBill)
                }
            }
        }
        billsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (resnap in snap.children) {
                                var temp = resnap.child("billname").getValue(String::class.java)
                                if (temp in billNames){
                                    var getTemp = resnap.getValue(WhoHowmuch::class.java)
                                    if(getTemp?.snapKeyOfGroup == snapKeyOfGroup){
                                        priceAndBillname.add(getTemp!!)
                                    }
                                }
                            }
                        }
                    }
                }
                if (billNames.isNotEmpty() && priceAndBillname.isNotEmpty() && billNames.size > position) {
                    CoroutineScope(Dispatchers.Main).launch {
                        for (a in priceAndBillname) {
                            if (holder.billName.text == a.billname && a.whohasPaid != 2 && a.snapKeyOfGroup == snapKeyOfGroup){
                                holder.price.text = "${a.totalPrice}₺"
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        holder.selectLinear.setOnClickListener {
            val intent = Intent(context, BillDetailsActivity::class.java)
            var temp: String? = null
            var temp2: String? = null
            for (a in setPhotoWLocation) {
                if (item == a.billName) {
                    temp = a.photo
                    temp2 = a.photoLocation
                }
            }
            intent.putExtra("photoLocation", temp2)
            intent.putExtra("billName", billNames[position])
            intent.putExtra("groupName", groupName)
            intent.putExtra("billImgUri", temp)
            intent.putExtra("snapKeyOfGroup",snapKeyOfGroup)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return billNames.size
    }
}



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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.R.R.model.WhoHowmuch
import com.google.firebase.storage.FirebaseStorage
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.BillDetailsActivity
import com.yucox.splitwise.model.PhotoLocationBillName
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class ListBillsAdapter(
    private val context: Context,
    var photoLocationHash: HashMap<String, String>,
    val billsArray: ArrayList<WhoHowmuch>

) :
    RecyclerView.Adapter<ListBillsAdapter.ViewHolder>() {
    private val storage = FirebaseStorage.getInstance()
    private val photoAndBillHashMap = HashMap<String, String>()


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val smallPhotoOfBill = view.findViewById<CircleImageView>(R.id.smallPhotoOfBill)
        val billName = view.findViewById<TextView>(R.id.billNameForRecycler)
        val selectLinear = view.findViewById<LinearLayout>(R.id.selectLinear)
        val price = view.findViewById<TextView>(R.id.priceOfBill)
        val createTime = view.findViewById<TextView>(R.id.createTime)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listbills_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        billsArray.sortByDescending { it.createTime }
        var item = billsArray[position]
        holder.billName.text = item.billname
        holder.price.text = "₺" + item.totalPrice.toString()

        if (item.createTime != null) {
            val sdf = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            var formatedTime = sdf.format(item.createTime)
            holder.createTime.text = formatedTime
        }

        if (item.billname != null) {
            goToDetailOfTheBill(holder.selectLinear, item, position)
        }

        getPhotos(item, holder.smallPhotoOfBill)

    }

    private fun getPhotos(item: WhoHowmuch, smallPhotoOfBill: CircleImageView) {
        storage.getReference(photoLocationHash[item.billname].toString()).downloadUrl
            .addOnSuccessListener { uri ->
                if(!(context as Activity).isFinishing)
                    Glide.with(context).load(uri).into(smallPhotoOfBill)
                photoAndBillHashMap.put(item.billname.toString(),uri.toString())
            }
            .addOnFailureListener {
                if(!(context as Activity).isFinishing)
                    Glide.with(context).load(R.drawable.nouploadedphoto).into(smallPhotoOfBill)
            }
    }

    private fun goToDetailOfTheBill(selectLinear: LinearLayout, item: WhoHowmuch, position: Int) {
        selectLinear.setOnClickListener {
            val intent = Intent(context, BillDetailsActivity::class.java)
            /*var temp: String? = null
            for (a in setPhotoWLocation) {
                if (item.billname == a.billName) {
                    temp = a.photo
                }
            }*/
            if (item.billname.isNullOrEmpty())
                return@setOnClickListener

            intent.putExtra("photoLocation", photoLocationHash[item.billname])
            intent.putExtra("billName", item.billname)
            intent.putExtra("groupName", item.groupName)
            intent.putExtra("billImgUri", photoAndBillHashMap[item.billname.toString()])
            intent.putExtra("snapKeyOfGroup", item.snapKeyOfGroup)
            context.startActivity(intent)
        }
    }

    /*private fun setPhotos(smallPhotoOfBill: CircleImageView, item: String) {
        for (a in setPhotoWLocation) {
            if (a.billName == item) {
                if (a.photo?.isBlank() == true || a.photo == null) {
                    smallPhotoOfBill.setImageResource(R.drawable.nouploadedphoto)
                } else {
                    Glide.with(context).load(a.photo).into(smallPhotoOfBill)
                }
            }
        }
    }*/

    /*private fun getBillsFromData(
        billName: TextView,
        price: TextView,
        position: Int,
        createTime: TextView
    ) {
        billsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (resnap in snap.children) {
                                var temp = resnap.child("billname").getValue(String::class.java)
                                if (temp in billNames) {
                                    var getTemp = resnap.getValue(WhoHowmuch::class.java)
                                    if (getTemp?.snapKeyOfGroup == snapKeyOfGroup) {
                                        priceAndBillname.add(getTemp!!)
                                    }
                                }
                            }
                        }
                    }
                }
                val sdf = SimpleDateFormat("dd.MM.yy")
                if (billNames.isNotEmpty() && priceAndBillname.isNotEmpty() && billNames.size > position) {
                    CoroutineScope(Dispatchers.Main).launch {
                        for (a in priceAndBillname) {
                            if (billName.text == a.billname && a.whohasPaid != 2 && a.snapKeyOfGroup == snapKeyOfGroup) {
                                if (price.text != a.totalPrice.toString()) {
                                    price.text = "${a.totalPrice}₺"
                                }
                                if (a.createTime != null) {
                                    var formatedTime = sdf.format(a.createTime)
                                    if(createTime.text != formatedTime){
                                        createTime.text = formatedTime
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }*/


    override fun getItemCount(): Int {
        return billsArray.size
    }
}



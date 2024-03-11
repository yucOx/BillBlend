package com.yucox.splitwise.Adapter


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.R.R.model.BillInfo
import com.google.firebase.storage.FirebaseStorage
import com.yucox.splitwise.R
import com.yucox.splitwise.View.BillDetailsActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class ListBillsAdapter(
    private val context: Context,
    private val photoLocationHash: HashMap<String, String>,
    private val billsArray: ArrayList<BillInfo>

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
        val item = billsArray[position]
        holder.billName.text = item.billname
        holder.price.text = "₺" + item.totalPrice.toString()

        if (item.createTime != null) {
            val sdf = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            val formatedTime = sdf.format(item.createTime)
            holder.createTime.text = formatedTime
        }

        if (item.billname != null) {
            holder.selectLinear.setOnClickListener {
                goToDetailOfTheBill(item)
            }
        }

        fetchPhotosAndSet(item, holder.smallPhotoOfBill)
    }

    private fun fetchPhotosAndSet(item: BillInfo, smallPhotoOfBill: CircleImageView) {
        storage.getReference(photoLocationHash[item.billname].toString()).downloadUrl
            .addOnSuccessListener { uri ->
                if (!(context as Activity).isFinishing)
                    Glide.with(context).load(uri).into(smallPhotoOfBill)
                photoAndBillHashMap.put(item.billname.toString(), uri.toString())
            }
            .addOnFailureListener {
                if (!(context as Activity).isFinishing)
                    Glide.with(context).load(R.drawable.nouploadedphoto).into(smallPhotoOfBill)
            }
    }

    private fun goToDetailOfTheBill(item: BillInfo) {
        val intent = Intent(context, BillDetailsActivity::class.java)
        if (item.billname.isNullOrEmpty())
            return

        intent.putExtra("photoLocation", photoLocationHash[item.billname])
        intent.putExtra("billName", item.billname)
        intent.putExtra("groupName", item.groupName)
        intent.putExtra("billImgUri", photoAndBillHashMap[item.billname.toString()])
        intent.putExtra("snapKeyOfGroup", item.snapKeyOfGroup)
        context.startActivity(intent)
    }


    override fun getItemCount(): Int {
        return billsArray.size
    }
}



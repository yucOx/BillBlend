package com.yucox.splitwise.activity



import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.WhoHowmuch
import com.google.firebase.database.DatabaseReference
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.BillDetailsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillDetailsActivity : AppCompatActivity() {
    private lateinit var groupName : String
    private lateinit var adapter : BillDetailsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bill_details_activity)

        var database = FirebaseDatabase.getInstance()
        var ref = database.getReference("Bills")
        var storage = Firebase.storage

        var billName = intent.getStringExtra("billName")
        var billImg = intent.getStringExtra("billImgUri")
        var photoLocation = intent.getStringExtra("photoLocation")
        groupName = intent.getStringExtra("groupName").toString()
        var snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")
        var billNameText = findViewById<TextView>(R.id.nameOfBill)
        var threedotOptions = findViewById<ConstraintLayout>(R.id.threedot_options_detail_layout)
        var deleteToBillBtn = findViewById<ImageView>(R.id.deleteGroup)
        var photoOftheBill = findViewById<ImageView>(R.id.photoOfBill)
        var threeDotBtn = findViewById<ImageView>(R.id.three_dot_btn)
        var uploadBillPhotoBtn = findViewById<ImageView>(R.id.add_photo_tobill_btn)
        var progressBar = findViewById<ProgressBar>(R.id.progressBar3)
        progressBar.visibility = View.GONE
        billNameText.text = billName
        threedotOptions.visibility = View.GONE

        if (billImg?.isBlank() == false) {
            Glide.with(this@BillDetailsActivity).load(Uri.parse(billImg)).into(photoOftheBill)
        }else{
            photoOftheBill.setBackgroundResource(R.drawable.nouploadedphoto)
        }

        var getBillDetailsArray = ArrayList<WhoHowmuch>()
        getBillDetailsFromData(ref,getBillDetailsArray,billName,snapKeyOfGroup)

        deleteToBillBtn.setOnClickListener {
            deleteToBillFun(getBillDetailsArray,photoLocation,snapKeyOfGroup,adapter)
        }

        threeDotBtn.setOnClickListener{
            threeDotSettings()
        }

        var galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    if(photoLocation.isNullOrEmpty()){
                        progressBar.visibility = View.VISIBLE
                        for(a in getBillDetailsArray){
                            if(a.whohasPaid == 2){
                                storage.getReference(billName.toString()).child(a.photoLocation.toString())
                                    .putFile(data?.data!!)
                                    .addOnSuccessListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Glide.with(this@BillDetailsActivity).load(data.data).into(photoOftheBill)
                                            progressBar.visibility = View.GONE
                                        }
                                    }
                                    .addOnFailureListener{
                                        CoroutineScope(Dispatchers.Main).launch {
                                            progressBar.visibility = View.GONE
                                        }
                                    }
                            }
                        }
                    }else {
                        progressBar.visibility = View.VISIBLE
                        storage.getReference(photoLocation)
                            .putFile(data?.data!!)
                            .addOnSuccessListener {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Glide.with(this@BillDetailsActivity).load(data.data).into(photoOftheBill)
                                    progressBar.visibility = View.GONE
                                }
                            }
                            .addOnFailureListener{
                                CoroutineScope(Dispatchers.Main).launch {
                                    progressBar.visibility = View.GONE
                                }
                            }
                    }

                } else {
                    Toast.makeText(
                        this@BillDetailsActivity,
                        "Hiçbir resim seçilmedi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        uploadBillPhotoBtn.setOnClickListener {
            uploadBillPhotoBtn.setOnClickListener {
            if(getBillDetailsArray[0].whoBought == Firebase.auth.currentUser?.email){
                uploadPhoto(galleryLauncher)
            } else{
                Toast.makeText(this@BillDetailsActivity,"Sadece fatura sahibi fotoğraf yükleyebilir veya değiştirebilir.",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initAdapter(getBillDetails: ArrayList<WhoHowmuch>) {
        adapter = BillDetailsAdapter(this@BillDetailsActivity, getBillDetails)
        var recyclerView = findViewById<RecyclerView>(R.id.recycler_billdetails)
        recyclerView.layoutManager =
            LinearLayoutManager(this@BillDetailsActivity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    private fun getBillDetailsFromData(
        ref: DatabaseReference,
        getBillDetails: ArrayList<WhoHowmuch>,
        billName: String?,
        snapKeyOfGroup: String?
    ) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (rSnap in snap.children) {
                                var temp = rSnap.getValue(WhoHowmuch::class.java)
                                if (temp?.billname == billName && temp?.groupName == groupName && temp.snapKeyOfGroup == snapKeyOfGroup)
                                    getBillDetails.add(temp!!)
                            }
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    initAdapter(getBillDetails)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun deleteToBillFun(
        getBillDetails: ArrayList<WhoHowmuch>,
        photoLocation: String?,
        snapKeyOfGroup: String?,
        adapter: BillDetailsAdapter
    ) {
        if (Firebase.auth.currentUser?.email == getBillDetails[0].whoBought) {
            var builder = AlertDialog.Builder(this@BillDetailsActivity)
            builder.setTitle("Faturayı silmek istediğinizden emin misiniz?")
            builder.setNegativeButton("Evet") { dialog, which ->
                var dataRef = Firebase.database.getReference("Bills")
                dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                for (rsnap in snap.children) {
                                    var temp = rsnap.getValue(WhoHowmuch::class.java)
                                    if (temp?.billname == getBillDetails[0].billname && temp?.groupName == groupName && temp.snapKeyOfGroup == snapKeyOfGroup) {
                                        var uniqueId = snap.key.toString()
                                        dataRef.child(uniqueId).removeValue()
                                            .addOnSuccessListener {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    Toast.makeText(
                                                        this@BillDetailsActivity,
                                                        "Fatura başarıyla silindi!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        for (a in getBillDetails) {
                                            if(!photoLocation.isNullOrEmpty()){
                                                var storage = Firebase.storage.getReference(photoLocation.toString())
                                                storage.delete().addOnSuccessListener {
                                                    if (!isFinishing()) {
                                                        finish()
                                                    }
                                                }.addOnFailureListener {
                                                    if (!isFinishing) {
                                                        finish()
                                                    }
                                                }
                                            }else{
                                                if (!isFinishing) {
                                                    finish()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        getBillDetails.clear()
                        CoroutineScope(Dispatchers.Main).launch {
                            this@BillDetailsActivity.adapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }.setPositiveButton("Hayır") { dialog, which ->

            }.show()
        } else {
            Toast.makeText(
                this@BillDetailsActivity,
                "Sadece fatura sahibi, faturayı silebilir.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun uploadPhoto(galleryLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun threeDotSettings() {
        var threedotOptions = findViewById<ConstraintLayout>(R.id.threedot_options_detail_layout)
        if(threedotOptions.visibility == View.GONE){
            threedotOptions.visibility = View.VISIBLE
        }else{
            threedotOptions.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@BillDetailsActivity,DetailsOfGroupActivity::class.java)
        intent.putExtra("GroupName",groupName)
        finish()
    }
}



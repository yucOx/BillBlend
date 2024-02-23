package com.yucox.splitwise.activity


import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.BillDetailsAdapter
import com.yucox.splitwise.databinding.BillDetailsActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillDetailsActivity : AppCompatActivity() {
    private lateinit var groupName: String
    private lateinit var adapter: BillDetailsAdapter
    private lateinit var binding: BillDetailsActivityBinding
    private var getBillDetailsArray = ArrayList<WhoHowmuch>()

    private var billImg: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BillDetailsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MobileAds.initialize(this) {}

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("Bills")
        val storage = Firebase.storage

        var billName = intent.getStringExtra("billName")
        billImg = intent.getStringExtra("billImgUri")
        println(billImg)
        var photoLocation = intent.getStringExtra("photoLocation")
        groupName = intent.getStringExtra("groupName").toString()
        var snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")
        binding.billNameTv.text = billName

        var galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    billImg = data?.data.toString()
                    if (!photoLocation.isNullOrEmpty()) {
                        binding.progressBar3.visibility = View.VISIBLE

                        storage.getReference(photoLocation.toString())
                            .putFile(data?.data!!)
                            .addOnSuccessListener {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Glide.with(this@BillDetailsActivity)
                                        .load(data.data)
                                        .centerCrop()
                                        .into(binding.photoOfBill)
                                    binding.progressBar3.visibility = View.GONE
                                }
                            }
                            .addOnFailureListener {
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.progressBar3.visibility = View.GONE
                                }
                            }
                    }
                }
            }

        goToBack()

        setBillPhoto()

        getBillDetailsFromData(ref, getBillDetailsArray, billName, snapKeyOfGroup, photoLocation)


        threeDotSettings()

        zoomOn()

        setInfo()

        binding.uploadPhotoBtn.setOnClickListener {
            if (getBillDetailsArray.size == 0) {
                uploadPhoto(galleryLauncher)
            } else {
                if (getBillDetailsArray[0].whoBought == Firebase.auth.currentUser?.email) {
                    uploadPhoto(galleryLauncher)
                } else {
                    Toast.makeText(
                        this@BillDetailsActivity,
                        "Sadece fatura sahibi fotoğraf yükleyebilir veya değiştirebilir.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setInfo() {
        val rootView: View = findViewById(android.R.id.content)
        Snackbar.make(rootView, "Resmi büyütmek için üzerine tıklayın", Snackbar.LENGTH_LONG).show()
    }

    private fun zoomOn() {
        binding.photoOfBill.setOnClickListener {
            val intent = Intent(this@BillDetailsActivity, BigScreenView::class.java)
            intent.putExtra("img", billImg)
            startActivity(intent)
        }
    }

    private fun goToBack() {
        binding.backBtn.setOnClickListener {
            val intent = Intent(this@BillDetailsActivity, DetailsOfGroupActivity::class.java)
            intent.putExtra("GroupName", groupName)
            finish()
        }
    }

    private fun setBillPhoto() {
        if (billImg?.isBlank() == false) {
            println("foto bos degil")
            Glide.with(this@BillDetailsActivity).load(Uri.parse(billImg)).centerCrop()
                .into(binding.photoOfBill)
        } else {
            Glide.with(this@BillDetailsActivity).load(R.drawable.nouploadedphoto).centerCrop()
                .into(binding.photoOfBill)
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
        snapKeyOfGroup: String?,
        photoLocation: String?
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
                    deleteToBill(getBillDetailsArray, photoLocation, snapKeyOfGroup, adapter)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun deleteToBill(
        getBillDetails: ArrayList<WhoHowmuch>,
        photoLocation: String?,
        snapKeyOfGroup: String?,
        adapter: BillDetailsAdapter
    ) {
        binding.deleteToBillBtn.setOnClickListener {

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
                                            if (!photoLocation.isNullOrEmpty()) {
                                                var storage =
                                                    Firebase.storage.getReference(photoLocation.toString())
                                                storage.delete().addOnSuccessListener {
                                                    if (!isFinishing()) {
                                                        finish()
                                                    }
                                                }.addOnFailureListener {
                                                    if (!isFinishing) {
                                                        finish()
                                                    }
                                                }
                                            } else {
                                                if (!isFinishing) {
                                                    finish()
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
    }

    private fun uploadPhoto(galleryLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun threeDotSettings() {
        binding.threeDotBtn.setOnClickListener {
            if (binding.threedotOptionsLayout.visibility == View.GONE) {
                binding.threedotOptionsLayout.visibility = View.VISIBLE
                binding.billNameTv.visibility = View.GONE
                println("burada")
            } else {
                binding.threedotOptionsLayout.visibility = View.GONE
                binding.billNameTv.visibility = View.VISIBLE
                println("burada")

            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@BillDetailsActivity, DetailsOfGroupActivity::class.java)
        intent.putExtra("GroupName", groupName)
        finish()
    }
}







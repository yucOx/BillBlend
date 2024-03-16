package com.yucox.splitwise.View


import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.yucox.splitwise.R
import com.yucox.splitwise.ViewModel.BillViewModel
import com.yucox.splitwise.Adapter.BillDetailsAdapter
import com.yucox.splitwise.databinding.BillDetailsActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BillDetailsActivity : AppCompatActivity() {
    private lateinit var groupName: String
    private lateinit var binding: BillDetailsActivityBinding
    private lateinit var billViewModel: BillViewModel

    private var billImg: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BillDetailsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MobileAds.initialize(this) {}

        val billName = intent.getStringExtra("billName")
        billImg = intent.getStringExtra("billImgUri")
        val photoLocation = intent.getStringExtra("photoLocation")
        groupName = intent.getStringExtra("groupName").toString()
        val snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")
        binding.billNameTv.text = billName

        billViewModel = ViewModelProvider(this).get(BillViewModel::class.java)

        fun initData() {
            if (billName?.isBlank() == true)
                return
            if (groupName?.isBlank() == true)
                return
            if (snapKeyOfGroup?.isBlank() == true)
                return
            if (photoLocation?.isBlank() == true)
                return
            billViewModel.setIntentData(billName!!, groupName, snapKeyOfGroup!!, photoLocation!!)
        }
        initData()

        CoroutineScope(Dispatchers.Main).launch {
            if (!billViewModel.fetchBillDetails().await())
                return@launch
            initAdapter()
        }

        val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    billImg = data?.data.toString()
                    if (photoLocation.isNullOrEmpty())
                        return@registerForActivityResult

                    binding.progressBar3.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        if (!billViewModel.uploadPhoto(data?.data!!).await()) {
                            binding.progressBar3.visibility = View.GONE
                            return@launch
                        }
                        Glide.with(this@BillDetailsActivity)
                            .load(data.data)
                            .centerCrop()
                            .into(binding.photoOfBill)
                        binding.progressBar3.visibility = View.GONE
                    }
                }
            }

        setBillPhoto()

        binding.deleteToBillBtn.setOnClickListener {
            if (Firebase.auth.currentUser?.email != billViewModel.getBillDetails()[0].whoBought) {
                Toast.makeText(
                    this@BillDetailsActivity,
                    "Sadece fatura sahibi, faturayı silebilir.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this@BillDetailsActivity)
            builder.setTitle(getString(R.string.sure_to_delete))
                .setNegativeButton("Evet") { dialog, which ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if (billViewModel.deleteToBill().await()) {
                            Toast.makeText(
                                this@BillDetailsActivity,
                                getString(R.string.bill_deleted),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
                .setPositiveButton("Hayır") { dialog, which -> }
                .show()

        }

        binding.threeDotBtn.setOnClickListener {
            threeDotSettings()
        }

        binding.photoOfBill.setOnClickListener {
            zoomOn()
        }
        setInfo()

        binding.backBtn.setOnClickListener {
            val intent = Intent(this@BillDetailsActivity, DetailsOfGroupActivity::class.java)
            intent.putExtra("GroupName", groupName)
            finish()
        }

        binding.uploadPhotoBtn.setOnClickListener {
            val billDetails = billViewModel.getBillDetails()
            if (billDetails.size == 0) {
                uploadPhoto(galleryLauncher)
            } else {
                if (billDetails[0].whoBought == Firebase.auth.currentUser?.email) {
                    uploadPhoto(galleryLauncher)
                } else {
                    Toast.makeText(
                        this@BillDetailsActivity,
                        getString(R.string.only_owner_can),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setInfo() {
        val rootView: View = findViewById(android.R.id.content)
        Snackbar.make(rootView, getString(R.string.zoom_info), Snackbar.LENGTH_LONG).show()
    }

    private fun zoomOn() {
        val intent = Intent(this@BillDetailsActivity, BigScreenView::class.java)
        intent.putExtra("img", billImg)
        startActivity(intent)

    }


    private fun setBillPhoto() {
        if (billImg?.isBlank() == false) {
            Glide.with(this@BillDetailsActivity).load(Uri.parse(billImg)).centerCrop()
                .into(binding.photoOfBill)
        } else {
            Glide.with(this@BillDetailsActivity).load(R.drawable.nouploadedphoto).centerCrop()
                .into(binding.photoOfBill)
        }
    }

    private fun initAdapter() {
        val adapter: BillDetailsAdapter
        val billDetails = billViewModel.getBillDetails()

        adapter = BillDetailsAdapter(this@BillDetailsActivity, billDetails)
        binding.recyclerBilldetails.layoutManager =
            LinearLayoutManager(this@BillDetailsActivity, RecyclerView.VERTICAL, false)
        binding.recyclerBilldetails.adapter = adapter
    }

    private fun uploadPhoto(galleryLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun threeDotSettings() {
        if (binding.threedotOptionsLayout.visibility == View.GONE) {
            binding.threedotOptionsLayout.visibility = View.VISIBLE
            binding.billNameTv.visibility = View.GONE
        } else {
            binding.threedotOptionsLayout.visibility = View.GONE
            binding.billNameTv.visibility = View.VISIBLE

        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@BillDetailsActivity, DetailsOfGroupActivity::class.java)
        intent.putExtra("GroupName", groupName)
        finish()
    }
}





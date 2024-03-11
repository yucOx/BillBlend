package com.yucox.splitwise.View


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yucox.splitwise.R
import com.yucox.splitwise.ViewModel.DetailsOfGroupViewModel
import com.yucox.splitwise.Adapter.ListBillsAdapter
import com.yucox.splitwise.Adapter.ListUserAdapter
import com.yucox.splitwise.databinding.DetailsofgroupActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailsOfGroupActivity : AppCompatActivity() {

    private lateinit var listBillsAdapter: ListBillsAdapter
    private lateinit var binding: DetailsofgroupActivityBinding
    private lateinit var mAdView: AdView
    private lateinit var listUserAdapter: ListUserAdapter
    private lateinit var detailsOfGroupViewModel: DetailsOfGroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsofgroupActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        initBanner()

        val groupName = intent.getStringExtra("GroupName")
        val snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")
        binding.groupNameTv.text = groupName

        detailsOfGroupViewModel = ViewModelProvider(this).get(DetailsOfGroupViewModel::class.java)

        detailsOfGroupViewModel.setGroupName(groupName.toString())
        detailsOfGroupViewModel.setSnapKey(snapKeyOfGroup.toString())

        CoroutineScope(Dispatchers.Main).launch {
            binding.refreshMe.isRefreshing = true
            val rootView = findViewById<View>(android.R.id.content)
            if (!detailsOfGroupViewModel.fetchGroupMembers().await()) {
                Snackbar.make(
                    rootView, getString(R.string.try_again_later), Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Tamam") {}
                    .show()
                return@launch
            }
            if (!detailsOfGroupViewModel.fetchMembersDetail().await()) {
                Snackbar.make(
                    rootView, getString(R.string.try_again_later), Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Tamam") {}
                    .show()
                binding.refreshMe.isRefreshing = false
                return@launch
            }
            if (!detailsOfGroupViewModel.fetchBills().await()) {
                Snackbar.make(rootView, "Mevcut fatura bulunamadı", Snackbar.LENGTH_LONG).show()
                binding.showBillNamesRecycler.removeAllViews()
                binding.refreshMe.isRefreshing = false
                initListUserRecycler()
                return@launch
            }
            binding.refreshMe.isRefreshing = false
            initListUserRecycler()
            initListBillsRecycler()
        }

        binding.deleteToGroup.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@DetailsOfGroupActivity)
            builder.setTitle(getString(R.string.sure_to_delete_group))
                .setNegativeButton("Evet") { _, _ ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if (!detailsOfGroupViewModel.checkGroupOwner()) {
                            Toast.makeText(
                                this@DetailsOfGroupActivity,
                                "Sadece Grup Kurucusu Grubu Silebilir.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }
                        if (!detailsOfGroupViewModel.deleteToGroup().await()) {
                            Toast.makeText(
                                this@DetailsOfGroupActivity,
                                getString(R.string.try_delete_later),
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }
                        if (detailsOfGroupViewModel.divideAndConquer().await())
                            finish()
                    }

                }.setPositiveButton("İptal") { _, _ -> }
                .show()
        }

        binding.leftFromGroupBtn.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.sure_to_left))
                .setNegativeButton("Evet") { _, _ ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if (!detailsOfGroupViewModel.leaveFromGroup().await()) {
                            Toast.makeText(
                                this@DetailsOfGroupActivity,
                                getString(R.string.try_later), Toast.LENGTH_LONG
                            )
                                .show()
                            return@launch
                        }
                        val intent = Intent(
                            this@DetailsOfGroupActivity,
                            MainActivity::class.java
                        )
                        startActivity(intent)
                        finish()
                    }
                }
                .setPositiveButton("İptal") { _, _ ->
                }
                .show()
        }

        binding.addBillBtn.setOnClickListener {
            if (detailsOfGroupViewModel.getGroupName()?.isNotBlank() == true) {
                val intent = Intent(this, AddBillActivity::class.java)
                intent.putExtra("groupName", detailsOfGroupViewModel.getGroupName())
                intent.putExtra("snapKeyOfGroup", detailsOfGroupViewModel.getSnapKey())
                startActivity(intent)
            }
        }

        binding.refreshMe.setOnRefreshListener {
            updateBills()
        }

        binding.backToLoginPage2.setOnClickListener {
            finish()
        }

        binding.threeDotBtn.setOnClickListener {
            showThreeDotOptions()
        }
    }

    private fun updateBills() {
        binding.refreshMe.isRefreshing = true

        CoroutineScope(Dispatchers.Main).launch {
            if (!detailsOfGroupViewModel.resetValues()) {
                return@launch
            }
            if (!detailsOfGroupViewModel.fetchBills().await()) {
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(rootView, "Mevcut fatura bulunamadı", Snackbar.LENGTH_LONG).show()
                binding.showBillNamesRecycler.removeAllViews()
                binding.refreshMe.isRefreshing = false
                return@launch
            }
            binding.refreshMe.isRefreshing = false
            initListBillsRecycler()
        }
    }

    private fun initListBillsRecycler() {
        val photoLocationHashMap = detailsOfGroupViewModel.getPhotoLocationHashMap()
        val billsArray = detailsOfGroupViewModel.getBills()
        listBillsAdapter = ListBillsAdapter(
            this,
            photoLocationHashMap,
            billsArray
        )
        binding.showBillNamesRecycler.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.showBillNamesRecycler.adapter = listBillsAdapter
    }


    private fun initBanner() {
        mAdView = findViewById(R.id.adView3)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun initListUserRecycler() {
        val groupMembers = detailsOfGroupViewModel.getGroupMembers()
        val groupMembersDetail = detailsOfGroupViewModel.getGroupMembersDetail()
        val randomPfp = mutableListOf<Int>()
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)

        listUserAdapter = ListUserAdapter(
            this@DetailsOfGroupActivity,
            groupMembersDetail,
            groupMembers,
            randomPfp
        )
        var recyclerView = findViewById<RecyclerView>(R.id.usersOfGroupRecycler)
        recyclerView.layoutManager =
            LinearLayoutManager(this@DetailsOfGroupActivity, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = listUserAdapter
    }

    private fun showThreeDotOptions() {
        var threeDotOptionsDetail =
            findViewById<ConstraintLayout>(R.id.threedot_options_detail_layout2)
        if (threeDotOptionsDetail.visibility == View.GONE) {
            threeDotOptionsDetail.visibility = View.VISIBLE
            binding.groupNameTv.visibility = View.GONE
        } else {
            threeDotOptionsDetail.visibility = View.GONE
            binding.groupNameTv.visibility = View.VISIBLE
        }
    }

    override fun onRestart() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            updateBills()
        }
        super.onRestart()
    }

    override fun onBackPressed() {
        finish()
    }
}





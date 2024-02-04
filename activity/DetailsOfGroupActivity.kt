package com.yucox.splitwise.activity


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.Group
import com.R.R.model.UserInfo
import com.R.R.model.WhoHowmuch
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.ListBillsAdapter
import com.yucox.splitwise.adapter.ListUserAdapter
import com.yucox.splitwise.databinding.DetailsofgroupActivityBinding
import com.yucox.splitwise.model.PhotoLocationBillName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailsOfGroupActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private var database = FirebaseDatabase.getInstance()
    private lateinit var listBillsAdapter: ListBillsAdapter
    var billNames = ArrayList<String>()
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    var getPhotoLocation: String? = ""
    private val refGroup = database.getReference("Groups")
    private val refPerson = database.getReference("UsersData")
    private val refForBills = database.getReference("Bills")
    private lateinit var binding: DetailsofgroupActivityBinding
    private var groupName: String? = null
    private var groupUsers = ArrayList<UserInfo>()
    private var group = ArrayList<Group>()
    lateinit var mAdView: AdView
    private lateinit var listUserAdapter: ListUserAdapter
    private var billNamesHash = hashSetOf<String>()
    private var getPhotosWLocation = ArrayList<PhotoLocationBillName>()
    private var snapKeyOfGroup: String? = null
    private var photoLocationHashMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailsofgroupActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        initBanner()

        groupName = intent.getStringExtra("GroupName")
        snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")
        binding.groupNameTv.text = groupName
        binding.threedotOptionsDetailLayout2.visibility = View.GONE

        getGroupUsersFromData()

        leaveFromGroup()
        deleteToGroup()

        addBill()

        backBtn()

        getBillsFromData()

        refresh()

        showThreeDotOptions()

    }

    private fun refresh() {
        binding.refreshMe.setOnRefreshListener {
            updateData()
        }
    }

    private fun deleteToGroup() {
        binding.deleteToGroup.setOnClickListener {
            var refForDel = database.getReference("Groups")
            if (group[0].groupOwner == Firebase.auth.currentUser?.email) {
                var builder = MaterialAlertDialogBuilder(this@DetailsOfGroupActivity)
                builder.setTitle("Grubu silmek istediğinze emin misiniz?")
                    .setNegativeButton("Evet, son kararım!") { dialog, which ->
                        refForDel.child(snapKeyOfGroup.toString()).removeValue()
                            .addOnSuccessListener {
                                deletePhotoOfBills(snapKeyOfGroup)
                            }
                    }.setPositiveButton("Hayır, sadece elim çarptı..") { dialog, which ->
                    }.show()
            } else {
                Toast.makeText(
                    this@DetailsOfGroupActivity,
                    "Sadece Grup Kurucusu Grubu Silebilir.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
        }
    }

    private fun updateData() {
        binding.refreshMe.isRefreshing = true
        billNames.clear()
        getPhotosWLocation.clear()
        photoLocationHashMap.clear()
        billNamesHash.clear()

        refForBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(WhoHowmuch::class.java)
                                if (temp?.groupName == groupName && temp?.snapKeyOfGroup == snapKeyOfGroup) {
                                    billNamesHash.add(temp?.billname.toString())
                                    if (temp?.whohasPaid == 2) {
                                        photoLocationHashMap.put(
                                            temp?.billname.toString(),
                                            temp?.photoLocation.toString()
                                        )
                                    }
                                    for (a in billNamesHash) {
                                        if (a in billNames) {
                                            continue
                                        } else if (!(a in billNames) && !a.isBlank()) {
                                            billNames.add(a)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (billNames.size == 0) {
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "Mevcut fatura bulunamadı", Snackbar.LENGTH_LONG).show()
                    binding.refreshMe.isRefreshing = false
                    binding.showBillNamesRecycler.removeAllViews()
                }

                var i = 0
                for (a in photoLocationHashMap) {
                    Firebase.storage.getReference(a.value)
                        .downloadUrl.addOnSuccessListener { uri ->
                            i++
                            getPhotoLocation = uri.lastPathSegment
        
                            getPhotosWLocation.add(
                                PhotoLocationBillName(
                                    a.key,
                                    a.value,
                                    uri.toString()
                                )
                            )
                            if (i == photoLocationHashMap.size) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (::listBillsAdapter.isInitialized) {
                                        listBillsAdapter.notifyDataSetChanged()
                                        binding.refreshMe.isRefreshing = false
                                    } else {
                                        getBillsFromData()
                                    }
                                }
                            }
                        }.addOnFailureListener {
                            getPhotosWLocation.add(
                                PhotoLocationBillName(
                                    a.key,
                                    a.value,
                                    ""
                                )
                            )
                            i++
                            if (i == photoLocationHashMap.size) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (::listBillsAdapter.isInitialized) {
                                        listBillsAdapter.notifyDataSetChanged()
                                        binding.refreshMe.isRefreshing = false
                                    } else {
                                        getBillsFromData()
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

    private fun getBillsFromData() {
        binding.refreshMe.isRefreshing = true
        refForBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(WhoHowmuch::class.java)
                                if (temp?.groupName == groupName && temp?.snapKeyOfGroup == snapKeyOfGroup) {
                                    billNamesHash.add(temp?.billname.toString())
                                    if (temp?.whohasPaid == 2) {
                                        photoLocationHashMap.put(
                                            temp?.billname.toString(),
                                            temp?.photoLocation.toString()
                                        )
                                    }
                                    for (a in billNamesHash) {
                                        if (a in billNames) {
                                            continue
                                        } else if (!(a in billNames) && !a.isBlank()) {
                                            billNames.add(a)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (billNames.size == 0) {
                    binding.refreshMe.isRefreshing = false
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "Mevcut fatura bulunamadı", Snackbar.LENGTH_LONG).show()
                    binding.showBillNamesRecycler.removeAllViews()
                }

                var i = 0
                for (a in photoLocationHashMap) {
                    Firebase.storage.getReference(a.value)
                        .downloadUrl.addOnSuccessListener { uri ->
                            i++
                            getPhotoLocation = uri.lastPathSegment
                         
                            getPhotosWLocation.add(
                                PhotoLocationBillName(
                                    a.key,
                                    a.value,
                                    uri.toString()
                                )
                            )
                            if (i == photoLocationHashMap.size) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    initListBillsRecycler()
                                    binding.refreshMe.isRefreshing = false
                                }
                            }
                        }.addOnFailureListener {
                            getPhotosWLocation.add(
                                PhotoLocationBillName(
                                    a.key,
                                    a.value,
                                    ""
                                )
                            )
                            i++
                            if (i == photoLocationHashMap.size) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    initListBillsRecycler()
                                    binding.refreshMe.isRefreshing = false
                                }
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initListBillsRecycler() {
        listBillsAdapter = ListBillsAdapter(
            this,
            billNames,
            groupName,
            getPhotosWLocation,
            snapKeyOfGroup,
            photoLocationHashMap
        )
        binding.showBillNamesRecycler.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.showBillNamesRecycler.adapter = listBillsAdapter
    }

    private fun backBtn() {
        binding.backToLoginPage2.setOnClickListener {
            finish()
        }
    }

    private fun addBill() {
        binding.addBillBtn.setOnClickListener {
            if (groupName != null) {
                val intent = Intent(this, AddBillActivity::class.java)
                intent.putExtra("groupName", groupName)
                intent.putExtra("snapKeyOfGroup", snapKeyOfGroup)
                startActivity(intent)
            }
        }
    }

    private fun getGroupUsersInfoFromData() {
        refPerson.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(UserInfo::class.java)
                        for (user in group) {
                            if (temp?.name == user.name && temp?.mail == user?.email) {
                                groupUsers.distinct()
                                groupUsers.add(temp!!)
                            }
                        }
                    }
                }
                initListUserRecycler()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getGroupUsersFromData() {
        var getGroupOwner: String? = ""
        refGroup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.key == snapKeyOfGroup) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(Group::class.java)
                                group.add(
                                    Group(
                                        temp?.groupOwner,
                                        temp?.groupName,
                                        temp?.name,
                                        temp?.surname,
                                        temp?.email
                                    )
                                )
                                getGroupOwner = temp?.groupOwner
                            }
                        }
                    }
                }
                getGroupUsersInfoFromData()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initBanner() {
        mAdView = findViewById(R.id.adView3)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun initListUserRecycler() {
        val randomPfp = mutableListOf<Int>()
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)

        listUserAdapter = ListUserAdapter(this@DetailsOfGroupActivity, groupUsers, group,randomPfp)
        var recyclerView = findViewById<RecyclerView>(R.id.usersOfGroupRecycler)
        recyclerView.layoutManager =
            LinearLayoutManager(this@DetailsOfGroupActivity, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = listUserAdapter
    }

    private fun leaveFromGroup() {
        binding.leftFromGroupBtn.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Gruptan ayrılmak istediğinize emin misiniz?")
                .setNegativeButton("Evet") { dialog, which ->
                    refGroup.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (snap in snapshot.children) {
                                if (snap.exists()) {
                                    for (superSnap in snap.children) {
                                        if (superSnap.child("email")
                                                .getValue() == auth.currentUser?.email && superSnap.child(
                                                "groupName"
                                            ).getValue() == groupName
                                        ) {
                                            superSnap.child("surName")
                                            refGroup.child(snap.key.toString())
                                                .child(superSnap.key.toString()).removeValue()
                                                .addOnSuccessListener {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        val intent = Intent(
                                                            this@DetailsOfGroupActivity,
                                                            MainActivity::class.java
                                                        )
                                                        startActivity(intent)
                                                        finish()
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
                }.setPositiveButton("Hayır") { dialog, which -> }
                .show()
        }
    }

    private fun deletePhotoOfBills(snapKeyOfGroup: String?) {
        var snapKeyOfBill: String
        var billsNodeRef = database.getReference("Bills")
        billsNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        for (rsnap in snap.children) {
                            if (rsnap.child("snapKeyOfGroup").getValue()
                                    ?.equals(snapKeyOfGroup) == true
                            ) {
                                snapKeyOfBill = snap.key.toString()
                                if (rsnap.child("photoLocation").getValue().toString()
                                        .isNullOrEmpty() == false
                                ) {
                                    var getSpecialNodeOfBill =
                                        rsnap.getValue(WhoHowmuch::class.java)
                                    Firebase.storage.getReference(getSpecialNodeOfBill?.photoLocation.toString())
                                        .delete()
                                    Firebase.database.getReference("Bills").child(snapKeyOfBill)
                                        .removeValue()
                                }
                            }
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    var intent = Intent(this@DetailsOfGroupActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showThreeDotOptions() {
        binding.threeDotBtn.setOnClickListener {
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
    }


    override fun onRestart() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            updateData()
        }
        super.onRestart()
    }

    private fun loadAds(groupName: String) {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-5",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.toString()?.let { Log.d(TAG, it) }
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    println("Ad Loaded")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun showAds(groupName: String) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    println("Ad Dismissed")
                    mInterstitialAd = null
                    val intent = Intent(this@DetailsOfGroupActivity, AddBillActivity::class.java)
                    intent.putExtra("groupName", groupName)
                    startActivity(intent)
                }

                override fun onAdShowedFullScreenContent() {
                    println("Ad Showed")
                    mInterstitialAd = null
                }
            }
        } else {
            println("The interstitial ad wasn't ready yet.")
        }
    }

    override fun onBackPressed() {
        finish()
    }
}





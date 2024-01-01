package com.yucox.splitwise.activity


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.google.firebase.database.DatabaseReference
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.ListBillsAdapter
import com.yucox.splitwise.adapter.ListUserAdapter
import com.yucox.splitwise.model.PhotoLocationBillName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailsOfGroupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var list_bills_recyclerview: RecyclerView
    private lateinit var listBillsAdapter: ListBillsAdapter
    private var hashBills: HashSet<String> = hashSetOf()
    var billNames = ArrayList<String>()
    private lateinit var singleListener: ValueEventListener
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    var getPhotoLocation: String? = ""
    lateinit var refForBillPrice: DatabaseReference
    lateinit var ref: DatabaseReference
    lateinit var refPerson: DatabaseReference
    lateinit var refForGroup: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detailsofgroup_activity)
        lateinit var mAdView: AdView

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView3)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        var groupName = intent.getStringExtra("GroupName")
        var snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")
        var showgroupName = findViewById<TextView>(R.id.groupName_deatilsofgroupactivity)
        showgroupName.text = groupName

        auth = Firebase.auth
        database = Firebase.database
        refForBillPrice = Firebase.database.getReference("Bills")
        ref = database.getReference("Groups")
        refPerson = database.getReference("UsersData")
        refForGroup = database.getReference("Bills")

        var groupUsers = ArrayList<UserInfo>()
        var group = ArrayList<Group>()

        var adapter = ListUserAdapter(this@DetailsOfGroupActivity, groupUsers, group)
        var recyclerView = findViewById<RecyclerView>(R.id.recyclyerView_detailsofgroup)
        recyclerView.layoutManager =
            LinearLayoutManager(this@DetailsOfGroupActivity, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter

        var leftFromHGroupBtn = findViewById<ImageView>(R.id.leftFromGroupBtn)
        leftFromHGroupBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Gruptan ayrılmak istediğinize emin misiniz?")
                .setNegativeButton("Evet") { dialog, which ->
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
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
                                            ref.child(snap.key.toString())
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

        var getGroupOwner: String? = ""
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.key == snapKeyOfGroup) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(Group::class.java)
                                if (temp?.GroupName == groupName) {
                                    group.add(
                                        Group(
                                            temp?.groupOwner,
                                            temp?.GroupName,
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
                }
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
                        CoroutineScope(Dispatchers.Main).launch {
                            adapter.notifyDataSetChanged()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        var addNewBill = findViewById<ImageView>(R.id.addBillBtn)
        addNewBill.setOnClickListener {
            if (groupName != null) {
                var builder = AlertDialog.Builder(this@DetailsOfGroupActivity)
                builder.setTitle("Fatura eklemek için bir adet reklam izlemeniz gerekiyor")
                builder.setNegativeButton("Evet") { dialog, which ->
                    val intent = Intent(this, AddBillActivity::class.java)
                    intent.putExtra("groupName", groupName)
                    intent.putExtra("snapKeyOfGroup", snapKeyOfGroup)
                    startActivity(intent)
                }.setPositiveButton("Hayır") { dialog, which -> }
                    .show()
            }
        }

        var backToActivity = findViewById<ImageView>(R.id.backToLoginPage2)
        backToActivity.setOnClickListener {
            finish()
        }

        var billNamesHash = hashSetOf<String>()
        var hashPhoto = ArrayList<PhotoLocationBillName>()
        var getPhotosWLocation = ArrayList<PhotoLocationBillName>()

        list_bills_recyclerview = findViewById<RecyclerView>(R.id.showBillNamesRecycler)
        listBillsAdapter = ListBillsAdapter(
            this,
            billNames,
            groupName,
            getPhotosWLocation,
            snapKeyOfGroup
        )
        list_bills_recyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        list_bills_recyclerview.adapter = listBillsAdapter

        var getBillsFromData = ArrayList<WhoHowmuch>()
        singleListener = (object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    billNames.clear()
                    hashPhoto.clear()
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(WhoHowmuch::class.java)
                                if (temp?.groupName == groupName &&  temp?.snapKeyOfGroup == snapKeyOfGroup){
                                    billNamesHash.add(temp?.billname.toString())
                                    if (temp?.whohasPaid == 2) {
                                        hashPhoto.add(
                                            PhotoLocationBillName(
                                                temp?.billname.toString(),
                                                temp?.photoLocation.toString()
                                            )
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
                for (a in hashPhoto) {
                    var tempStorageRef = Firebase.storage.getReference(a.billName.toString())
                    tempStorageRef.child(a.photoLocation.toString()).downloadUrl.addOnSuccessListener { uri ->
                        println(uri.lastPathSegment)
                        getPhotoLocation = uri.lastPathSegment
                        getPhotosWLocation.add(
                            PhotoLocationBillName(a.billName.toString(), getPhotoLocation, uri.toString())
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            listBillsAdapter.notifyDataSetChanged()
                        }
                    }.addOnFailureListener {
                        getPhotosWLocation.add(
                            PhotoLocationBillName(
                                a.billName.toString(),
                                getPhotoLocation,
                                ""
                            )
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            listBillsAdapter.notifyDataSetChanged()
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    listBillsAdapter.notifyDataSetChanged()
                    var refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshMe)
                    refreshLayout.isRefreshing = false
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        refForGroup.addListenerForSingleValueEvent(singleListener)
        var deleteToGroup = findViewById<ImageView>(R.id.deleteToGroup)
        deleteToGroup.setOnClickListener {
            var refForDel = database.getReference("Groups")
            if (group[0].groupOwner == Firebase.auth.currentUser?.email) {
                var builder = AlertDialog.Builder(this@DetailsOfGroupActivity)
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

        var refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshMe)
        refreshLayout.setOnRefreshListener {
            refForGroup.addListenerForSingleValueEvent(singleListener)
        }
        var threeDotOptionsDetail =
            findViewById<ConstraintLayout>(R.id.threedot_options_detail_layout2)
        threeDotOptionsDetail.visibility = View.GONE
        var threeDotBtn = findViewById<ImageView>(R.id.three_dot_btn2)
        threeDotBtn.setOnClickListener { showThreeDotOptions() }
    }

    private fun deletePhotoOfBills(snapKeyOfGroup : String?) {
        var snapKeyOfBill : String
        var billsNodeRef = database.getReference("Bills")
        billsNodeRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        for(rsnap in snap.children){
                            if(rsnap.child("snapKeyOfGroup").getValue()?.equals(snapKeyOfGroup) == true){
                                snapKeyOfBill = snap.key.toString()
                                if(rsnap.child("photoLocation").getValue().toString().isNullOrEmpty() == false){
                                    var getSpecialNodeOfBill = rsnap.getValue(WhoHowmuch::class.java)
                                    println(rsnap.childrenCount)
                                    Firebase.storage.getReference(getSpecialNodeOfBill?.billname.toString()).child(getSpecialNodeOfBill?.photoLocation.toString()).delete()
                                    Firebase.database.getReference("Bills").child(snapKeyOfBill).removeValue()
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
        var threeDotOptionsDetail =
            findViewById<ConstraintLayout>(R.id.threedot_options_detail_layout2)
        if (threeDotOptionsDetail.visibility == View.GONE) {
            threeDotOptionsDetail.visibility = View.VISIBLE
        } else {
            threeDotOptionsDetail.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        refForGroup.removeEventListener(singleListener)
        super.onDestroy()
    }

    override fun onRestart() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            refForGroup.addListenerForSingleValueEvent(singleListener)
        }
        super.onRestart()
    }

    private fun loadAds(groupName: String) {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
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





package com.yucox.splitwise.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.ListBillsAdapter
import com.yucox.splitwise.adapter.ListUserAdapter
import com.yucox.splitwise.fragment.AddBill

class DetailsOfGroupActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var showgroupMembers : ImageView
    private lateinit var hidegroupMembers : ImageView
    private lateinit var showBillNamesRecycler : RecyclerView
    private lateinit var billNamesAdapter : ListBillsAdapter
    private var billUsers : HashSet<String> = hashSetOf()
    private var hashBills : HashSet<String> = hashSetOf()
    var arraylistbillNames = ArrayList<String>()
    private lateinit var listenerForGroupShow : ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detailsofgroup_activity)
        lateinit var mAdView : AdView

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView3)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        var groupName = intent.getStringExtra("GroupName")

        var showgroupName = findViewById<TextView>(R.id.groupName_deatilsofgroupactivity)
        showgroupName.text = groupName

        database = Firebase.database
        var ref = database.getReference("Groups")
        var refPerson = database.getReference( "UsersData")

        var groupUsers = ArrayList<UserInfo>()
        var group = ArrayList<Group>()

        var adapter = ListUserAdapter(this@DetailsOfGroupActivity,groupUsers,group)
        var recyclerView = findViewById<RecyclerView>(R.id.recyclyerView_detailsofgroup)
        recyclerView.layoutManager = LinearLayoutManager(this@DetailsOfGroupActivity,RecyclerView.HORIZONTAL,false)
        recyclerView.adapter = adapter


        var listenerForUserInfo = refPerson.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue(UserInfo::class.java)
                        for(user in group) {
                            if (temp?.name == user.name && temp?.mail == user?.email){
                                groupUsers.distinct()
                                groupUsers.add(temp!!)
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        var listener = ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.exists()){
                            for(realSnap in snap.children){
                                var temp = realSnap.getValue(Group::class.java)
                                if(temp?.GroupName == groupName) {
                                    group.add(Group(temp?.groupOwner,temp?.GroupName,temp?.name,temp?.surname,temp?.email))
                                }
                            }
                        }
                    }
                }
            refPerson.addValueEventListener(listenerForUserInfo)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        ref.addValueEventListener(listener)



        var hideOrshow = findViewById<TextView>(R.id.hideOrshow)
        hideOrshow.text = "Gizle"


        hidegroupMembers = findViewById(R.id.hidegroupMembers_detailsofgroupactivity)
        hidegroupMembers.visibility = View.VISIBLE
        hidegroupMembers.setOnClickListener {
            recyclerView.visibility = View.GONE
            hidegroupMembers.visibility = View.GONE
            showgroupMembers.visibility = View.VISIBLE
            hideOrshow.text = "Göster"
        }

        showgroupMembers = findViewById<ImageView>(R.id.showgroupMembersBtn)
        showgroupMembers.visibility = View.GONE
        showgroupMembers.setOnClickListener {
            recyclerView.visibility = View.VISIBLE
            showgroupMembers.visibility = View.GONE
            hidegroupMembers.visibility = View.VISIBLE
            hideOrshow.text = "Gizle"
        }


        var addNewBill = findViewById<LinearLayout>(R.id.addBillBtn)

        var fragmentContainer = findViewById<View>(R.id.fragmentContainerView_detailsofgroupactivity)
        fragmentContainer.visibility = View.GONE

        var secondLinear = findViewById<LinearLayout>(R.id.secondLinear)

        var detailsOfGroupBackground = findViewById<LinearLayout>(R.id.detailsofgroupBackground)
        detailsOfGroupBackground.setOnClickListener{
            secondLinear.visibility = View.VISIBLE
            showBillNamesRecycler.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
            showgroupMembers.isClickable = true
            addNewBill.visibility = View.VISIBLE
            if(hidegroupMembers.isVisible == true){
                recyclerView.visibility = View.VISIBLE
            }else{
                recyclerView.visibility = View.INVISIBLE
            }
            if(showgroupMembers.isVisible == true){
                recyclerView.visibility = View.GONE
            }
            var refForGroup2 = database.getReference("Bills")
            refForGroup2.addValueEventListener(listenerForGroupShow)
            billNamesAdapter.notifyDataSetChanged()
        }

        var counter = 0
        addNewBill.setOnClickListener {
            if(counter == 0) {
                var AddBillFragment = AddBill()
                var args = Bundle()
                args.putString("GroupName", groupName)
                AddBillFragment.arguments = args
                replaceFragment(AddBillFragment)
                counter++
                Toast.makeText(this@DetailsOfGroupActivity,"Kapatmak için çerçeve dışına tıkla!",Toast.LENGTH_LONG).show()
            }
            secondLinear.visibility = View.GONE
            showBillNamesRecycler.visibility = View.GONE
            recyclerView.visibility = View.GONE
            addNewBill.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            fragmentContainer.isClickable = true
            showgroupMembers.isClickable = false
        }



        var refForGroup = database.getReference("Bills")
        var billNamesHash = hashSetOf<String>()
        var dataWhoHowmuch = ArrayList<WhoHowmuch>()

        showBillNamesRecycler = findViewById<RecyclerView>(R.id.showBillNamesRecycler)
        billNamesAdapter = ListBillsAdapter(this,arraylistbillNames,groupName,hashBills,billUsers)
        showBillNamesRecycler.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        showBillNamesRecycler.adapter = billNamesAdapter

        listenerForGroupShow = refForGroup.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.exists()){
                            for(realSnap in snap.children){
                                var temp = realSnap.getValue(WhoHowmuch::class.java)
                                if(temp?.groupName == groupName) {
                                    billNamesHash.add(temp?.billname.toString())
                                    billUsers.add(temp?.whoBought.toString())
                                    println(temp?.billname)
                                    println(billUsers)
                                    for(a in billNamesHash){
                                        if(a in arraylistbillNames){

                                        }else if(!(a in arraylistbillNames) && !a.isBlank())
                                        arraylistbillNames.add(a)
                                    }
                                }
                            }
                        }
                    }
                }
                billNamesAdapter.notifyDataSetChanged()
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        refForGroup.addValueEventListener(listenerForGroupShow)
        var deleteToGroup = findViewById<ImageView>(R.id.deleteToGroup)
        deleteToGroup.setOnClickListener {
            if(group[0].groupOwner == Firebase.auth.currentUser?.email){
                var builder = AlertDialog.Builder(this@DetailsOfGroupActivity)
                builder.setTitle("Grubu silmek istediğinze emin misiniz?")
                    .setNegativeButton("Evet, son kararım!"){dialog,which ->
                        var refForDelete = database.getReference("Groups")
                        refForDelete.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()){
                                    for(snap in snapshot.children){
                                        if(snap.exists()){
                                            for(rsnap in snap.children){
                                                var control = rsnap.child("groupName").getValue()?.equals(groupName)
                                                var key = snap.key
                                                if(control == true){
                                                    refForDelete.child(key.toString()).removeValue()
                                                    Toast.makeText(this@DetailsOfGroupActivity,"Grup Başarıyla Silindi!",Toast.LENGTH_LONG).show()
                                                    recyclerView.adapter = null
                                                    deleteBillsAndPhotos(groupName.toString(),billUsers)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }.setPositiveButton("Hayır, sadece elim çarptı..") {dialog,which ->
                        Toast.makeText(this@DetailsOfGroupActivity,"Grup Başarıyla Silin- \nDemek sadece elin çarptı ^^",Toast.LENGTH_LONG).show()
                    }.show()
            }else{
                Toast.makeText(this@DetailsOfGroupActivity,"Sadece Grup Kurucusu Grubu Silebilir.",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }

        var refForBillPrice = Firebase.database.getReference("Bills")
        refForBillPrice.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.exists()){
                            for(rsnap in snap.children){
                                var temp = rsnap.child("totalPrice").getValue()
                                hashBills?.add(temp.toString())
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        var refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refreshMe)
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
        }
    }

    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView_detailsofgroupactivity,fragment)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
    fun deleteBillsAndPhotos(groupName : String, billUsers : HashSet<String>){
        var dataRef = Firebase.database.getReference("Bills")
        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        for (rsnap in snap.children) {
                            var temp = rsnap.getValue(WhoHowmuch::class.java)
                            for(bill in arraylistbillNames) {
                                if (temp?.billname == bill) {
                                    var uniqueId = snap.key.toString()
                                    println(uniqueId)
                                    dataRef.child(uniqueId).removeValue().addOnSuccessListener {
                                    }
                                }
                                for(a in billUsers) {
                                    var storageRef = Firebase.storage.getReference("$groupName").child(bill).child(a)
                                    storageRef.delete()
                                    println("silinecek $groupName child $bill child $a")
                                }
                            }
                        }
                    }
                }
                finish()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}








package com.yucox.splitwise.activity



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.R.R.model.GetMails
import com.R.R.model.SendFriendRequest
import com.R.R.model.Group
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.yucox.splitwise.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateGroup : AppCompatActivity() {
    private lateinit var database : FirebaseDatabase
    private var selectedUsers =  mutableListOf<String>()
    private lateinit var adapter3 : ArrayAdapter<String>
    private lateinit var adapter4 : ArrayAdapter<String>
    private lateinit var adapter5 : ArrayAdapter<String>
    private lateinit var adapter6 : ArrayAdapter<String>
    private lateinit var adapter7 : ArrayAdapter<String>
    private lateinit var adapter8 : ArrayAdapter<String>
    var getNamendSurnameFromData = mutableListOf<String>()
    var getMails = ArrayList<GetMails>()
    var allFriendsInfo = ArrayList<com.R.R.model.UserInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)
        lateinit var mAdView : AdView

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView2)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                println("başarısız")
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                println("başarılı")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }


        var auth = FirebaseAuth.getInstance()

        database = Firebase.database
        var usersDataRef = database.getReference("UsersData")
        var groupRef = database.getReference("Groups")
        var groupRefKey = groupRef.push().key.toString()

        var isThatRepeat = HashSet<String>()

        var ref = database.getReference("FriendRequest")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var tempControl = snap.getValue(SendFriendRequest::class.java)
                        if(tempControl?.whoGetFriendRequest == auth.currentUser?.email){
                            if(tempControl?.whoSentFriendRequest in isThatRepeat == false) {
                                if (tempControl?.status == 1) {
                                    getMails.add(GetMails(tempControl?.whoSentFriendRequest))
                                }
                            }
                            isThatRepeat.add(tempControl?.whoSentFriendRequest.toString())
                        }else if(tempControl?.whoSentFriendRequest == auth.currentUser?.email){
                            if(tempControl?.whoGetFriendRequest in isThatRepeat == false) {
                                if (tempControl?.status == 1) {
                                    getMails.add(GetMails(tempControl?.whoGetFriendRequest))
                                }
                            }
                            isThatRepeat.add(tempControl?.whoSentFriendRequest.toString())
                        }
                    }
                }
                getInfoFromMail()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })



        val autoComplete3 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup3)
        adapter3 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)

        autoComplete3.setAdapter(adapter3)
        autoComplete3.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)
            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        autoComplete3.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        autoComplete3.isEnabled = false
                    }
                }
            }
        }

        val autoComplete4 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup4)

        adapter4 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)

        autoComplete4.setAdapter(adapter4)
        autoComplete4.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        autoComplete4.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        autoComplete4.isEnabled = false

                    }
                }
            }
        }
        val autoComplete5 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup5)

        adapter5 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)

        autoComplete5.setAdapter(adapter5)
        autoComplete5.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        autoComplete5.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        autoComplete5.isEnabled = false

                    }
                }
            }
        }
        val autoComplete6 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup6)


        adapter6 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)

        autoComplete6.setAdapter(adapter6)
        autoComplete6.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        autoComplete6.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        autoComplete6.isEnabled = false
                    }
                }
            }
        }
        val autoComplete7 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup7)

        adapter7 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)

        autoComplete7.setAdapter(adapter7)
        autoComplete7.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        autoComplete7.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        autoComplete7.isEnabled = false
                    }
                }
            }
        }
        val autoComplete8 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup8)

        adapter8 = ArrayAdapter(this,R.layout.list_item,getNamendSurnameFromData)

        autoComplete8.visibility = View.GONE
        var textInput = findViewById<TextInputLayout>(R.id.textInput1)
        textInput.visibility = View.GONE

        var addmore2 = findViewById<ImageView>(R.id.addMoreBtn2)
        addmore2.visibility = View.GONE
        var addMore = findViewById<ImageView>(R.id.addMoreBtn)
        addMore.setOnClickListener {
            textInput.visibility = View.VISIBLE
            autoComplete8.visibility = View.VISIBLE
            addmore2.visibility = View.VISIBLE
            autoComplete8.setAdapter(adapter8)
            addMore.visibility = View.GONE
            autoComplete8.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                val selectedItem = adapterView.getItemAtPosition(i)

                for(user in allFriendsInfo){
                    if(selectedItem == "${user.name} ${user.surname}"){
                        if(selectedItem.toString() in selectedUsers){
                            Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                            autoComplete8.text.clear()
                        }else{
                            selectedUsers.add(selectedItem.toString())
                            autoComplete8.isEnabled = false
                        }
                    }
                }
            }
        }
        var linearX2 = findViewById<LinearLayout>(R.id.linearX2)
        linearX2.visibility = View.GONE
        var addmore3 = findViewById<ImageView>(R.id.addMoreBtn3)
        addmore3.visibility = View.GONE
        addmore2.setOnClickListener {
            linearX2.visibility = View.VISIBLE
            addmore2.visibility = View.GONE
            addmore3.visibility = View.VISIBLE
        }
        var linearX3 = findViewById<LinearLayout>(R.id.linearX3)
        linearX3.visibility = View.GONE
        addmore3.setOnClickListener {
            linearX3.visibility = View.VISIBLE
            addmore3.visibility = View.GONE
        }

        var saveBtn = findViewById<ImageView>(R.id.group_save)
        saveBtn.setOnClickListener {
            var groupName = findViewById<EditText>(R.id.selectGroupname).text.toString()
            var counter = 0

            for(user in selectedUsers){
                //println(user)
                counter++
            }

            var clean_group_arraylist = ArrayList<Group>()

            if(counter < 1) {
                var getGroupOwnerInfo = Group()

                usersDataRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(snap in snapshot.children){
                                var temp = snap.getValue(com.R.R.model.UserInfo::class.java)
                                    if(auth.currentUser?.email.toString() == temp?.mail){
                                        getGroupOwnerInfo.groupOwner = temp?.mail
                                        getGroupOwnerInfo.GroupName = groupName
                                        getGroupOwnerInfo.name = temp?.name
                                        getGroupOwnerInfo.surname = temp?.surname
                                        getGroupOwnerInfo.email = temp?.mail
                                        getGroupOwnerInfo.snapKeyOfGroup = groupRefKey
                                }
                                clean_group_arraylist.add(getGroupOwnerInfo)
                            }
                        }
                        var send_group_name = groupName
                        save_and_controldata(getGroupOwnerInfo,clean_group_arraylist,send_group_name,groupRefKey)
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }else if(groupName.isBlank()){
                Toast.makeText(this@CreateGroup, "Grup ismi boş olmamalıdır.", Toast.LENGTH_SHORT).show()
            }
            else if(counter >= 0 && groupName.isBlank() == false){

                var getGroupOwnerInfo = Group()

                usersDataRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(snap in snapshot.children){
                                var temp = snap.getValue(com.R.R.model.UserInfo::class.java)
                                for(selectedUser in selectedUsers){
                                    if(auth.currentUser?.email.toString() == temp?.mail){
                                        getGroupOwnerInfo.groupOwner = temp?.mail
                                        getGroupOwnerInfo.GroupName = groupName
                                        getGroupOwnerInfo.name = temp?.name
                                        getGroupOwnerInfo.surname = temp?.surname
                                        getGroupOwnerInfo.email = temp?.mail
                                        getGroupOwnerInfo.snapKeyOfGroup = groupRefKey
                                    }
                                    for(usermail in getMails){
                                        if(selectedUser == "${temp?.name} ${temp?.surname}" && usermail.userMail == temp?.mail){
                                            clean_group_arraylist.add(Group(auth.currentUser?.email.toString(),groupName,temp?.name,temp?.surname,temp?.mail,groupRefKey))
                                        }
                                    }
                                }
                            }
                        }
                        var send_group_name = groupName
                        save_and_controldata(getGroupOwnerInfo,clean_group_arraylist,send_group_name,groupRefKey)
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
        var goToBack = findViewById<ImageView>(R.id.backToLoginPage3)
        goToBack.setOnClickListener {
            val intent = Intent(this@CreateGroup,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getInfoFromMail() {
        var usersDataRef = database.getReference("UsersData")
        usersDataRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var i = 0
                    for(snap in snapshot.children){
                        var temp = snap.getValue(com.R.R.model.UserInfo::class.java)
                        for(user in getMails){
                            if(user.userMail == temp?.mail){
                                getNamendSurnameFromData.add(i,"${temp?.name.toString()} ${temp?.surname.toString()}")
                                allFriendsInfo.add(com.R.R.model.UserInfo(temp?.name,temp?.surname,temp?.mail,temp?.pfpUri))
                            }
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    adapter3.notifyDataSetChanged()
                    adapter4.notifyDataSetChanged()
                    adapter5.notifyDataSetChanged()
                    adapter6.notifyDataSetChanged()
                    adapter7.notifyDataSetChanged()
                    adapter8.notifyDataSetChanged()
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun save_and_controldata(creatorUser : Group, clean_group_arraylist : ArrayList<Group>, group_name : String,groupRefKey : String){
        var groupRef = database.getReference("Groups")

        clean_group_arraylist.add(creatorUser)

        var clean_and_checkis_group_repeat = clean_group_arraylist.distinct()

        var checker = 0

        var is_group_saved = Group()
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.exists()){
                            for(memo in snap.children){
                                var temp = memo.getValue(Group::class.java)
                                is_group_saved = temp!!
                                if(is_group_saved.groupOwner == creatorUser.groupOwner && is_group_saved.GroupName == creatorUser.GroupName)
                                {
                                    checker = 1
                                }
                            }
                        }
                    }
                }
                if(checker == 1){
                    clean_group_arraylist.clear()
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@CreateGroup,"Bu grup adı önceden kullanılmış, lütfen başka bir isim seçiniz",Toast.LENGTH_LONG).show()
                    }
                }else{
                    if(clean_and_checkis_group_repeat != null) {
                        groupRef.child(groupRefKey).setValue(clean_and_checkis_group_repeat).addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(this@CreateGroup,"Grup başarıyla oluşturuldu.",Toast.LENGTH_LONG).show()
                                val intent = Intent(this@CreateGroup, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onBackPressed() {
        val intent = Intent(this@CreateGroup,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}




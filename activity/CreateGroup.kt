package com.yucox.splitwise.activity



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
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
import com.R.R.model.GetMails
import com.R.R.model.SendFriendRequest
import com.R.R.model.Group
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.yucox.splitwise.R
import com.yucox.splitwise.databinding.CreateGroupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateGroup : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private var selectedUsers =  mutableListOf<String>()
    private val usersDataRef = database.getReference("UsersData")
    private val groupRef = database.getReference("Groups")
    private val groupRefKey = groupRef.push().key.toString()

    private lateinit var adapter3 : ArrayAdapter<String>
    private lateinit var adapter4 : ArrayAdapter<String>
    private lateinit var adapter5 : ArrayAdapter<String>
    private lateinit var adapter6 : ArrayAdapter<String>
    private lateinit var adapter7 : ArrayAdapter<String>
    private lateinit var adapter8 : ArrayAdapter<String>
    var getNamendSurnameFromData = mutableListOf<String>()
    var getMails = ArrayList<GetMails>()
    var allFriendsInfo = ArrayList<com.R.R.model.UserInfo>()
    lateinit var mAdView : AdView
    private val auth = FirebaseAuth.getInstance()
    private var isThatRepeat = HashSet<String>()
    private lateinit var binding : CreateGroupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        initBanner()

        getMailsFromData()

        addMoreUser()

        saveSelectedUsers()

        backToOldActivity()
    }

    private fun backToOldActivity() {
        binding.backToOldPageBtn.setOnClickListener {
            val intent = Intent(this@CreateGroup,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveSelectedUsers() {
        binding.saveBtn.setOnClickListener {
            var groupName = findViewById<EditText>(R.id.selectGroupname).text.toString()
            var counter = 0
            for(user in selectedUsers){
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
                                    getGroupOwnerInfo.groupName = groupName
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
                                        getGroupOwnerInfo.groupName = groupName
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
    }

    private fun addMoreUser() {
        binding.secondSelectLinear.visibility = View.GONE
        binding.thirdSelectLinear.visibility = View.GONE
        binding.addMoreBtn3.visibility = View.GONE

        binding.addMoreBtn2.setOnClickListener {
            binding.secondSelectLinear.visibility = View.VISIBLE
            binding.addMoreBtn2.visibility = View.GONE
            binding.addMoreBtn3.visibility = View.VISIBLE
        }
        binding.addMoreBtn3.setOnClickListener {
            binding.thirdSelectLinear.visibility = View.VISIBLE
            binding.addMoreBtn3.visibility = View.GONE
        }
    }

    private fun initSelectUserAdapters() {
        adapter3 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)
        binding.autoCompleteTextCreateGroup3.setAdapter(adapter3)
        binding.autoCompleteTextCreateGroup3.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)
            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        binding.autoCompleteTextCreateGroup3.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        binding.autoCompleteTextCreateGroup3.isEnabled = false
                    }
                }
            }
        }


        adapter4 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)
        binding.autoCompleteTextCreateGroup4.setAdapter(adapter4)
        binding.autoCompleteTextCreateGroup4.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        binding.autoCompleteTextCreateGroup4.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        binding.autoCompleteTextCreateGroup4.isEnabled = false

                    }
                }
            }
        }

        adapter5 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)
        binding.autoCompleteTextCreateGroup5.setAdapter(adapter5)
        binding.autoCompleteTextCreateGroup5.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        binding.autoCompleteTextCreateGroup5.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        binding.autoCompleteTextCreateGroup5.isEnabled = false

                    }
                }
            }
        }

        adapter6 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)
        binding.autoCompleteTextCreateGroup6.setAdapter(adapter6)
        binding.autoCompleteTextCreateGroup6.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        binding.autoCompleteTextCreateGroup6.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        binding.autoCompleteTextCreateGroup6.isEnabled = false
                    }
                }
            }
        }
        adapter7 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurnameFromData)

        binding.autoCompleteTextCreateGroup7.setAdapter(adapter7)
        binding.autoCompleteTextCreateGroup7.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i)

            for(user in allFriendsInfo){
                if(selectedItem == "${user.name} ${user.surname}"){
                    if(selectedItem.toString() in selectedUsers){
                        Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                        binding.autoCompleteTextCreateGroup7.text.clear()
                    }else{
                        selectedUsers.add(selectedItem.toString())
                        binding.autoCompleteTextCreateGroup7.isEnabled = false
                    }
                }
            }
        }

        adapter8 = ArrayAdapter(this,R.layout.list_item,getNamendSurnameFromData)
        binding.autoCompleteTextCreateGroup8.visibility = View.GONE
        var textInput = findViewById<TextInputLayout>(R.id.textInput1)
        textInput.visibility = View.GONE

        binding.addMoreBtn2.visibility = View.GONE
        binding.addMoreBtn.setOnClickListener {
            textInput.visibility = View.VISIBLE
            binding.autoCompleteTextCreateGroup8.visibility = View.VISIBLE
            binding.addMoreBtn2.visibility = View.VISIBLE
            binding.autoCompleteTextCreateGroup8.setAdapter(adapter8)
            binding.addMoreBtn.visibility = View.GONE
            binding.autoCompleteTextCreateGroup8.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                val selectedItem = adapterView.getItemAtPosition(i)

                for(user in allFriendsInfo){
                    if(selectedItem == "${user.name} ${user.surname}"){
                        if(selectedItem.toString() in selectedUsers){
                            Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                            binding.autoCompleteTextCreateGroup8.text.clear()
                        }else{
                            selectedUsers.add(selectedItem.toString())
                            binding.autoCompleteTextCreateGroup8.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun getMailsFromData() {
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
    }

    private fun initBanner() {
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
                initSelectUserAdapters()
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
                                if(is_group_saved.groupOwner == creatorUser.groupOwner && is_group_saved.groupName == creatorUser.groupName)
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
                    if(clean_and_checkis_group_repeat != null && binding.selectGroupname.text.toString().isNullOrEmpty() == false) {
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



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
import com.yucox.splitwise.R

class CreateGroup : AppCompatActivity() {
    private lateinit var database : FirebaseDatabase
    private lateinit var listener : ValueEventListener
    private var userList =  mutableListOf<String>()
    private lateinit var adapter3 : ArrayAdapter<String>
    private lateinit var adapter4 : ArrayAdapter<String>
    private lateinit var adapter5 : ArrayAdapter<String>
    private lateinit var adapter6 : ArrayAdapter<String>
    private lateinit var adapter7 : ArrayAdapter<String>
    private lateinit var adapter8 : ArrayAdapter<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)
        lateinit var mAdView : AdView

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView2)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        var auth = FirebaseAuth.getInstance()

        database = Firebase.database
        var ref = database.getReference("FriendRequest")

        var getMails = ArrayList<GetMails>()


        var isThatRepeat = HashSet<String>()
        ref.addValueEventListener(object : ValueEventListener{
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
                ref.addValueEventListener(listener)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        var ref2 = database.getReference("UsersData")

        var getNamendSurname = mutableListOf<String>()

        var allUserInfo = ArrayList<com.R.R.model.UserInfo>()
        var count = 0

        listener = ref2.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var i = 0
                    for(snap in snapshot.children){
                        var temp = snap.getValue(com.R.R.model.UserInfo::class.java)
                        for(user in getMails){
                            if(user.userMail == temp?.mail){
                                getNamendSurname.add(i,"${temp?.name.toString()} ${temp?.surname.toString()}")
                                allUserInfo.add(com.R.R.model.UserInfo(temp?.name,temp?.surname,temp?.mail,temp?.pfpUri))
                                count++
                            }
                        }
                    }
                }
                var warningText = findViewById<TextView>(R.id.warningText)
                warningText.visibility = View.GONE
                if(count == 0){
                    warningText.visibility = View.VISIBLE
                    warningText.text = "Grup oluşturmanız için önce arkadaş eklemelisiniz."
                }

                userList =  mutableListOf<String>()

                val autoComplete3 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup3)

                adapter3 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurname)

                autoComplete3.setAdapter(adapter3)
                autoComplete3.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                    val selectedItem = adapterView.getItemAtPosition(i)
                    for(user in allUserInfo){
                        if(selectedItem == "${user.name} ${user.surname}"){
                            if(selectedItem.toString() in userList){
                                Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                                autoComplete3.text.clear()
                            }else{
                                userList.add(selectedItem.toString())
                                autoComplete3.isEnabled = false
                            }
                        }
                    }
                }

                val autoComplete4 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup4)

                adapter4 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurname)

                autoComplete4.setAdapter(adapter4)
                autoComplete4.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                    val selectedItem = adapterView.getItemAtPosition(i)

                    for(user in allUserInfo){
                        if(selectedItem == "${user.name} ${user.surname}"){
                            if(selectedItem.toString() in userList){
                                Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                                autoComplete4.text.clear()
                            }else{
                                userList.add(selectedItem.toString())
                                autoComplete4.isEnabled = false

                            }
                        }
                    }
                }
                val autoComplete5 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup5)

                adapter5 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurname)

                autoComplete5.setAdapter(adapter5)
                autoComplete5.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                    val selectedItem = adapterView.getItemAtPosition(i)

                    for(user in allUserInfo){
                        if(selectedItem == "${user.name} ${user.surname}"){
                            if(selectedItem.toString() in userList){
                                Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                                autoComplete5.text.clear()
                            }else{
                                userList.add(selectedItem.toString())
                                autoComplete5.isEnabled = false

                            }
                        }
                    }
                }
                val autoComplete6 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup6)


                adapter6 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurname)

                autoComplete6.setAdapter(adapter6)
                autoComplete6.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                    val selectedItem = adapterView.getItemAtPosition(i)

                    for(user in allUserInfo){
                        if(selectedItem == "${user.name} ${user.surname}"){
                            if(selectedItem.toString() in userList){
                                Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                                autoComplete6.text.clear()
                            }else{
                                userList.add(selectedItem.toString())
                                autoComplete6.isEnabled = false
                            }
                        }
                    }
                }
                val autoComplete7 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup7)

                adapter7 = ArrayAdapter(this@CreateGroup,R.layout.list_item,getNamendSurname)

                autoComplete7.setAdapter(adapter7)
                autoComplete7.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                    val selectedItem = adapterView.getItemAtPosition(i)

                    for(user in allUserInfo){
                        if(selectedItem == "${user.name} ${user.surname}"){
                            if(selectedItem.toString() in userList){
                                Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                                autoComplete7.text.clear()
                            }else{
                                userList.add(selectedItem.toString())
                                autoComplete7.isEnabled = false
                            }
                        }
                    }
                }
                adapter3.notifyDataSetChanged()
                adapter4.notifyDataSetChanged()
                adapter5.notifyDataSetChanged()
                adapter6.notifyDataSetChanged()
                adapter7.notifyDataSetChanged()
                adapter8.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })



        val autoComplete8 = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextCreateGroup8)

        adapter8 = ArrayAdapter(this,R.layout.list_item,getNamendSurname)

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

                for(user in allUserInfo){
                    if(selectedItem == "${user.name} ${user.surname}"){
                        if(selectedItem.toString() in userList){
                            Toast.makeText(this@CreateGroup,"Sadece bir kez ekleyebilirsin",Toast.LENGTH_SHORT).show()
                            autoComplete8.text.clear()
                        }else{
                            userList.add(selectedItem.toString())
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



        var cleanAllTexts = findViewById<LinearLayout>(R.id.cleangrouptexts)

        cleanAllTexts.setOnClickListener{

            userList.clear()

            var groupName = findViewById<EditText>(R.id.selectGroupname)

            groupName.text.clear()
        }



        var saveBtn = findViewById<ImageView>(R.id.group_save)

        saveBtn.setOnClickListener {

            var groupName = findViewById<EditText>(R.id.selectGroupname).text.toString()

            var counter = 0

            for(user in userList){
                //println(user)
                counter++
            }

            var clean_group_arraylist = ArrayList<Group>()

            if(counter < 1) {
                Toast.makeText(this@CreateGroup, "Grubunuz en az iki kişi olmalıdır.", Toast.LENGTH_SHORT).show()
            }else if(groupName.isBlank()){
                Toast.makeText(this@CreateGroup, "Grup ismi boş olmamalıdır.", Toast.LENGTH_SHORT).show()
            }
            else if(counter >= 1 && groupName.isBlank() == false){

                var getandset_group_owner = Group()

                ref2.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(snap in snapshot.children){
                                var temp = snap.getValue(com.R.R.model.UserInfo::class.java)
                                for(user in userList){
                                    if(auth.currentUser?.email.toString() == temp?.mail){
                                        getandset_group_owner.name = temp?.name
                                        getandset_group_owner.surname = temp?.surname
                                        getandset_group_owner.email = temp?.mail
                                        getandset_group_owner.GroupName = groupName
                                        getandset_group_owner.groupOwner = temp?.mail
                                    }
                                    for(usermail in getMails){
                                    if(user == "${temp?.name} ${temp?.surname}" && usermail.userMail == temp?.mail){
                                        clean_group_arraylist.add(Group(auth.currentUser?.email,groupName,temp?.name,temp?.surname,temp?.mail))
                                    }
                                    }
                                }
                            }
                        }
                        var send_group_name = groupName
                        save_and_controldata(getandset_group_owner,clean_group_arraylist,send_group_name)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }
    }
    private fun save_and_controldata(creatorUser : Group, clean_group_arraylist : ArrayList<Group>, group_name : String){
        var groupRef = database.getReference("Groups")

        var auth = FirebaseAuth.getInstance()

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
                    Toast.makeText(this@CreateGroup,"Bu grup adı önceden kullanılmış, lütfen başka bir isim seçiniz",Toast.LENGTH_LONG).show()
                }else{
                    if(clean_and_checkis_group_repeat != null) {
                        groupRef.push().setValue(clean_and_checkis_group_repeat)

                        Toast.makeText(this@CreateGroup,"Grup başarıyla oluşturuldu.",Toast.LENGTH_LONG).show()
                        val intent = Intent(this@CreateGroup, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onBackPressed() {
        val intent = Intent(this@CreateGroup,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}










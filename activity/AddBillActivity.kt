package com.yucox.splitwise.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.R.R.model.Group
import com.R.R.model.UserInfo
import com.R.R.model.WhoHowmuch
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBillActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    private lateinit var database: FirebaseDatabase
    var handler = Handler()
    private lateinit var firstListener: ValueEventListener
    lateinit var refOfBills: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_bill_activity)

        var groupUsers = ArrayList<UserInfo>()
        var group = ArrayList<Group>()

        var groupName = intent.getStringExtra("groupName")
        if (groupName != null) {
            loadAds(groupName)
        }
        var snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")

        database = Firebase.database
        var ref = database.getReference("Groups")
        refOfBills = database.getReference("Bills")
        var refgroupusersInfo = database.getReference("UsersData")
        var auth = Firebase.auth
        var whoBought: String? = ""

        var getonlyNameAndSurname = mutableListOf<String>()

        var progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        progressBar.visibility = View.GONE

        fun afterGotGroupData() {
            refgroupusersInfo.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var i = 0
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            var temp = snap.getValue(UserInfo::class.java)
                            for (user in group.distinct()) {
                                if (user?.email == temp?.mail && user.snapKeyOfGroup == snapKeyOfGroup){
                                    if (user.email != auth.currentUser?.email) {
                                        getonlyNameAndSurname.add(i, "${user.name} ${user.surname}")
                                        i++
                                    } else {
                                        whoBought = "${user.name} ${user.surname}"
                                    }
                                    groupUsers.add(temp!!)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realsnap in snap.children) {
                                var temp = realsnap.getValue(Group::class.java)
                                if (temp?.snapKeyOfGroup == snapKeyOfGroup) {
                                    group.add(temp!!)
                                }
                            }
                        }
                    }
                }
                afterGotGroupData()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        var getSelectedImage: String? = ""
        var galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    var selectedImageUri = data?.data
                    var smallBillPhoto = findViewById<ImageView>(R.id.smallBillPhoto)
                    smallBillPhoto.setImageURI(selectedImageUri)
                    getSelectedImage = selectedImageUri.toString()
                } else {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Hiçbir resim seçilmedi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        var photoOfBill = findViewById<LinearLayout>(R.id.photoOfBill_addbillfragment)
        photoOfBill.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }

        var billName = findViewById<TextView>(R.id.bill_name_et)
        var addPrice = findViewById<TextView>(R.id.price_et)
        var savetheBill = findViewById<ImageView>(R.id.savetheBill)

        var specialPriceForEveryoneBtn = findViewById<ImageView>(R.id.specialPriceForEveryPersonBtn)
        var specialpriceLinear = findViewById<LinearLayout>(R.id.specialpriceLinear)
        var partofSpecialLinear2 = findViewById<LinearLayout>(R.id.partofSpecialLinear2)

        var cake2 = findViewById<LinearLayout>(R.id.cake2)
        cake2.visibility = View.GONE
        var cake3 = findViewById<LinearLayout>(R.id.cake3)
        cake3.visibility = View.GONE
        var cake4 = findViewById<LinearLayout>(R.id.cake4)
        cake4.visibility = View.GONE
        var cake5 = findViewById<LinearLayout>(R.id.cake5)
        cake5.visibility = View.GONE
        var cake6 = findViewById<LinearLayout>(R.id.cake6)
        cake6.visibility = View.GONE

        specialpriceLinear.visibility = View.GONE
        partofSpecialLinear2.visibility = View.GONE
        specialPriceForEveryoneBtn.setOnClickListener {
            if (specialpriceLinear.visibility == View.VISIBLE) {
                specialpriceLinear.visibility = View.GONE
            } else {
                specialpriceLinear.visibility = View.VISIBLE
            }
        }
        var addmoreSpecialPriceBtn = findViewById<ImageView>(R.id.addmoreSpecialPrice)
        var specialpriceBorderCounter = 1
        addmoreSpecialPriceBtn.setOnClickListener {
            specialpriceBorderCounter++
            if (specialpriceBorderCounter == 2) {
                cake2.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 3) {
                cake3.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 4) {
                partofSpecialLinear2.visibility = View.VISIBLE
                cake4.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 5) {
                cake5.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 6) {
                cake6.visibility = View.VISIBLE
            }
        }
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.selectSpecial)
        val autoComplete2 = findViewById<AutoCompleteTextView>(R.id.selectSpecial2)
        val autoComplete3 = findViewById<AutoCompleteTextView>(R.id.selectSpecial3)
        val autoComplete4 = findViewById<AutoCompleteTextView>(R.id.selectSpecial4)
        val autoComplete5 = findViewById<AutoCompleteTextView>(R.id.selectSpecial5)
        val autoComplete6 = findViewById<AutoCompleteTextView>(R.id.selectSpecial6)

        val adapter = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter2 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter3 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter4 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter5 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter6 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)

        autoComplete.setAdapter(adapter)
        autoComplete2.setAdapter(adapter2)
        autoComplete3.setAdapter(adapter3)
        autoComplete4.setAdapter(adapter4)
        autoComplete5.setAdapter(adapter5)
        autoComplete6.setAdapter(adapter6)

        var checkIsNameRepeat = mutableListOf<String>()

        var user1WhoWillPay: String? = ""
        var selectedItem1: Any?
        var user2WhoWillPay: String? = ""
        var selectedItem2: Any?
        var user3WhoWillPay: String? = ""
        var selectedItem3: Any?
        var user4WhoWillPay: String? = ""
        var selectedItem4: Any?
        var user5WhoWillPay: String? = ""
        var selectedItem5: Any?
        var user6WhoWillPay: String? = ""
        var selectedItem6: Any?

        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem1 = adapterView.getItemAtPosition(i)
                if (selectedItem1 in checkIsNameRepeat) {
                    autoComplete.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user1WhoWillPay = selectedItem1.toString()
                    checkIsNameRepeat.add(user1WhoWillPay.toString())
                    autoComplete.isEnabled = false
                }
            }
        autoComplete2.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem2 = adapterView.getItemAtPosition(i)
                if (selectedItem2 in checkIsNameRepeat) {
                    autoComplete2.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user2WhoWillPay = selectedItem2.toString()
                    checkIsNameRepeat.add(user2WhoWillPay.toString())
                    autoComplete2.isEnabled = false
                }
            }
        autoComplete3.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem3 = adapterView.getItemAtPosition(i)
                if (selectedItem3 in checkIsNameRepeat) {
                    autoComplete3.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user3WhoWillPay = selectedItem3.toString()
                    checkIsNameRepeat.add(user3WhoWillPay.toString())
                    autoComplete3.isEnabled = false
                }
            }
        autoComplete4.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem4 = adapterView.getItemAtPosition(i)
                if (selectedItem4 in checkIsNameRepeat) {
                    autoComplete4.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user4WhoWillPay = selectedItem4.toString()
                    checkIsNameRepeat.add(user4WhoWillPay.toString())
                    autoComplete4.isEnabled = false
                }
            }
        autoComplete5.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem5 = adapterView.getItemAtPosition(i)
                if (selectedItem5 in checkIsNameRepeat) {
                    autoComplete5.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user5WhoWillPay = selectedItem5.toString()
                    checkIsNameRepeat.add(user5WhoWillPay.toString())
                    autoComplete5.isEnabled = false
                }
            }
        autoComplete6.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem6 = adapterView.getItemAtPosition(i)
                if (selectedItem6 in checkIsNameRepeat) {
                    autoComplete6.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user6WhoWillPay = selectedItem6.toString()
                    checkIsNameRepeat.add(user6WhoWillPay.toString())
                    autoComplete6.isEnabled = false
                }
            }
        var getHowMuchSpecialPrice = findViewById<EditText>(R.id.editTextNumber)
        var getHowMuchSpecialPrice2 = findViewById<EditText>(R.id.editTextNumber2)
        var getHowMuchSpecialPrice3 = findViewById<EditText>(R.id.editTextNumber3)
        var getHowMuchSpecialPrice4 = findViewById<EditText>(R.id.editTextNumber4)
        var getHowMuchSpecialPrice5 = findViewById<EditText>(R.id.editTextNumber5)
        var getHowMuchSpecialPrice6 = findViewById<EditText>(R.id.editTextNumber6)

        var whowillPaySeperateCounter = 0
        var deleteAllSelected = findViewById<ImageView>(R.id.deleteAllSpecialPricesBtn)
        deleteAllSelected.setOnClickListener {
            autoComplete.text.clear()
            autoComplete2.text.clear()
            autoComplete3.text.clear()
            autoComplete4.text.clear()
            autoComplete5.text.clear()
            autoComplete6.text.clear()

            checkIsNameRepeat.clear()
            user1WhoWillPay = ""
            user2WhoWillPay = ""
            user3WhoWillPay = ""
            user4WhoWillPay = ""
            user5WhoWillPay = ""
            user6WhoWillPay = ""
            autoComplete.isEnabled = true
            autoComplete2.isEnabled = true
            autoComplete3.isEnabled = true
            autoComplete4.isEnabled = true
            autoComplete5.isEnabled = true
            autoComplete6.isEnabled = true


            whowillPaySeperateCounter = 0
            Toast.makeText(this@AddBillActivity, "Temizlendi", Toast.LENGTH_SHORT).show()
        }
        savetheBill.setOnClickListener {
            var whoHowmuch = ArrayList<WhoHowmuch>()

            val price1 = getHowMuchSpecialPrice.text.toString().toDoubleOrNull() ?: null
            val price2 = getHowMuchSpecialPrice2.text.toString().toDoubleOrNull() ?: null
            val price3 = getHowMuchSpecialPrice3.text.toString().toDoubleOrNull() ?: null
            val price4 = getHowMuchSpecialPrice4.text.toString().toDoubleOrNull() ?: null
            val price5 = getHowMuchSpecialPrice5.text.toString().toDoubleOrNull() ?: null
            val price6 = getHowMuchSpecialPrice6.text.toString().toDoubleOrNull() ?: null
            var howmuch = 0.0
            if (addPrice.text.toString().isBlank()) {
                Toast.makeText(
                    this@AddBillActivity,
                    "Lütfen boş alanları doldurunuz.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            howmuch = addPrice.text.toString().toDouble()
            var totalPrice = 0.0

            if (user1WhoWillPay?.isBlank() == false && price1 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user1WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user1WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price1,
                            howmuch,
                            billName.text.toString(),
                            "",
                            snapKeyOfGroup
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price1 + totalPrice
            }
            if (user2WhoWillPay?.isBlank() == false && price2 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user2WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user2WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price2,
                            howmuch,
                            billName.text.toString(),
                            "",
                            snapKeyOfGroup

                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price2 + totalPrice
            }
            if (user3WhoWillPay?.isBlank() == false && price3 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user3WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user3WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price3,
                            howmuch,
                            billName.text.toString(),
                            "",
                            snapKeyOfGroup
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price3 + totalPrice

            }
            if (user4WhoWillPay?.isBlank() == false && price4 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user4WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user4WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price4,
                            howmuch,
                            billName.text.toString()
                            ,
                            "",
                            snapKeyOfGroup
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price4 + totalPrice
            }
            if (user5WhoWillPay?.isBlank() == false && price5 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user5WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user5WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price5,
                            howmuch,
                            billName.text.toString()
                            ,
                            "",
                            snapKeyOfGroup
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price5 + totalPrice
            }
            if (user6WhoWillPay?.isBlank() == false && price6 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user6WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user6WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price6,
                            howmuch,
                            billName.text.toString(),
                            "",
                            snapKeyOfGroup
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price6 + totalPrice
            }


            var ref = database.getReference("Bills")
            var auth = Firebase.auth
            if (billName.text.toString().isBlank()) {
                Toast.makeText(
                    this@AddBillActivity,
                    "Lütfen fatura ismini giriniz.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (addPrice.text.toString().isBlank()) {
                Toast.makeText(
                    this@AddBillActivity,
                    "Fatura fiyatı boş olmamalıdır.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (!billName.text.toString().isBlank() && !addPrice.text.toString().isBlank()) {
                if (addPrice.text.toString().toInt() < 0) {
                    Toast.makeText(
                        this@AddBillActivity,
                        "Fatura tutarı 1den küçük olamaz.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                var getBillName =
                    findViewById<TextView>(R.id.bill_name_et)
                if (groupUsers.size == 1) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            groupUsers[0].name + " " + groupUsers[0].surname,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            addPrice.text.toString().toDouble(),
                            howmuch,
                            billName.text.toString(),
                            "",
                            snapKeyOfGroup
                        )
                    )
                    var getKey = refOfBills.push().key
                    println(getKey.toString())
                    whoHowmuch.add(
                        WhoHowmuch(
                            "",
                            groupName,
                            2,
                            "",
                            0.0,
                            0.0,
                            billName.text.toString(),
                            getKey.toString(),
                            snapKeyOfGroup
                        )
                    )
                    refOfBills.child(getKey.toString()).setValue(whoHowmuch)
                        .addOnSuccessListener {
                            if (getSelectedImage?.isBlank() == false) {
                                var storageRef = Firebase.storage.getReference(billName.text.toString())
                                storageRef.child(getKey.toString())
                                    .putFile(Uri.parse(getSelectedImage)).addOnSuccessListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(
                                                this@AddBillActivity,
                                                "Fatura başarıyla eklendi.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }
                                    }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(
                                        this@AddBillActivity,
                                        "Fatura başarıyla eklendi.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        }
                }

                var cleanandSplitted = howmuch - totalPrice
                var willHowMuchPay: Double = 0.0
                if (groupUsers.size - whowillPaySeperateCounter - 1 != 0) {
                    willHowMuchPay =
                        cleanandSplitted / ((groupUsers.size + 1) - whowillPaySeperateCounter - 1)
                }
                var getNamesFromData = mutableListOf<String>()
                for (a in groupUsers) {
                    if (a.mail != auth.currentUser?.email) {
                        getNamesFromData.add("${a.name} ${a.surname}")
                    }
                }
                var getNamesFromwhoHowMuchWillPay = mutableListOf<String>()
                for (a in whoHowmuch) {
                    getNamesFromwhoHowMuchWillPay.add("${a.whoWillPay}")
                }
                for (temp in getNamesFromwhoHowMuchWillPay) {
                    if (temp in getNamesFromData) {

                    } else {
                        whoHowmuch.add(
                            WhoHowmuch(
                                temp,
                                groupName,
                                0,
                                auth.currentUser?.email,
                                willHowMuchPay,
                                howmuch,
                                billName.text.toString(),
                                "",
                                snapKeyOfGroup
                            )
                        )
                    }
                }
                for (a in getNamesFromData) {
                    if (a in getNamesFromwhoHowMuchWillPay) {
                    } else {
                        whoHowmuch.add(
                            WhoHowmuch(
                                a,
                                groupName,
                                0,
                                auth.currentUser?.email,
                                willHowMuchPay,
                                howmuch,
                                billName.text.toString(),
                                "",
                                snapKeyOfGroup
                            )
                        )
                    }
                }
                var getOldBill = ArrayList<WhoHowmuch>()
                var isBillNameActive = 0
                var counterSave = 0
                firstListener = (object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                if (snap.exists()) {
                                    for (rsnap in snap.children) {
                                        var temp = rsnap.getValue(WhoHowmuch::class.java)
                                        if (groupName == temp?.groupName) {
                                            getOldBill.add(temp!!)
                                        }
                                    }
                                }
                            }
                        }
                        for (a in getOldBill) {
                            if (a.billname == getBillName.text.toString() && isBillNameActive == 0) {
                                isBillNameActive = 1
                            }
                        }
                        if (isBillNameActive == 0) {
                            var getKey = refOfBills.push().key
                            whoHowmuch.add(
                                WhoHowmuch(
                                    "", groupName, 2, "",
                                    0.0, 0.0, billName.text.toString(), getKey.toString(),snapKeyOfGroup
                                )
                            )
                            refOfBills.child(getKey.toString()).setValue(whoHowmuch)
                            if (getSelectedImage?.isBlank() == false) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    progressBar.visibility = View.VISIBLE
                                }
                                var storageRef =
                                    Firebase.storage.getReference("${billName.text.toString()}")
                                storageRef.child(getKey.toString())
                                    .putFile(Uri.parse(getSelectedImage))
                                    .addOnSuccessListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(
                                                this@AddBillActivity,
                                                "Başarıyla kaydedildi.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            progressBar.visibility = View.GONE
                                            finish()
                                        }
                                    }
                                    .addOnFailureListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Toast.makeText(
                                                this@AddBillActivity,
                                                "Fotoğraf kaydedilemedi.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            progressBar.visibility = View.GONE
                                        }
                                    }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(
                                        this@AddBillActivity,
                                        "Başarıyla kaydedildi.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressBar.visibility = View.GONE
                                    finish()
                                }
                            }
                            isBillNameActive = 1
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                refOfBills.addValueEventListener(firstListener)
            }
        }

        var backToDetailsOfGroupActivityBtn = findViewById<ImageView>(R.id.backToGroupbtn)
        backToDetailsOfGroupActivityBtn.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        refOfBills = database.getReference("Bills")
        if (::firstListener.isInitialized)
            refOfBills.removeEventListener(firstListener)
        super.onDestroy()
    }

    private fun loadAds(groupName: String) {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.toString()?.let { Log.d(TAG, it) }
                    var mainscreen = findViewById<ConstraintLayout>(R.id.bigAddbillfragmentConst)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    println("Ad Loaded")
                    mInterstitialAd = interstitialAd
                    showAds(groupName)
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
}

package com.yucox.splitwise.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.R.R.model.Group
import com.R.R.model.UserInfo
import com.R.R.model.WhoHowmuch
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.R
import com.yucox.splitwise.databinding.AddBillActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AddBillActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    private val database = FirebaseDatabase.getInstance()
    private var refOfBills = database.getReference("Bills")

    private lateinit var binding: AddBillActivityBinding
    private val groupRef = database.getReference("Groups")
    private var groupUsers = ArrayList<Group>()
    private val refGroupUsersInfo = database.getReference("UsersData")
    private var getonlyNameAndSurname = mutableListOf<String>()
    private var groupUsersInfo = ArrayList<UserInfo>()
    private val auth = FirebaseAuth.getInstance()
    private var specialpriceBorderCounter = 1
    lateinit var mAdView: AdView
    private var user1WhoWillPay: String? = ""
    private var selectedItem1: Any? = null
    private var user2WhoWillPay: String? = ""
    private var selectedItem2: Any? = null
    private var user3WhoWillPay: String? = ""
    private var selectedItem3: Any? = null
    private var user4WhoWillPay: String? = ""
    private var selectedItem4: Any? = null
    private var user5WhoWillPay: String? = ""
    private var selectedItem5: Any? = null
    private var user6WhoWillPay: String? = ""
    private var selectedItem6: Any? = null
    private val checkIsNameRepeat = mutableListOf<String>()
    private var whowillPaySeperateCounter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddBillActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupName = intent.getStringExtra("groupName")
        val snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")

        MobileAds.initialize(this) {}
        initBanner()

        getGroupUsersFromData(snapKeyOfGroup)

        val calendar = Calendar.getInstance()

        var getSelectedImage: String? = ""
        val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    var selectedImageUri = data?.data
                    var smallBillPhoto = findViewById<ImageView>(R.id.smallBillPhoto)
                    smallBillPhoto.setImageURI(selectedImageUri)
                    getSelectedImage = selectedImageUri.toString()
                } else {
                    Toast.makeText(
                        this@AddBillActivity, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        binding.addPhotoToBillBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }

        showSpecialPartArea()
        addSpecialPriceMore()

        selectSpecialPriceUsers()

        clearAllSelectedPrice()

        binding.saveBillBtn.setOnClickListener {
            val whoHowmuch = ArrayList<WhoHowmuch>()
            val price1 = binding.specialPriceInput1.text.toString().toDoubleOrNull()
            val price2 = binding.specialPriceInput2.text.toString().toDoubleOrNull()
            val price3 = binding.specialPriceInput3.text.toString().toDoubleOrNull()
            val price4 = binding.specialPriceInput4.text.toString().toDoubleOrNull()
            val price5 = binding.specialPriceInput5.text.toString().toDoubleOrNull()
            val price6 = binding.specialPriceInput6.text.toString().toDoubleOrNull()
            var howmuch = 0.0

            if (isBillPriceEmpty() == 0) {
                return@setOnClickListener
            }

            howmuch = binding.priceEt.text.toString().toDouble()
            var totalPrice = 0.0

            if (user1WhoWillPay?.isBlank() == false && price1 != null) {
                var cleanName: String? = ""
                for (user in groupUsers) {
                    if (user1WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user1WhoWillPay
                    }
                }
                if (!cleanName.isNullOrEmpty()) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price1,
                            howmuch,
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price1 + totalPrice
            }
            if (user2WhoWillPay?.isBlank() == false && price2 != null) {
                var cleanName: String? = ""
                for (user in groupUsers) {
                    if (user2WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user2WhoWillPay
                    }
                }
                if (!cleanName.isNullOrEmpty()) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price2,
                            howmuch,
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time

                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price2 + totalPrice
            }
            if (user3WhoWillPay?.isBlank() == false && price3 != null) {
                var cleanName: String? = ""
                for (user in groupUsers) {
                    if (user3WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user3WhoWillPay
                    }
                }
                if (!cleanName.isNullOrEmpty()) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price3,
                            howmuch,
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price3 + totalPrice

            }
            if (user4WhoWillPay?.isBlank() == false && price4 != null) {
                var cleanName: String? = ""
                for (user in groupUsers) {
                    if (user4WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user4WhoWillPay
                    }
                }
                if (!cleanName.isNullOrEmpty()) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price4,
                            howmuch,
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price4 + totalPrice
            }
            if (user5WhoWillPay?.isBlank() == false && price5 != null) {
                var cleanName: String? = ""
                for (user in groupUsers) {
                    if (user5WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user5WhoWillPay
                    }
                }
                if (!cleanName.isNullOrEmpty()) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price5,
                            howmuch,
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price5 + totalPrice
            }
            if (user6WhoWillPay?.isBlank() == false && price6 != null) {
                var cleanName: String? = ""
                for (user in groupUsers) {
                    if (user6WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user6WhoWillPay
                    }
                }
                if (!cleanName.isNullOrEmpty()) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price6,
                            howmuch,
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price6 + totalPrice
            }

            if (checkEmptyAreas() == 1) {
                Toast.makeText(
                    this@AddBillActivity, getString(R.string.fill_empty_areas), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (isPriceBiggerThanO() == 0) {
                Toast.makeText(
                    this@AddBillActivity, R.string.MustBeBigger, Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (groupUsersInfo.size == 1) {
                saveForOnePerson(
                    whoHowmuch,
                    groupName,
                    howmuch,
                    snapKeyOfGroup,
                    calendar,
                    getSelectedImage
                )
            }

            var cleanandSplitted = howmuch - totalPrice
            var willHowMuchPay: Double = 0.0
            if (groupUsersInfo.size - whowillPaySeperateCounter - 1 != 0) {
                willHowMuchPay =
                    cleanandSplitted / ((groupUsersInfo.size + 1) - whowillPaySeperateCounter - 1)
            }
            val getNamesFromData = mutableListOf<String>()
            for (a in groupUsersInfo) {
                if (a.mail != auth.currentUser?.email) {
                    getNamesFromData.add("${a.name} ${a.surname}")
                }
            }
            val getNamesFromwhoHowMuchWillPay = mutableListOf<String>()
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
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time
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
                            binding.billNameEt.text.toString(),
                            "",
                            snapKeyOfGroup,
                            calendar.time
                        )
                    )
                }
            }
            val getOldBill = ArrayList<WhoHowmuch>()
            var isBillNameActive = 0
            var counterSave = 0
            refOfBills.addListenerForSingleValueEvent(object : ValueEventListener {
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
                        if (a.billname == binding.billNameEt.text.toString() && isBillNameActive == 0) {
                            isBillNameActive = 1
                        }
                    }
                    if (isBillNameActive == 0) {
                        var getKey = refOfBills.push().key
                        whoHowmuch.add(
                            WhoHowmuch(
                                "",
                                groupName,
                                2,
                                "",
                                0.0,
                                0.0,
                                binding.billNameEt.text.toString(),
                                getKey.toString(),
                                snapKeyOfGroup,
                                calendar.time
                            )
                        )
                        refOfBills.child(getKey.toString()).setValue(whoHowmuch)
                        if (getSelectedImage?.isBlank() == false) {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progressBar2.visibility = View.VISIBLE
                            }
                            Firebase.storage.getReference(getKey.toString())
                                .putFile(Uri.parse(getSelectedImage)).addOnSuccessListener {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(
                                            this@AddBillActivity,
                                            "Başarıyla kaydedildi.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding.progressBar2.visibility = View.GONE
                                        finish()
                                    }
                                }.addOnFailureListener {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(
                                            this@AddBillActivity,
                                            "Fotoğraf kaydedilemedi.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding.progressBar2.visibility = View.GONE
                                    }
                                }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    this@AddBillActivity,
                                    "Başarıyla kaydedildi.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.progressBar2.visibility = View.GONE
                                finish()
                            }
                        }
                        isBillNameActive = 1
                    } else {
                        val rootView = findViewById<View>(android.R.id.content)
                        Snackbar.make(
                            rootView,
                            getString(R.string.ChoiceDifferentName),
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Tamam") {}.show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        binding.backToGroupbtn.setOnClickListener {
            finish()
        }
    }

    private fun saveForOnePerson(
        whoHowmuch: ArrayList<WhoHowmuch>,
        groupName: String?,
        howmuch: Double,
        snapKeyOfGroup: String?,
        calendar: Calendar,
        getSelectedImage: String?
    ) {

        whoHowmuch.add(
            WhoHowmuch(
                groupUsersInfo[0].name + " " + groupUsersInfo[0].surname,
                groupName,
                0,
                auth.currentUser?.email,
                binding.priceEt.text.toString().toDouble(),
                howmuch,
                binding.billNameEt.text.toString(),
                "",
                snapKeyOfGroup,
                calendar.time
            )
        )
        var getKey = refOfBills.push().key
        whoHowmuch.add(
            WhoHowmuch(
                "",
                groupName,
                2,
                "",
                0.0,
                0.0,
                binding.billNameEt.text.toString(),
                getKey.toString(),
                snapKeyOfGroup,
                calendar.time
            )
        )
        refOfBills.child(getKey.toString()).setValue(whoHowmuch).addOnSuccessListener {
            if (getSelectedImage?.isBlank() == false) {
                var storageRef = Firebase.storage.getReference(getKey.toString())
                storageRef.putFile(Uri.parse(getSelectedImage)).addOnSuccessListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@AddBillActivity,
                            getString(R.string.BillAddedSuccessfully), Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@AddBillActivity,
                        getString(R.string.BillAddedSuccessfully), Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun isPriceBiggerThanO(): Int {
        if (binding.priceEt.text.toString().toInt() < 0) {
            return 0
        }
        return 1
    }

    private fun checkEmptyAreas(): Int {
        if (binding.billNameEt.text.toString().isBlank() || binding.priceEt.text.toString()
                .isBlank()
        ) {
            return 1
        }
        return 0
    }

    private fun isBillPriceEmpty(): Int {
        if (binding.priceEt.text.toString().isBlank()) {
            Toast.makeText(
                this@AddBillActivity, R.string.fill_empty_areas, Toast.LENGTH_SHORT
            ).show()
            return 0
        }
        return 1
    }

    private fun selectSpecialPriceUsers() {
        binding.selectSpecialPriceUser1.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem1 = adapterView.getItemAtPosition(i)
                if (selectedItem1 in checkIsNameRepeat) {
                    binding.selectSpecialPriceUser1.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        getString(R.string.onlyOneTimeYouCanPick),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user1WhoWillPay = selectedItem1.toString()
                    checkIsNameRepeat.add(user1WhoWillPay.toString())
                    binding.selectSpecialPriceUser1.isEnabled = false
                }
            }
        binding.selectSpecialPriceUser2.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem2 = adapterView.getItemAtPosition(i)
                if (selectedItem2 in checkIsNameRepeat) {
                    binding.selectSpecialPriceUser2.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        getString(R.string.onlyOneTimeYouCanPick),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user2WhoWillPay = selectedItem2.toString()
                    checkIsNameRepeat.add(user2WhoWillPay.toString())
                    binding.selectSpecialPriceUser2.isEnabled = false
                }
            }
        binding.selectSpecialPriceUser3.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem3 = adapterView.getItemAtPosition(i)
                if (selectedItem3 in checkIsNameRepeat) {
                    binding.selectSpecialPriceUser3.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        getString(R.string.onlyOneTimeYouCanPick),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user3WhoWillPay = selectedItem3.toString()
                    checkIsNameRepeat.add(user3WhoWillPay.toString())
                    binding.selectSpecialPriceUser3.isEnabled = false
                }
            }
        binding.selectSpecialPriceUser4.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem4 = adapterView.getItemAtPosition(i)
                if (selectedItem4 in checkIsNameRepeat) {
                    binding.selectSpecialPriceUser4.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        getString(R.string.onlyOneTimeYouCanPick),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user4WhoWillPay = selectedItem4.toString()
                    checkIsNameRepeat.add(user4WhoWillPay.toString())
                    binding.selectSpecialPriceUser4.isEnabled = false
                }
            }
        binding.selectSpecialPriceUser5.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                selectedItem5 = adapterView.getItemAtPosition(i)
                if (selectedItem5 in checkIsNameRepeat) {
                    binding.selectSpecialPriceUser5.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        getString(R.string.onlyOneTimeYouCanPick),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user5WhoWillPay = selectedItem5.toString()
                    checkIsNameRepeat.add(user5WhoWillPay.toString())
                    binding.selectSpecialPriceUser5.isEnabled = false
                }
            }
        binding.selectSpecialPriceUser6.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem6 = adapterView.getItemAtPosition(i)
                if (selectedItem6 in checkIsNameRepeat) {
                    binding.selectSpecialPriceUser6.text.clear()
                    Toast.makeText(
                        this@AddBillActivity,
                        getString(R.string.onlyOneTimeYouCanPick),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user6WhoWillPay = selectedItem6.toString()
                    checkIsNameRepeat.add(user6WhoWillPay.toString())
                    binding.selectSpecialPriceUser6.isEnabled = false
                }
            }
    }

    private fun clearAllSelectedPrice() {
        binding.deleteAllSpecialPricesBtn.setOnClickListener {
            binding.selectSpecialPriceUser1.text.clear()
            binding.selectSpecialPriceUser2.text.clear()
            binding.selectSpecialPriceUser3.text.clear()
            binding.selectSpecialPriceUser5.text.clear()
            binding.selectSpecialPriceUser4.text.clear()

            binding.specialPriceInput1.text.clear()
            binding.specialPriceInput2.text.clear()
            binding.specialPriceInput3.text.clear()
            binding.specialPriceInput4.text.clear()
            binding.specialPriceInput5.text.clear()
            binding.specialPriceInput6.text.clear()

            checkIsNameRepeat.clear()
            user1WhoWillPay = ""
            user2WhoWillPay = ""
            user3WhoWillPay = ""
            user4WhoWillPay = ""
            user5WhoWillPay = ""
            user6WhoWillPay = ""
            binding.selectSpecialPriceUser1.isEnabled = true
            binding.selectSpecialPriceUser2.isEnabled = true
            binding.selectSpecialPriceUser3.isEnabled = true
            binding.selectSpecialPriceUser4.isEnabled = true
            binding.selectSpecialPriceUser5.isEnabled = true
            binding.selectSpecialPriceUser6.isEnabled = true

            whowillPaySeperateCounter = 0
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView, "Temizlendi", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initBanner() {
        mAdView = findViewById(R.id.adView4)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun addSpecialPriceMore() {
        binding.addmoreSpecialPriceBtn.setOnClickListener {
            specialpriceBorderCounter++
            if (specialpriceBorderCounter == 2) {
                binding.cake2.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 3) {
                binding.cake3.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 4) {
                binding.partofSpecialLinear2.visibility = View.VISIBLE
                binding.cake4.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 5) {
                binding.cake5.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 6) {
                binding.cake6.visibility = View.VISIBLE
            }
        }
    }

    private fun showSpecialPartArea() {
        binding.specialPriceForEveryPersonBtn.setOnClickListener {
            if (binding.specialpriceLinear.visibility == View.VISIBLE) {
                binding.specialpriceLinear.visibility = View.GONE
            } else {
                binding.specialpriceLinear.visibility = View.VISIBLE
            }
        }
    }


    private fun getGroupUsersInfo(snapKeyOfGroup: String?) {
        refGroupUsersInfo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var i = 0
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue(UserInfo::class.java)
                        for (user in groupUsers.distinct()) {
                            if (user?.email == temp?.mail && user.snapKeyOfGroup == snapKeyOfGroup) {
                                if (user.email != auth.currentUser?.email) {
                                    getonlyNameAndSurname.add(i, "${user.name} ${user.surname}")
                                    i++
                                }
                                groupUsersInfo.add(temp!!)
                            }
                        }
                    }
                }
                initSelectAdapters()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initSelectAdapters() {
        val adapter = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter2 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter3 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter4 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter5 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)
        val adapter6 = ArrayAdapter(this@AddBillActivity, R.layout.list_item, getonlyNameAndSurname)

        binding.selectSpecialPriceUser1.setAdapter(adapter)
        binding.selectSpecialPriceUser2.setAdapter(adapter2)
        binding.selectSpecialPriceUser3.setAdapter(adapter3)
        binding.selectSpecialPriceUser4.setAdapter(adapter4)
        binding.selectSpecialPriceUser5.setAdapter(adapter5)
        binding.selectSpecialPriceUser6.setAdapter(adapter6)
    }

    private fun getGroupUsersFromData(snapKeyOfGroup: String?) {
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realsnap in snap.children) {
                                var temp = realsnap.getValue(Group::class.java)
                                if (temp?.snapKeyOfGroup == snapKeyOfGroup) {
                                    groupUsers.add(temp!!)
                                }
                            }
                        }
                    }
                }
                getGroupUsersInfo(snapKeyOfGroup)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun loadAds(groupName: String) {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,
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

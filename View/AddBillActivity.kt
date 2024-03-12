package com.yucox.splitwise.View

import android.content.Intent
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
import androidx.lifecycle.ViewModelProvider
import com.R.R.model.BillInfo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.yucox.splitwise.R
import com.yucox.splitwise.ViewModel.AddBillViewModel
import com.yucox.splitwise.databinding.AddBillActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class AddBillActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"

    private lateinit var binding: AddBillActivityBinding
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

    private lateinit var groupViewModel: AddBillViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddBillActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupName = intent.getStringExtra("groupName")
        val snapKeyOfGroup = intent.getStringExtra("snapKeyOfGroup")

        MobileAds.initialize(this) {}
        initBanner()

        groupViewModel = ViewModelProvider(this).get(AddBillViewModel::class.java)

        groupViewModel.setSnapKey(snapKeyOfGroup!!)
        groupViewModel.setGroupName(groupName!!)

        CoroutineScope(Dispatchers.Main).launch {
            if (!groupViewModel.fetchGroupUsers().await()) return@launch
            if (!groupViewModel.fetchUsersInfo().await()) return@launch
            initSelectAdapters()

        }

        val calendar = Calendar.getInstance()

        var getSelectedImage: String? = ""
        val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri = data?.data
                    val smallBillPhoto = findViewById<ImageView>(R.id.smallBillPhoto)
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
            val rootView = findViewById<View>(android.R.id.content)
            groupViewModel.checkBillName(binding.billNameEt.text.toString()) { it ->
                if (it) {
                    Snackbar.make(
                        rootView,
                        getString(R.string.ChoiceDifferentName),
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Tamam") {}.show()
                    return@checkBillName
                } else {
                    val price1 = binding.specialPriceInput1.text.toString().toDoubleOrNull()
                    val price2 = binding.specialPriceInput2.text.toString().toDoubleOrNull()
                    val price3 = binding.specialPriceInput3.text.toString().toDoubleOrNull()
                    val price4 = binding.specialPriceInput4.text.toString().toDoubleOrNull()
                    val price5 = binding.specialPriceInput5.text.toString().toDoubleOrNull()
                    val price6 = binding.specialPriceInput6.text.toString().toDoubleOrNull()
                    var howmuch = 0.0

                    val groupUsers = groupViewModel.getGroupUsers()
                    val usersInfo = groupViewModel.getUsersInfo()

                    if (isBillPriceEmpty() == 0)
                        return@checkBillName

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
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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
                        groupViewModel.incWhoWillPaySeperate()
                        totalPrice += price1
                    }
                    if (user2WhoWillPay?.isBlank() == false && price2 != null) {
                        var cleanName: String? = ""
                        for (user in groupUsers) {
                            if (user2WhoWillPay == "${user.name} ${user.surname}") {
                                cleanName = user2WhoWillPay
                            }
                        }
                        if (!cleanName.isNullOrEmpty()) {
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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
                        groupViewModel.incWhoWillPaySeperate()
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
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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
                        groupViewModel.incWhoWillPaySeperate()
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
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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
                        groupViewModel.incWhoWillPaySeperate()
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
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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
                        groupViewModel.incWhoWillPaySeperate()

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
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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
                        groupViewModel.incWhoWillPaySeperate()
                        totalPrice = price6 + totalPrice
                    }

                    if (checkEmptyAreas() == 1) {
                        Toast.makeText(
                            this@AddBillActivity,
                            getString(R.string.fill_empty_areas),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@checkBillName
                    }

                    if (isPriceBiggerThanO() == 0) {
                        Toast.makeText(
                            this@AddBillActivity, R.string.MustBeBigger, Toast.LENGTH_SHORT
                        ).show()
                        return@checkBillName
                    }

                    if (usersInfo.size == 1) {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (!groupViewModel.saveTheBillForOne(
                                    getSelectedImage.toString(),
                                    binding.billNameEt.text.toString(),
                                    binding.priceEt.text.toString(),
                                    calendar.time
                                ).await()
                            ) {
                                Snackbar.make(
                                    rootView, "Başarısız.", Snackbar.LENGTH_INDEFINITE
                                ).setAction("Tamam") {}.show()
                                binding.progressBar2.visibility = View.GONE
                                clearAllSelectedPrice()
                                return@launch
                            }
                            finish()
                            binding.progressBar2.visibility = View.GONE
                            return@launch
                        }
                        return@checkBillName
                    }

                    val cleanandSplitted = howmuch - totalPrice
                    var willHowMuchPay: Double = 0.0
                    if (usersInfo.size - groupViewModel.getWhoWillPaySeperate() - 1 != 0) {
                        willHowMuchPay =
                            cleanandSplitted / ((usersInfo.size + 1) - groupViewModel.getWhoWillPaySeperate() - 1)
                    }
                    val getNamesFromData = mutableListOf<String>()
                    for (a in usersInfo) {
                        if (a.mail != auth.currentUser?.email) {
                            getNamesFromData.add("${a.name} ${a.surname}")
                        }
                    }

                    val getNamesFromwhoHowMuchWillPay = mutableListOf<String>()
                    for (a in groupViewModel.getWhoWillPay()) {
                        getNamesFromwhoHowMuchWillPay.add("${a.whoWillPay}")
                    }

                    for (temp in getNamesFromwhoHowMuchWillPay) {
                        if (temp in getNamesFromData) {
                            continue
                        } else {
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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
                            continue
                        } else {
                            groupViewModel.setWhoWillPay(
                                BillInfo(
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

                    CoroutineScope(Dispatchers.Main).launch {
                        val rootView = findViewById<View>(android.R.id.content)
                        if (!groupViewModel.saveTheBill(
                                getSelectedImage.toString(),
                                binding.billNameEt.text.toString(),
                                calendar.time
                            ).await()
                        ) {
                            Snackbar.make(
                                rootView, "Başarısız.", Snackbar.LENGTH_INDEFINITE
                            ).setAction("Tamam") {}.show()
                            binding.progressBar2.visibility = View.GONE
                            clearAllSelectedPrice()
                            return@launch
                        }
                        finish()
                        return@launch

                    }
                }
            }

        }

        binding.backToGroupbtn.setOnClickListener {
            finish()
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

            groupViewModel.cleanWhoWillPaySeperate()

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


    private fun initSelectAdapters() {
        val getonlyNameAndSurname = groupViewModel.getNames()
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


    private fun loadAds(groupName: String) {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,
            "ca-app-pub-5841174734258930/8173377178",
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
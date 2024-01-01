package com.yucox.splitwise.activity


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yucox.splitwise.R
import com.R.R.model.Group
import com.R.R.model.UserInfo
import com.google.android.gms.auth.api.Auth
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.adapter.ListGroupAdapter
import com.yucox.splitwise.fragment.AddFriendFragment
import com.yucox.splitwise.fragment.SettingsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var listener2: ValueEventListener
    private var mInterstitialAd: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var mAdView: AdView
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        var hideNavBtn = findViewById<ImageView>(R.id.hideNavBtn)
        var auth = Firebase.auth

        loadAds()
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        adsProperties(mAdView)

        var storage = Firebase.storage.getReference(auth.currentUser?.email.toString())
        var getPfpBeforeGoToSettings: String? = null
        storage.downloadUrl.addOnSuccessListener { uri ->
            getPfpBeforeGoToSettings = uri.toString()
        }
        var getMainUserInfoForSettings = UserInfo()
        var userListRef = Firebase.database.getReference("UsersData")
        userListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue<com.R.R.model.UserInfo>()
                        if (temp?.mail == auth.currentUser?.email) {
                            getMainUserInfoForSettings.name = temp?.name
                            getMainUserInfoForSettings.surname = temp?.surname
                            getMainUserInfoForSettings.mail = temp?.mail
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        var addFriendbtn = findViewById<ImageView>(R.id.addFriendbtn)
        var settingsBtn = findViewById<ImageView>(R.id.settingsBtnmainactivity)

        var recyclerViewGroup = findViewById<RecyclerView>(R.id.GroupRecyclerView)
        var createnewGrouLinear = findViewById<LinearLayout>(R.id.createnewGroupLinear)
        var background = findViewById<ConstraintLayout>(R.id.constraintLayoutMain)
        var fragmentContainer = findViewById<View>(R.id.fragmentContainer)

        recyclerViewGroup.visibility = View.VISIBLE
        createnewGrouLinear.visibility = View.VISIBLE
        fragmentContainer.visibility = View.INVISIBLE

        var friendsBtn = findViewById<ImageView>(R.id.friendsBtn)
        friendsBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, FriendsActivity::class.java)
            startActivity(intent)
        }

        background.setOnClickListener {
            fragmentContainer.visibility = View.INVISIBLE
            recyclerViewGroup.visibility = View.VISIBLE
            createnewGrouLinear.visibility = View.VISIBLE
            hideNavBtn.visibility = View.GONE

            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = fragmentManager.findFragmentById(R.id.fragmentContainer)
            if (fragment != null) {
                fragmentTransaction.remove(fragment)
                fragmentTransaction.commit()
            }
            addFriendbtn.isClickable = true
            settingsBtn.isClickable = true
        }

        addFriendbtn.setOnClickListener {
            addFriendbtn.isClickable = false
            settingsBtn.isClickable = false
            recyclerViewGroup.visibility = View.INVISIBLE
            createnewGrouLinear.visibility = View.INVISIBLE
            val addFragment = AddFriendFragment()
            replaceFragment(addFragment)
            hideNavBtn.visibility = View.VISIBLE
        }

        var answerTheRequest = findViewById<ImageView>(R.id.acceptRequestBtn)
        answerTheRequest.setOnClickListener {
            val intent = Intent(this@MainActivity, AnswerFriendshipRequest::class.java)
            startActivity(intent)
        }

        settingsBtn.setOnClickListener {
            addFriendbtn.isClickable = false
            settingsBtn.isClickable = false
            recyclerViewGroup.visibility = View.INVISIBLE
            createnewGrouLinear.visibility = View.INVISIBLE
            val addFragment = SettingsFragment()
            val bundle = Bundle().apply {
                putString("pfp", getPfpBeforeGoToSettings)
                putString("name",getMainUserInfoForSettings.name)
                putString("surname",getMainUserInfoForSettings.surname)
                putString("mail",getMainUserInfoForSettings.mail)

            }
            addFragment.arguments = bundle
            replaceFragment(addFragment)
            hideNavBtn.visibility = View.VISIBLE

        }
        var createGroup = findViewById<ImageView>(R.id.createnewGroup)
        createGroup.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateGroup::class.java)
            startActivity(intent)
            finish()
        }

        var database = Firebase.database
        var groupRef = database.getReference("Groups")
        var getGroupInfo = ArrayList<Group>()
        var hashset = hashSetOf<String>()
        var whichGroupUserInSnapKeys = mutableListOf<String>()


        var groupAdapter =
            ListGroupAdapter(this@MainActivity, getGroupInfo, hashset, whichGroupUserInSnapKeys)

        recyclerViewGroup.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        recyclerViewGroup.adapter = groupAdapter

        var getGroupNames = HashSet<String>()
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(Group::class.java)
                                if (auth.currentUser?.email == temp?.email) {
                                    getGroupNames.add(temp?.GroupName.toString())
                                    whichGroupUserInSnapKeys.add(snap.key.toString())
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.key in whichGroupUserInSnapKeys) {
                            for (rsnap in snap.children) {
                                var a = rsnap.getValue(Group::class.java)
                                getGroupInfo.add(a!!)
                                hashset.add(a.GroupName.toString())
                            }
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    groupAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        hideNavBtn.visibility = View.GONE
        hideNavBtn.setOnClickListener {
            fragmentContainer.visibility = View.INVISIBLE
            recyclerViewGroup.visibility = View.VISIBLE
            createnewGrouLinear.visibility = View.VISIBLE
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = fragmentManager.findFragmentById(R.id.fragmentContainer)

            if (fragment != null) {
                fragmentTransaction.remove(fragment)
                fragmentTransaction.commit()
            }
            addFriendbtn.isClickable = true
            settingsBtn.isClickable = true
            hideNavBtn.visibility = View.GONE
        }
    }

    private fun adsProperties(mAdView: AdView?) {
        mAdView?.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
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

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        var fragmentContainer = findViewById<View>(R.id.fragmentContainer)
        fragmentContainer.visibility = View.VISIBLE
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        Toast.makeText(
            this@MainActivity, "Çıkmak için ana navigasyon butonuna basın.", Toast.LENGTH_SHORT
        ).show()
    }

    private fun showAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    val intent = Intent(this@MainActivity, CreateGroup::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    val intent = Intent(this@MainActivity, CreateGroup::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                }

            }
            mInterstitialAd?.show(this@MainActivity)
        } else {
            val intent = Intent(this@MainActivity, CreateGroup::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadAds() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this@MainActivity,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }
}

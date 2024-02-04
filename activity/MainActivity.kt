package com.yucox.splitwise.activity


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yucox.splitwise.R
import com.R.R.model.Group
import com.R.R.model.UserInfo
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.adapter.ListGroupAdapter
import com.yucox.splitwise.databinding.MainActivityBinding
import com.yucox.splitwise.fragment.AddFriendFragment
import com.yucox.splitwise.fragment.SettingsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var mAdView: AdView
    private var mInterstitialAd: InterstitialAd? = null
    private val auth = FirebaseAuth.getInstance()
    private var mainInfo = UserInfo()
    private var pfp: String? = null
    private val database = FirebaseDatabase.getInstance()
    private var getGroupInfo = ArrayList<Group>()
    private val groupRef = database.getReference("Groups")
    private var groupNamesHash = hashSetOf<String>()
    private var groupKeysAndNamesHashMap = hashMapOf<String, String>()
    private lateinit var listGroupAdapter : ListGroupAdapter

    private lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fragmentContainer.visibility = View.INVISIBLE

        loadAds()
        MobileAds.initialize(this) {}
        initBanner()
        adsProperties(mAdView)

        getAndSetUserInfos()
        openSettings()

        goToFriendsActivity()
        checkFriendshipRequest()
        searchFriend()

        createNewGroup()

        getKeysFromData()

        listenOutsideToHideFragment()
        //hideFragmentWButton()

        refreshToData()


    }

    private fun updateKeys() {
        val newgroupKeysAndNamesHashMap = HashMap<String, String>()
        newgroupKeysAndNamesHashMap.clear()

        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(Group::class.java)
                                if (auth.currentUser?.email == temp?.email) {
                                    newgroupKeysAndNamesHashMap.put(
                                        temp?.groupName!!,
                                        temp?.snapKeyOfGroup!!
                                    )
                                }
                            }
                        }
                    }
                }
                if (newgroupKeysAndNamesHashMap.size != groupKeysAndNamesHashMap.size) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    groupKeysAndNamesHashMap.clear()
                    groupKeysAndNamesHashMap = newgroupKeysAndNamesHashMap
                    getGroupInfo.clear()
                    groupNamesHash.clear()

                    updateGroups()
                } else {
                    binding.swipeRefreshLayout.isRefreshing = false

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateGroups() {
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.key in groupKeysAndNamesHashMap.values) {
                            for (rsnap in snap.children) {
                                var a = rsnap.getValue(Group::class.java)
                                getGroupInfo.add(a!!)
                                groupNamesHash.add(a.groupName.toString())
                            }
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    binding.swipeRefreshLayout.isRefreshing = false
                    listGroupAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun refreshToData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            updateKeys()
        }
    }


    private fun checkAndGetGroupNames() {
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.key in groupKeysAndNamesHashMap.values) {
                            for (rsnap in snap.children) {
                                var a = rsnap.getValue(Group::class.java)
                                getGroupInfo.add(a!!)
                                groupNamesHash.add(a.groupName.toString())
                            }
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    binding.swipeRefreshLayout.isRefreshing = false
                    initListGroupRecycler()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initListGroupRecycler() {
        listGroupAdapter = ListGroupAdapter(
            this@MainActivity,
            getGroupInfo,
            groupNamesHash,
            groupKeysAndNamesHashMap
        )
        binding.listGroupRv.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        binding.listGroupRv.adapter = listGroupAdapter
        listGroupAdapter.notifyDataSetChanged()
    }

    private fun getKeysFromData() {
        binding.swipeRefreshLayout.isRefreshing = true
        groupRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realSnap in snap.children) {
                                var temp = realSnap.getValue(Group::class.java)
                                if (auth.currentUser?.email == temp?.email) {
                                    groupKeysAndNamesHashMap.put(
                                        temp?.groupName!!,
                                        temp?.snapKeyOfGroup!!
                                    )
                                }
                            }
                        }
                    }
                }
                checkAndGetGroupNames()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun createNewGroup() {
        binding.createNewGroupBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateGroup::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openSettings() {
        binding.settingsBtn.setOnClickListener {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView,"Kapatmak için çerçeve dışında herhangi bir yere tıklayın.",Snackbar.LENGTH_LONG).show()
            binding.searchPeopleBtn.isClickable = false
            binding.settingsBtn.isClickable = false
            binding.listGroupRv.visibility = View.INVISIBLE
            binding.adView.visibility = View.GONE
            val addFragment = SettingsFragment()
            val bundle = Bundle().apply {
                putString("pfp", pfp)
                putString("name", mainInfo.name)
                putString("surname", mainInfo.surname)
                putString("mail", mainInfo.mail)

            }
            addFragment.arguments = bundle
            replaceFragment(addFragment)
            //binding.hideSettingsBtn.visibility = View.VISIBLE

        }
    }

    private fun listenOutsideToHideFragment() {
        binding.backgroundConst.setOnClickListener {
            binding.fragmentContainer.visibility = View.INVISIBLE
            binding.listGroupRv.visibility = View.VISIBLE
            binding.adView.visibility = View.VISIBLE
            //binding.hideSettingsBtn.visibility = View.GONE

            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = fragmentManager.findFragmentById(R.id.fragmentContainer)
            if (fragment != null) {
                fragmentTransaction.remove(fragment)
                fragmentTransaction.commit()
            }
            binding.searchPeopleBtn.isClickable = true
            binding.settingsBtn.isClickable = true
        }
    }

    private fun searchFriend() {
        binding.searchPeopleBtn.setOnClickListener {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView,"Kapatmak için çerçeve dışında herhangi bir yere tıklayın.",Snackbar.LENGTH_LONG).show()
            binding.searchPeopleBtn.isClickable = false
            binding.settingsBtn.isClickable = false
            binding.listGroupRv.visibility = View.INVISIBLE
            val addFragment = AddFriendFragment()
            replaceFragment(addFragment)
            //binding.hideSettingsBtn.visibility = View.VISIBLE
            binding.adView.visibility = View.GONE
        }
    }

    private fun checkFriendshipRequest() {
        binding.checkFriendRequestBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, AnswerFriendshipRequest::class.java)
            startActivity(intent)
        }
    }

    private fun goToFriendsActivity() {
        binding.myFriendsBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, FriendsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getAndSetUserInfos() {
        var storage = Firebase.storage.getReference(auth.currentUser?.email.toString())
        storage.downloadUrl.addOnSuccessListener { uri ->
            pfp = uri.toString()
        }
        var userListRef = Firebase.database.getReference("UsersData")
        userListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        var temp = snap.getValue<com.R.R.model.UserInfo>()
                        if (temp?.mail == auth.currentUser?.email) {
                            mainInfo.name = temp?.name
                            mainInfo.surname = temp?.surname
                            mainInfo.mail = temp?.mail
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initBanner() {
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
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
        if (binding.fragmentContainer.visibility == View.VISIBLE) {
            binding.fragmentContainer.visibility = View.INVISIBLE
            binding.listGroupRv.visibility = View.VISIBLE
            binding.adView.visibility = View.VISIBLE
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = fragmentManager.findFragmentById(R.id.fragmentContainer)

            if (fragment != null) {
                fragmentTransaction.remove(fragment)
                fragmentTransaction.commit()
            }
            binding.searchPeopleBtn.isClickable = true
            binding.settingsBtn.isClickable = true
        } else {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(
                rootView, "Çıkmak için home tuşunu kullanın", Snackbar.LENGTH_LONG
            ).show()
        }

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
            "ca-app-pub-5",
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

    /*private fun hideFragmentWButton() {
        binding.hideSettingsBtn.visibility = View.GONE
        binding.hideSettingsBtn.setOnClickListener {
            binding.fragmentContainer.visibility = View.INVISIBLE
            binding.listGroupRv.visibility = View.VISIBLE
            binding.adView.visibility = View.VISIBLE
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fragment = fragmentManager.findFragmentById(R.id.fragmentContainer)

            if (fragment != null) {
                fragmentTransaction.remove(fragment)
                fragmentTransaction.commit()
            }
            binding.searchPeopleBtn.isClickable = true
            binding.settingsBtn.isClickable = true
            binding.hideSettingsBtn.visibility = View.GONE
        }
    }*/
}

package com.yucox.splitwise.View


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.yucox.splitwise.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hieupt.android.standalonescrollbar.attachTo
import com.yucox.splitwise.Adapter.ListGroupAdapter
import com.yucox.splitwise.databinding.MainActivityBinding
import com.yucox.splitwise.Fragment.SearchFragment
import com.yucox.splitwise.Fragment.SettingsFragment
import com.yucox.splitwise.ViewModel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    lateinit var mAdView: AdView
    val auth = FirebaseAuth.getInstance()
    private lateinit var listGroupAdapter: ListGroupAdapter
    private lateinit var mainViewModel: MainViewModel

    private lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val mainUserMail = auth.currentUser?.email.toString()
        mainViewModel.fetchMainUserProfile(mainUserMail)

        CoroutineScope(Dispatchers.Main).launch {
            binding.swipeRefreshLayout.isRefreshing = true
            if (!mainViewModel.fetchGroups(mainUserMail).await()) {
                binding.swipeRefreshLayout.isRefreshing = false
                return@launch
            }
            initListGroupRecycler()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.profileBtn.setOnClickListener {
            openProfileFragment()
        }

        binding.myFriendsBtn.setOnClickListener {
            goToFriendsActivity()
        }

        binding.checkFriendRequestBtn.setOnClickListener {
            checkFriendshipRequest()
        }

        binding.searchPeopleBtn.setOnClickListener {
            addFriendFragment()
        }
        binding.createNewGroupBtn.setOnClickListener {
            createNewGroup()
        }

        binding.backgroundConst.setOnClickListener {
            hideFragment()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData(mainUserMail)
        }

        MobileAds.initialize(this) {}
        initBanner()
    }

    private fun refreshData(mainUserMail: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.swipeRefreshLayout.isRefreshing = true
            val result = mainViewModel.reFetchGroups(mainUserMail)
            if (!result.await()) {
                binding.swipeRefreshLayout.isRefreshing = false
                return@launch
            }
            mainViewModel.resetVariables()
            if (mainViewModel.fetchGroups(mainUserMail).await())
                listGroupAdapter.notifyDataSetChanged()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun initListGroupRecycler() {
        val groupList = mainViewModel.getGroupList()
        val groupNames = mainViewModel.getGroupNames()
        val keyAndNameMap = mainViewModel.getKeyAndNameMap()
        listGroupAdapter = ListGroupAdapter(
            this@MainActivity,
            groupList,
            groupNames,
            keyAndNameMap
        )
        binding.listGroupRv.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        binding.listGroupRv.adapter = listGroupAdapter
        binding.scrollbar.attachTo(binding.listGroupRv)
    }


    private fun createNewGroup() {
        val intent = Intent(this@MainActivity, CreateGroup::class.java)
        startActivity(intent)
        finish()

    }

    private fun openProfileFragment() {
        val mainUser = mainViewModel.getMainUser()
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(
            rootView,
            "Kapatmak için çerçeve dışında herhangi bir yere tıklayın.",
            Snackbar.LENGTH_SHORT
        ).show()
        binding.searchPeopleBtn.isClickable = false
        binding.profileBtn.isClickable = false
        binding.listGroupRv.visibility = View.INVISIBLE
        binding.adView.visibility = View.GONE
        val addFragment = SettingsFragment()
        val bundle = Bundle().apply {
            putString("pfp", mainUser.pfpUri)
            putString("name", mainUser.name)
            putString("surname", mainUser.surname)
            putString("mail", mainUser.mail)

        }
        addFragment.arguments = bundle
        replaceFragment(addFragment)
    }

    private fun hideFragment() {
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
        binding.profileBtn.isClickable = true
    }


    private fun addFriendFragment() {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(
            rootView,
            "Kapatmak için çerçeve dışında herhangi bir yere tıklayın.",
            Snackbar.LENGTH_SHORT
        ).show()
        binding.searchPeopleBtn.isClickable = false
        binding.profileBtn.isClickable = false
        binding.listGroupRv.visibility = View.INVISIBLE
        val addFragment = SearchFragment()
        replaceFragment(addFragment)
        binding.adView.visibility = View.GONE

    }

    private fun checkFriendshipRequest() {
        val intent = Intent(this@MainActivity, AnswerFriendshipRequest::class.java)
        startActivity(intent)

    }

    private fun goToFriendsActivity() {
        val intent = Intent(this@MainActivity, FriendsActivity::class.java)
        startActivity(intent)

    }


    private fun initBanner() {
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
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
            binding.profileBtn.isClickable = true
        } else {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(
                rootView, "Çıkmak için home tuşunu kullanın", Snackbar.LENGTH_LONG
            ).show()
        }

    }

}
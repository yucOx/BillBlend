
class MainActivity : AppCompatActivity() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var listener2 : ValueEventListener
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var mAdView : AdView
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        loadAds()

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
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
                println("hata")
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                println("yüklendi")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
        var text = findViewById<TextView>(R.id.tellMeHowMuch)
        text.visibility = View.GONE
        refreshData()
        var addFriendbtn = findViewById<ImageView>(R.id.addFriendbtn)
        var settingsBtn = findViewById<ImageView>(R.id.settingsBtnmainactivity)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        var recyclerViewGroup = findViewById<RecyclerView>(R.id.GroupRecyclerView)
        var createnewGrouLinear = findViewById<LinearLayout>(R.id.createnewGroupLinear)
        var background = findViewById<ConstraintLayout>(R.id.constraintLayoutMain)
        var fragmentContainer = findViewById<View>(R.id.fragmentContainer)

        recyclerViewGroup.visibility = View.VISIBLE
        createnewGrouLinear.visibility = View.VISIBLE


        var friendsBtn = findViewById<ImageView>(R.id.friendsBtn)
        friendsBtn.setOnClickListener {
            val intent = Intent(this@MainActivity,FriendsActivity::class.java)
            startActivity(intent)
        }

        background.setOnClickListener{
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

        }

        addFriendbtn.setOnClickListener {
            addFriendbtn.isClickable = false
            settingsBtn.isClickable = false
            recyclerViewGroup.visibility = View.INVISIBLE
            createnewGrouLinear.visibility = View.INVISIBLE
            val addFragment = AddFriendFragment()
                replaceFragment(addFragment)
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
                replaceFragment(addFragment)
        }



        var createGroup = findViewById<ImageView>(R.id.createnewGroup)
        createGroup.setOnClickListener {
            showAds()
        }

        var database = Firebase.database

        var groupRef = database.getReference("Groups")

        var auth = Firebase.auth

        var getGroupInfo = ArrayList<Group>()
        var hashset = hashSetOf<String>()

        var groupAdapter = ListGroupAdapter(this@MainActivity, getGroupInfo, hashset)

        recyclerViewGroup.layoutManager =
            LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
        recyclerViewGroup.adapter = groupAdapter

        var getNameOfUser: String? = ""
        var getGroupNames = HashSet<String>()

        var listener1 = groupRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for(realSnap in snap.children) {
                                var temp = realSnap.getValue(Group::class.java)
                                if(auth.currentUser?.email == temp?.email){
                                    getGroupNames.add(temp?.GroupName.toString())
                                }
                            }
                        }
                    }
                }
            groupRef.addValueEventListener(listener2)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        var checkerHashSet = HashSet<String>()
        listener2 = groupRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.exists()){
                            for(rsnap in snap.children){
                                var temp = rsnap.getValue(Group::class.java)
                                if(temp?.GroupName in getGroupNames){
                                    getGroupInfo.add(Group(temp?.groupOwner,temp?.GroupName,temp?.name,temp?.surname,temp?.email))
                                    hashset.add(temp?.GroupName.toString())
                                }
                            }
                        }
                    }
                }
                groupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
            hashset.clear()
            getGroupInfo.clear()
            groupRef.addValueEventListener(listener1)
        }
        Firebase.storage.getReference(auth.currentUser?.email.toString()).downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(this@MainActivity).load(uri).into(settingsBtn)
            }.addOnFailureListener{
                Glide.with(this@MainActivity).load(R.drawable.bojackprofile_asset).into(settingsBtn)
            }
    }

    private fun refreshData() {
        var database = Firebase.database
        var ref = database.getReference("FriendRequest")
        var auth = FirebaseAuth.getInstance()
        var counter = 0
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                       var a = snap.getValue(SendFriendRequest::class.java)
                        if(auth.currentUser?.email == a?.whoGetFriendRequest && a?.status == 0){
                            counter++
                        }
                    }
                updateNotification(counter)
                }
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun updateNotification(count : Int){
        var text = findViewById<TextView>(R.id.tellMeHowMuch)
        if(count > 0){
            text.visibility = View.VISIBLE
            text.text = "$count"
        }else{
            text.visibility = View.GONE
        }
    }

    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        var fragmentContainer = findViewById<View>(R.id.fragmentContainer)
        fragmentContainer.visibility = View.VISIBLE
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        Toast.makeText(this@MainActivity,"Çıkmak için ana navigasyon butonuna basın.",Toast.LENGTH_SHORT).show()
    }
    private fun showAds(){
        if(mInterstitialAd != null){
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback(){
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
                    val intent = Intent(this@MainActivity,CreateGroup::class.java)
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
        }else{
            val intent = Intent(this@MainActivity,CreateGroup::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun loadAds(){
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this@MainActivity,"**********************", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }
}

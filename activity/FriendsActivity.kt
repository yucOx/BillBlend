
class FriendsActivity : AppCompatActivity() {
    private lateinit var adapter : FriendAdapter
    private lateinit var listener : ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friends_activity)

        var doyouHaveFriend = findViewById<TextView>(R.id.doyouHave)
        doyouHaveFriend.visibility = View.GONE

        var mailOfFriends = HashSet<String>()

        var refUsersData = Firebase.database.getReference("UsersData")
        var ref = Firebase.database.getReference("FriendRequest")
        ref.addValueEventListener(object : ValueEventListener{
            var counter = 0
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue(SendFriendRequest::class.java)
                        if( Firebase.auth.currentUser?.email == temp?.whoSentFriendRequest){
                            if(temp?.status == 1){
                                mailOfFriends.add(temp?.whoGetFriendRequest.toString())
                                counter++
                            }
                        }else if(Firebase.auth.currentUser?.email == temp?.whoGetFriendRequest ){
                            if(temp?.status == 1) {
                                mailOfFriends.add(temp?.whoSentFriendRequest.toString())
                                counter++
                            }
                        }
                    }
                }
                if(counter == 0){
                    doyouHaveFriend.visibility = View.VISIBLE
                    doyouHaveFriend.text = "Ekli hiçbir arkadaşınız yok."
                }
                refUsersData.addListenerForSingleValueEvent(listener)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        var userInfos = ArrayList<UserInfo>()
        var hashControl = HashSet<String>()
        listener = ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var userList = ArrayList(mailOfFriends)
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue(UserInfo::class.java)
                        if(temp?.mail in userList){
                            if(temp?.mail in hashControl){

                            }else{
                                userInfos.add(temp!!)
                            }
                            hashControl.add(temp?.mail.toString())
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        adapter = FriendAdapter(this@FriendsActivity,mailOfFriends,userInfos)
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerFriend)
        recyclerView.layoutManager = LinearLayoutManager(this@FriendsActivity,RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapter

    }

    override fun onBackPressed() {
        finish()
    }
}

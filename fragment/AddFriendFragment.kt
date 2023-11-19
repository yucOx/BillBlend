

class AddFriendFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lateinit var afterDataCheck : Runnable
        val view =  inflater.inflate(R.layout.add_friend_fragment, container, false)

        /*val cancelBtn = view.findViewById<ImageView>(R.id.cancelButton)
        cancelBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }*/

        var profileUri : String? = ""
        var firebaseStorage = FirebaseStorage.getInstance()
        var database = Firebase.database
        var ref = database.getReference("UsersData")
        var userList = ArrayList<UserInfo>()


        var randomPfp = ArrayList<Int>()
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.tesla)

        var adapter = SearchAdapter(requireContext(),userList,randomPfp)
        var recyclerView = view.findViewById<RecyclerView>(R.id.listSavedUsers)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapter

        var getNames = ArrayList<String>()
        var getMails = ArrayList<String>()
        var searchResult = view.findViewById<EditText>(R.id.searchingAreaFrg)
        var getTempUsers = ArrayList<UserInfo>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                if(snapshot.exists()){
                    for(snap in snapshot.children) {
                        var tempUser = snap.getValue(UserInfo::class.java)
                        if(tempUser?.mail != Firebase.auth.currentUser?.email){
                            getTempUsers.add(tempUser!!)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        var searchBtn = view.findViewById<ImageView>(R.id.searchBtnFrg)
        searchBtn.setOnClickListener {
            userList.clear()
            var getsearchResult = searchResult.text.toString().lowercase().trim()
            if(!getsearchResult.isBlank() && getTempUsers != null){
                for(a in getTempUsers){
                    var b = a.name?.lowercase()
                    if(b?.contains(getsearchResult) == true){
                        userList.add(a!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }else{
                Toast.makeText(context,"Aratılacak kişiyi giriniz",Toast.LENGTH_SHORT).show()
                userList.clear()
                adapter.notifyDataSetChanged()
            }
        }
        return view
    }
}

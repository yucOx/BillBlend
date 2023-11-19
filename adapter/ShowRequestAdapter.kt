
class ShowRequestAdapter(private val context: Context, private var userList: ArrayList<UserInfo>, var randomImg : ArrayList<Int>) :
    RecyclerView.Adapter<ShowRequestAdapter.ViewHolder>() {
    lateinit var randomImgShuffled : List<Int>
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pfp = view.findViewById<CircleImageView>(R.id.profileShowRequestItem)
        var name =  view.findViewById<TextView>(R.id.nameShowRequestItem)
        var surname = view.findViewById<TextView>(R.id.surnameShowRequestItem)
        var mail = view.findViewById<TextView>(R.id.mailShowRequestItem)
        var linearLayout = view.findViewById<LinearLayout>(R.id.selectLinearShowRequest)
        var acceptBtn = view.findViewById<ImageView>(R.id.acceptRequestShowRequestItem)
        var rejectBtn = view.findViewById<ImageView>(R.id.refuseRequestShowRequestItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.show_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var database = Firebase.database
        var ref = database.getReference("FriendRequest")

        val item = userList[position]
        holder.name.text = item.name.toString()
        holder.surname.text = item.surname.toString()
        holder.mail.text = item.mail.toString()
        holder.acceptBtn.setOnClickListener {
            var auth = FirebaseAuth.getInstance()
            var database = Firebase.database
            var ref = database.getReference("FriendRequest")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(snap in snapshot.children){
                            var a = snap.getValue(SendFriendRequest::class.java)
                            if(a?.whoSentFriendRequest == item.mail && a?.whoGetFriendRequest == Firebase.auth.currentUser?.email){
                                var uniqueId = snap.key.toString()
                                ref.child(uniqueId).child("status").setValue(1)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
        holder.rejectBtn.setOnClickListener {
            var database = Firebase.database
            var ref = database.getReference("FriendRequest")
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Arkadaşlık isteğini reddetmek istediğine emin misin?")
            builder.setNegativeButton("Evet") { dialog, which ->
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                var a = snap.getValue(SendFriendRequest::class.java)
                                if (a?.whoSentFriendRequest == item.mail && a?.whoGetFriendRequest == Firebase.auth.currentUser?.email) {
                                    var uniqueId = snap.key.toString()
                                    ref.child(uniqueId).removeValue()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
            builder.setPositiveButton("Hayır"){dialog,which->}.show()
        }
            if(counter == 0) {
            randomImgShuffled = randomImg.shuffled()
            counter++
        }
        Firebase.storage.getReference(item.mail.toString()).downloadUrl
            .addOnSuccessListener {uri->
                Glide.with(context).load(uri).into(holder.pfp)
            }.addOnFailureListener{
                Glide.with(context).load(randomImgShuffled[position]).into(holder.pfp)
            }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}


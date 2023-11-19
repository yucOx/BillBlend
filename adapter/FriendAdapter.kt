
class FriendAdapter(private val context: Context, private var friendsMail : HashSet<String>,var friendsInfo : ArrayList<UserInfo>) :
    RecyclerView.Adapter<FriendAdapter.ViewHolder>() {
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var pfp = view.findViewById<CircleImageView>(R.id.pfpUser)
        var name =  view.findViewById<TextView>(R.id.nameUserItem)
        var surname = view.findViewById<TextView>(R.id.surnameUserItem)
        var mail = view.findViewById<TextView>(R.id.mailUserItem)
        var unfriendBtn = view.findViewById<ImageView>(R.id.unfriendBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.friendsitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(friendsInfo.isNotEmpty()) {
            holder.name.text = friendsInfo[position].name
            holder.surname.text = friendsInfo[position].surname
            holder.mail.text = friendsInfo[position].mail
        }
        if(friendsInfo.isNotEmpty()) {
            if(holder.mail.text == friendsInfo[position].mail) {
                if(!(context as Activity).isFinishing) {
                    var refStorage =
                        Firebase.storage.getReference(friendsInfo[position].mail.toString()).downloadUrl
                            .addOnSuccessListener { uri ->
                                Glide.with(context).load(uri).into(holder.pfp)
                            }.addOnFailureListener {
                                Glide.with(context).load(R.drawable.dostoyevski).into(holder.pfp)
                            }
                }
            }
        }
        holder.unfriendBtn.setOnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Arkadaşlıktan çıkarmak istediğinize emin misiniz?")
            builder.setNegativeButton("Evet"){dialog,which ->
                var ref = Firebase.database.getReference("FriendRequest")
                ref.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(snap in snapshot.children){
                                if(snap.child("whoGetFriendRequest").getValue() == friendsInfo[position].mail && snap.child("whoSentFriendRequest").getValue() ==
                                    Firebase.auth.currentUser?.email){
                                    var key = snap.key
                                    ref.child(key.toString()).removeValue()
                                    Toast.makeText(context,"Arkadaşlıktan çıkarıldı.",Toast.LENGTH_LONG).show()
                                    (context as Activity).finish()
                                }else if(snap.child("whoSentFriendRequest").getValue() == friendsInfo[position].mail && snap.child("whoGetFriendRequest").getValue() ==
                                    Firebase.auth.currentUser?.email){
                                    var key = snap.key
                                    ref.child(key.toString()).removeValue()
                                    Toast.makeText(context,"Arkadaşlıktan çıkarıldı.",Toast.LENGTH_LONG).show()
                                    (context as Activity).finish()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }.setPositiveButton("Hayır") {dialog,which -> }.show()
        }
    }

    override fun getItemCount(): Int {
        return friendsInfo.size
    }
}

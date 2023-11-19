
class ProfileDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_detail_activity)

        var name = intent.getStringExtra("name")
        var surname = intent.getStringExtra("surname")
        var mail = intent.getStringExtra("mail")

        var nameText = findViewById<TextView>(R.id.name)
        var surnameText = findViewById<TextView>(R.id.surname)
        var mailText = findViewById<TextView>(R.id.mail)
        var profilePic = findViewById<CircleImageView>(R.id.profilepic)
        var deleteFriend = findViewById<ImageView>(R.id.deleteFriend)
        var isFriend = findViewById<TextView>(R.id.isFriend)
        var addFriend = findViewById<ImageView>(R.id.addFriend)
        deleteFriend.visibility = View.GONE
        addFriend.visibility = View.GONE

        if(name?.isNotEmpty() == true){
            nameText.text = name
        }
        if(surname?.isNotEmpty() == true){
            surnameText.text = surname
        }
        if(mail?.isNotEmpty() == true){
            mailText.text = mail

        }

        var ref = Firebase.database.getReference("FriendRequest")
        var storageRef = Firebase.storage.getReference(mail.toString()).downloadUrl
            .addOnSuccessListener {uri ->
                if(!this.isFinishing){
                    Glide.with(this@ProfileDetailActivity).load(uri).into(profilePic)
                }
            }
            .addOnFailureListener {
                if (!this.isFinishing) {
                    Glide.with(this@ProfileDetailActivity).load(R.drawable.splitwisecat).into(profilePic)
                }
            }

        var controler = 0
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue(SendFriendRequest::class.java)
                        if((temp?.whoSentFriendRequest == mail && temp?.status == 1 && Firebase.auth.currentUser?.email
                                    == temp?.whoGetFriendRequest) || (temp?.whoGetFriendRequest == mail && temp?.status == 1 &&
                                Firebase.auth.currentUser?.email == temp?.whoSentFriendRequest)){
                            if(Firebase.auth.currentUser?.email != mail) {
                                deleteFriend.visibility = View.VISIBLE
                                isFriend.text = "Arkadaşsınız"
                                controler = 1
                            }
                            if(Firebase.auth.currentUser?.email == mail){
                                isFriend.text = "Siz"
                                controler = 1
                            }
                        }
                    }
                }
                if(controler == 0) {
                    if (mail != Firebase.auth.currentUser?.email) {
                        addFriend.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        deleteFriend.setOnClickListener {
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Arkadaşlıktan çıkarmak istediğinize emin misiniz?")
                .setNegativeButton("Evet"){dialog,which ->
                    ref.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                for(snap in snapshot.children){
                                    var temp = snap.getValue(SendFriendRequest::class.java)
                                    if(temp?.whoGetFriendRequest == mail || temp?.whoSentFriendRequest == mail){
                                        var key = snap.key
                                        ref.child(key.toString()).removeValue()
                                            .addOnSuccessListener {
                                                Toast.makeText(this@ProfileDetailActivity,"Arkadaş başarıyla silindi.",Toast.LENGTH_SHORT).show()
                                                deleteFriend.visibility = View.GONE
                                                addFriend.visibility = View.VISIBLE
                                                isFriend.text = "Arkadaş değilsiniz."
                                            }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
                .setPositiveButton("Hayır"){dialog,which ->

                }.show()
        }
        addFriend.setOnClickListener {
            var sendFriendRequest = SendFriendRequest()
            sendFriendRequest.whoSentFriendRequest = Firebase.auth.currentUser?.email
            sendFriendRequest.whoGetFriendRequest = mail
            sendFriendRequest.status = 0
            var checker = 0
                ref.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var check = 0
                        if(snapshot.exists()){
                            for(snap in snapshot.children){
                                var a = snap.getValue(SendFriendRequest::class.java)
                                if (a != null) {
                                    if(sendFriendRequest.whoSentFriendRequest == a.whoSentFriendRequest.toString() && sendFriendRequest.whoGetFriendRequest
                                        == a.whoGetFriendRequest.toString() && a.status == 0){
                                        check = 1
                                    }else if(sendFriendRequest.whoSentFriendRequest == a.whoSentFriendRequest.toString() && sendFriendRequest.whoGetFriendRequest
                                        == a.whoGetFriendRequest.toString() && a.status == 1) {
                                        check = 2
                                    }
                                    else if(sendFriendRequest.whoSentFriendRequest != a.whoSentFriendRequest && sendFriendRequest.whoGetFriendRequest != a.whoGetFriendRequest){
                                        check = 0
                                    }

                                }
                            }
                            if(check == 0){
                                ref.push().setValue(sendFriendRequest)
                                Toast.makeText(this@ProfileDetailActivity,"Başarıyla istek gönderildi!",Toast.LENGTH_LONG).show()
                            }else if(check == 1){
                                Toast.makeText(this@ProfileDetailActivity,"Daha önceden istek gönderildi.",Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}

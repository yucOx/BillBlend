
class BillDetailsActivity : AppCompatActivity() {
    private lateinit var groupName : String
    private lateinit var adapter : BillDetailsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bill_details_activity)

        var billName = intent.getStringExtra("billName")
        var billImg = intent.getStringExtra("billImgUri")
        groupName = intent.getStringExtra("groupName").toString()
        println(billName)

        var photoOftheBill = findViewById<ImageView>(R.id.photoOfBill)
        if(billImg?.isBlank() == false){
            Glide.with(this@BillDetailsActivity).load(Uri.parse(billImg)).into(photoOftheBill)
        }
        var billNameText = findViewById<TextView>(R.id.nameOfBill)
        billNameText.text = billName

        var database = FirebaseDatabase.getInstance()
        var ref = database.getReference("Bills")

        var getBillDetails = ArrayList<WhoHowmuch>()
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.exists()){
                            for(rSnap in snap.children) {
                                var temp = rSnap.getValue(WhoHowmuch::class.java)
                                if(temp?.billname == billName) {
                                    println(temp?.whoWillPay)
                                    println(temp?.billname)
                                    getBillDetails.add(temp!!)
                                }
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        adapter = BillDetailsAdapter(this@BillDetailsActivity,getBillDetails)
        var recyclerView = findViewById<RecyclerView>(R.id.recycler_billdetails)
        recyclerView.layoutManager = LinearLayoutManager(this@BillDetailsActivity,RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapter



        var deleteToGroup = findViewById<ImageView>(R.id.deleteGroup)
        deleteToGroup.setOnClickListener {
            if(Firebase.auth.currentUser?.email == getBillDetails[0].whoBought) {
                var builder = AlertDialog.Builder(this@BillDetailsActivity)
                builder.setTitle("Faturayı silmek istediğinizden emin misiniz?")
                builder.setNegativeButton("Evet"){dialog,which->
                    var dataRef = Firebase.database.getReference("Bills")
                    dataRef.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                for(snap in snapshot.children){
                                    for(rsnap in snap.children) {
                                        var temp = rsnap.getValue(WhoHowmuch::class.java)
                                        if (temp?.billname == getBillDetails[0].billname) {
                                            var uniqueId = snap.key.toString()
                                            println(uniqueId)
                                            dataRef.child(uniqueId).removeValue()
                                                .addOnSuccessListener {
                                                    Toast.makeText(this@BillDetailsActivity, "Fatura başarıyla silindi!",Toast.LENGTH_SHORT).show()
                                            }
                                            for(a in getBillDetails) {
                                                var storage = Firebase.storage.getReference("${groupName}").child(billName.toString()).child(a.whoBought.toString())
                                                storage.delete().addOnSuccessListener {
                                                    if(!isFinishing()){
                                                        var intent = Intent(this@BillDetailsActivity,DetailsOfGroupActivity::class.java)
                                                        intent.putExtra("GroupName",groupName)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }.addOnFailureListener{
                                                    if(!isFinishing){
                                                        var intent = Intent(this@BillDetailsActivity,
                                                            DetailsOfGroupActivity::class.java)
                                                        intent.putExtra("GroupName",groupName)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            getBillDetails.clear()
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }.setPositiveButton("Hayır"){dialog,which ->

                }.show()
            }else{
                Toast.makeText(this@BillDetailsActivity,"Sadece fatura sahibi, faturayı silebilir.", Toast.LENGTH_SHORT).show()
            }
        }
        var showTheBill = findViewById<CircleImageView>(R.id.showTheBill)
        var hideTheBill = findViewById<CircleImageView>(R.id.hideTheBill)
        showTheBill.visibility = View.GONE
        hideTheBill.setOnClickListener {
            photoOftheBill.visibility = View.GONE
            hideTheBill.visibility = View.GONE
            showTheBill.visibility = View.VISIBLE
        }
        showTheBill.setOnClickListener{
            hideTheBill.visibility = View.VISIBLE
            photoOftheBill.visibility = View.VISIBLE
            showTheBill.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@BillDetailsActivity,DetailsOfGroupActivity::class.java)
        intent.putExtra("GroupName",groupName)
        startActivity(intent)
        finish()
    }
}

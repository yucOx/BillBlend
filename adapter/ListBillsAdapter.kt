
class ListBillsAdapter(private val context: Context, private var billNames: ArrayList<String>, var groupName : String ? = "", var billPrice : HashSet<String>,var groupOwner : HashSet<String>) :
    RecyclerView.Adapter<ListBillsAdapter.ViewHolder>() {
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var smallPhotoOfBill = view.findViewById<CircleImageView>(R.id.smallPhotoOfBill)
        var billName = view.findViewById<TextView>(R.id.billNameForRecycler)
        var selectLinear = view.findViewById<LinearLayout>(R.id.selectLinear)
        var price = view.findViewById<TextView>(R.id.priceOfBill)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listbills_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = billNames[position]
        holder.billName.text = item

        var priceAndBillname = ArrayList<WhoHowmuch>()
        var priceRef = Firebase.database.getReference("Bills")

        var handler = android.os.Handler()
        var runnable : Runnable = Runnable(){}
        priceRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.exists()){
                            for(resnap in snap.children){
                                var temp = resnap.child("billname").getValue(String::class.java)
                                var temp2 = resnap.child("howmuchWillpay").getValue()
                                if(temp in billNames){
                                    var getTemp = resnap.getValue(WhoHowmuch::class.java)
                                    priceAndBillname.add(getTemp!!)
                                }
                            }
                        }
                    }
                }
            handler.post{
                if(billNames.isNotEmpty() && priceAndBillname.isNotEmpty() && billNames.size > position) {
                    for(a in priceAndBillname){
                        if (holder.billName.text == a.billname) {
                            holder.price.text = "${a.totalPrice}₺"
                        }
                    }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        var imgUri : String? = ""
        for(a in groupOwner){
            var Firebasestorage = FirebaseStorage.getInstance()
            var ref = Firebasestorage.getReference("$groupName").child(item).child(a)
                ref.downloadUrl.addOnSuccessListener { uri ->
                    imgUri = uri.toString()
                    if(!(context as Activity).isFinishing) {
                    Glide.with(context).load(uri).into(holder.smallPhotoOfBill)
                }
            }
        }


        holder.selectLinear.setOnClickListener {
            val intent = Intent(context, BillDetailsActivity::class.java)
            intent.putExtra("billName",billNames[position])
            intent.putExtra("groupName",groupName)
            if(imgUri?.isBlank() == false){
                intent.putExtra("billImgUri",imgUri)
            }
            context.startActivity(intent)
            (context as Activity).finish()
        }
    }

    override fun getItemCount(): Int {
        return billNames.size
    }
}

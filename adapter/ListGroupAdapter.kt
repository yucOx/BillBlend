
class ListGroupAdapter(private val context: Context, private var groupUsers: ArrayList<Group>, private var groupNames : HashSet<String>) :
    RecyclerView.Adapter<ListGroupAdapter.ViewHolder>() {
    lateinit var randomImgShuffled : List<Int>
    var counter = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var usersInGroup = view.findViewById<TextView>(R.id.usersInGroup)
        var groupName = view.findViewById<TextView>(R.id.GroupName)
        var img1 = view.findViewById<CircleImageView>(R.id.img1)
        var img2 = view.findViewById<CircleImageView>(R.id.img2)
        var img3 = view.findViewById<CircleImageView>(R.id.img3)
        var img4 = view.findViewById<CircleImageView>(R.id.img4)
        var img5 = view.findViewById<CircleImageView>(R.id.img5)
        var img6 = view.findViewById<CircleImageView>(R.id.img6)
        var img7 = view.findViewById<CircleImageView>(R.id.img7)
        var img8 = view.findViewById<CircleImageView>(R.id.img8)
        var selectGroup = view.findViewById<LinearLayout>(R.id.selectGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_group_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = groupUsers[position]
        var arrayListOfGroupNames = ArrayList<String>()
        for(a in groupNames){
            arrayListOfGroupNames.add(a)
        }
        holder.img1.visibility = View.GONE
        holder.img2.visibility = View.GONE
        holder.img3.visibility = View.GONE
        holder.img4.visibility = View.GONE
        holder.img5.visibility = View.GONE
        holder.img6.visibility = View.GONE
        holder.img7.visibility = View.GONE
        holder.img8.visibility = View.GONE

        var stringBuff : String? = ""
        var counter = 0
        var hashCheck = HashSet<String>()
        for(user in groupUsers) {
                if (arrayListOfGroupNames[position] == user.GroupName){
                    if(!("${user.name} ${user.surname}" in hashCheck)){
                        counter++
                        stringBuff += "${user.name} ${user.surname}\n"
                        hashCheck.add("${user.name} ${user.surname}")
                    }
            }
        }
        holder.usersInGroup.text = stringBuff
        holder.groupName.text = arrayListOfGroupNames[position]
        if(counter == 2){
            holder.img1.visibility = View.VISIBLE
            holder.img2.visibility = View.VISIBLE
        }
        if(counter == 3){
            holder.img1.visibility = View.VISIBLE
            holder.img2.visibility = View.VISIBLE
            holder.img3.visibility = View.VISIBLE
        }
        if(counter == 4){
            holder.img1.visibility = View.VISIBLE
            holder.img2.visibility = View.VISIBLE
            holder.img3.visibility = View.VISIBLE
            holder.img4.visibility = View.VISIBLE
        }
        if(counter == 5){
            holder.img1.visibility = View.VISIBLE
            holder.img2.visibility = View.VISIBLE
            holder.img3.visibility = View.VISIBLE
            holder.img4.visibility = View.VISIBLE
            holder.img5.visibility = View.VISIBLE
        }
        if(counter == 6){
            holder.img1.visibility = View.VISIBLE
            holder.img2.visibility = View.VISIBLE
            holder.img3.visibility = View.VISIBLE
            holder.img4.visibility = View.VISIBLE
            holder.img5.visibility = View.VISIBLE
            holder.img6.visibility = View.VISIBLE
        }

        holder.selectGroup.setOnClickListener{
            val intent = Intent(context, DetailsOfGroupActivity::class.java)
            var groupName = arrayListOfGroupNames[position]
            intent.putExtra("GroupName",groupName)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return groupNames.size
    }

}

package com.yucox.splitwise.Adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.R.R.model.Group
import com.yucox.splitwise.R
import com.yucox.splitwise.View.DetailsOfGroupActivity

class ListGroupAdapter(
    private val context: Context,
    private val groupUsers: ArrayList<Group>,
    private val groupNames: HashSet<String>,
    private val groupKeyAndNameHashMap: HashMap<String, String>
) :
    RecyclerView.Adapter<ListGroupAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usersInGroup = view.findViewById<TextView>(R.id.usersInGroup)
        val groupName = view.findViewById<TextView>(R.id.GroupName)
        val listPfpRecycler = view.findViewById<RecyclerView>(R.id.listPfpRecycler)
        val selectGroup = view.findViewById<ConstraintLayout>(R.id.selectGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_group_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val arrayListOfGroupNames = ArrayList<String>(groupNames)

        var stringBuff: String? = ""
        var counter = 0
        val hashCheck = HashSet<String>()
        val usersMail = HashSet<String>()
        for (user in groupUsers) {
            if (arrayListOfGroupNames[position] != user.groupName)
                continue

            if ("${user.name} ${user.surname}" in hashCheck)
                continue

            counter++
            stringBuff += "${user.name} ${user.surname}\n"
            hashCheck.add("${user.name} ${user.surname}")
            usersMail.add(user.email.toString())
        }

        holder.usersInGroup.text = stringBuff
        holder.groupName.text = arrayListOfGroupNames[position]
        initListPfpRecycler(usersMail.size, usersMail, holder.listPfpRecycler)


        holder.selectGroup.setOnClickListener {
            val intent = Intent(context, DetailsOfGroupActivity::class.java)
            val groupName = arrayListOfGroupNames[position]
            intent.putExtra("GroupName", groupName)
            intent.putExtra("snapKeyOfGroup", groupKeyAndNameHashMap[groupName])
            context.startActivity(intent)
        }
    }

    private fun initListPfpRecycler(
        counter: Int,
        usersMail: HashSet<String>,
        listPfpRecycler: RecyclerView
    ) {
        val adapter = ListPfpAdapter(context, usersMail, counter)
        listPfpRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        listPfpRecycler.adapter = adapter
    }

    override fun getItemCount(): Int {
        return groupNames.size
    }

}
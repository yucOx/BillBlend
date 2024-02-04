package com.yucox.splitwise.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.R.R.model.UserInfo
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.SearchAdapter
import com.yucox.splitwise.model.GetUserPhotoWithName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AddFriendFragment : Fragment() {
    private lateinit var adapter : SearchAdapter
    private val userList = ArrayList<UserInfo>()
    private val getUserPhotoWNameArray = ArrayList<GetUserPhotoWithName>()
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("UsersData")
    private val getTempUsers = ArrayList<UserInfo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_search_friend, container, false)

        val randomPfp = ArrayList<Int>()
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.tesla)

        initAdapter(view)

        getAllUsers()

        searchFriend(view)

        return view
    }

    private fun getAllUsers() {
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
            }
        })
    }

    private fun searchFriend(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.listSavedUsers)
        val searchResult = view.findViewById<EditText>(R.id.searchingAreaFrg)
        val searchBtn = view.findViewById<ImageView>(R.id.searchBtnFrg)

        searchBtn.setOnClickListener {
            userList.clear()
            getUserPhotoWNameArray.clear()
            recyclerView.removeAllViews()
            var getsearchResult = searchResult.text.toString().lowercase().trim()
            if(!getsearchResult.isBlank() && getTempUsers != null){
                for(a in getTempUsers){
                    Firebase.storage.getReference(a.mail.toString()).downloadUrl.addOnSuccessListener { uri ->
                        getUserPhotoWNameArray.add(GetUserPhotoWithName(uri.toString(),a.mail))
                        CoroutineScope(Dispatchers.Main).launch {
                            adapter.notifyDataSetChanged()
                        }
                    }.addOnFailureListener {
                        getUserPhotoWNameArray.add(GetUserPhotoWithName(null,a.mail))
                        CoroutineScope(Dispatchers.Main).launch {
                            adapter.notifyDataSetChanged()
                        }
                    }
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
    }

    private fun initAdapter(view: View) {
        adapter = SearchAdapter(requireContext(),userList,getUserPhotoWNameArray)
        val recyclerView = view.findViewById<RecyclerView>(R.id.listSavedUsers)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapter
    }
}
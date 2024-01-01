package com.yucox.splitwise.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.R.R.model.UserInfo
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.R
import com.yucox.splitwise.adapter.SearchAdapter
import com.yucox.splitwise.model.GetUserPhotoWithName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
        var getUserPhotoWNameArray = ArrayList<GetUserPhotoWithName>()


        var randomPfp = ArrayList<Int>()
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.tesla)

        var adapter = SearchAdapter(requireContext(),userList,getUserPhotoWNameArray)
        var recyclerView = view.findViewById<RecyclerView>(R.id.listSavedUsers)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        recyclerView.adapter = adapter

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
            }
        })

        var searchBtn = view.findViewById<ImageView>(R.id.searchBtnFrg)
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
        return view
    }
}
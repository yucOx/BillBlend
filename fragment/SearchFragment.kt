package com.yucox.splitwise.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.R.R.model.User
import com.yucox.splitwise.Adapter.FriendAdapter
import com.yucox.splitwise.R
import com.yucox.splitwise.Adapter.SearchAdapter
import com.yucox.splitwise.Model.PhotoAndMail
import com.yucox.splitwise.ViewModel.SearchViewModel
import com.yucox.splitwise.databinding.FragmentSearchFriendBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class SearchFragment : Fragment() {
    private var binding: FragmentSearchFriendBinding? = null
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var adapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchFriendBinding.inflate(inflater, container, false)
        searchViewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)

        searchViewModel.fetchUsers()

        binding!!.searchBtnFrg.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val searchedOne = binding!!.searchAreaEt.text.toString()

                if (!searchViewModel.searchUser(searchedOne).await()) {
                    Toast.makeText(context, "Aratılacak kişiyi giriniz", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                if (!searchViewModel.getUserPhoto().await())
                    return@launch

                refreshOrInitAdapter()
            }
        }
        return binding?.root
    }


    private fun refreshOrInitAdapter() {
        val queriedUsersList = searchViewModel.getQueryList()
        val photoAndMails = searchViewModel.getPhotoAndMails()

        if (queriedUsersList.size == 0) {
            queriedUsersList.add(User("Bu isimde veya\nmailde biri bulunamadı."))
            photoAndMails.add(PhotoAndMail(null, ""))
        }
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
            return
        }
        adapter = SearchAdapter(requireContext(), queriedUsersList, photoAndMails)
        binding!!.usersRecycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding!!.usersRecycler.adapter = adapter
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
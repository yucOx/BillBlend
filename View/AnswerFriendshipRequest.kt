package com.yucox.splitwise.View


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.R.R.model.User
import com.yucox.splitwise.R
import com.yucox.splitwise.ViewModel.FriendRequestViewModel
import com.yucox.splitwise.Adapter.ShowRequestAdapter
import com.yucox.splitwise.databinding.AnswerFriendshipRequestActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AnswerFriendshipRequest : AppCompatActivity() {
    private lateinit var adapter: ShowRequestAdapter
    private lateinit var friendViewModel: FriendRequestViewModel
    private lateinit var binding: AnswerFriendshipRequestActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AnswerFriendshipRequestActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendViewModel = ViewModelProvider(this).get(FriendRequestViewModel::class.java)

        fetchData(1)

        binding.refresh.setOnRefreshListener {
            friendViewModel.resetValues()
            fetchData(0)
            binding.refresh.isRefreshing = false
        }

        binding.backToMainFromRequest.setOnClickListener {
            finish()
        }
    }

    private fun fetchData(which: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            if (!friendViewModel.fetchFriendRequest().await())
                return@launch

            if (friendViewModel.getFriendRequestCount() <= 0)
                binding.howmuchRequestAnswerFriendship.text = "Arkadaşlık isteğiniz yok."

            if (!friendViewModel.fetchUsersDetail().await())
                return@launch
            if (which == 1)
                initAdapter()
            else
                adapter.notifyDataSetChanged()
        }
    }


    private fun initAdapter() {
        val friendInfo = friendViewModel.getRequestInfo()
        val userInfo = ArrayList<User>()
        for (user in friendInfo) {
            if (user.name?.isBlank() == true) {
                continue
            } else {
                userInfo.add(user)
            }
        }

        val randomPfp = ArrayList<Int>()
        randomPfp.add(R.drawable.luffy)
        randomPfp.add(R.drawable.neitzsche)
        randomPfp.add(R.drawable.rick)
        randomPfp.add(R.drawable.azizsancar)
        randomPfp.add(R.drawable.dostoyevski)
        randomPfp.add(R.drawable.kateguri)
        randomPfp.add(R.drawable.einstein)
        randomPfp.add(R.drawable.tesla)

        adapter = ShowRequestAdapter(this@AnswerFriendshipRequest, userInfo, randomPfp)
        binding.FriendshipRequestRecyclerView.layoutManager =
            LinearLayoutManager(this@AnswerFriendshipRequest, RecyclerView.VERTICAL, false)
        binding.FriendshipRequestRecyclerView.adapter = adapter

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
package com.yucox.splitwise.View


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.R.R.model.User
import com.yucox.splitwise.R
import com.yucox.splitwise.ViewModel.FriendsViewModel
import com.yucox.splitwise.Adapter.FriendAdapter
import com.yucox.splitwise.databinding.FriendsActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendsActivity : AppCompatActivity() {
    private lateinit var adapter: FriendAdapter
    private lateinit var binding: FriendsActivityBinding
    private lateinit var friendsViewModel: FriendsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FriendsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.swipeRefreshFr.isRefreshing = false

        friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            binding.swipeRefreshFr.isRefreshing = true
            if (!friendsViewModel.fetchMails().await()) {
                Toast.makeText(
                    this@FriendsActivity, R.string.try_later, Toast.LENGTH_LONG
                )
                    .show()
                binding.swipeRefreshFr.isRefreshing = false
                return@launch
            }
            if (friendsViewModel.areYouAlone()) {
                binding.friendStatusTv.visibility = View.VISIBLE
                binding.friendStatusTv.text = getString(R.string.alone_assfuck)
                binding.swipeRefreshFr.isRefreshing = false
                return@launch
            }
            if (!friendsViewModel.fetchFriends().await()) {
                Toast.makeText(this@FriendsActivity, R.string.try_later, Toast.LENGTH_LONG)
                    .show()
                binding.swipeRefreshFr.isRefreshing = false
                return@launch
            }

            binding.swipeRefreshFr.isRefreshing = false
            initAdapter(friendsViewModel.getMyFriends())
        }

        binding.swipeRefreshFr.setOnRefreshListener {
            anyOneNew()
        }

        binding.backToMainActivity.setOnClickListener {
            finish()
        }
    }

    private fun anyOneNew() {
        CoroutineScope(Dispatchers.Main).launch {
            if (!friendsViewModel.newFriendCheck().await()) {
                binding.swipeRefreshFr.isRefreshing = false
                return@launch
            }
            if (!friendsViewModel.fetchFriends().await()) {
                Toast.makeText(this@FriendsActivity, R.string.try_later, Toast.LENGTH_LONG)
                    .show()
                binding.swipeRefreshFr.isRefreshing = false
                return@launch
            }
            binding.swipeRefreshFr.isRefreshing = false
            adapter.notifyDataSetChanged()
        }
    }


    private fun initAdapter(friendList: ArrayList<User>) {
        adapter = FriendAdapter(this@FriendsActivity, friendList)
        binding.recyclerFriend.layoutManager =
            LinearLayoutManager(this@FriendsActivity, RecyclerView.VERTICAL, false)
        binding.recyclerFriend.adapter = adapter
    }


    override fun onRestart() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            binding.swipeRefreshFr.isRefreshing = true
            anyOneNew()
        }
        super.onRestart()
    }

    override fun onBackPressed() {
        finish()
    }
}
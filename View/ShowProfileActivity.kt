package com.yucox.splitwise.View


import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.splitwise.ViewModel.ShowProfileViewModel
import com.yucox.splitwise.databinding.ShowProfileActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var showProfileViewModel: ShowProfileViewModel
    private lateinit var binding: ShowProfileActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShowProfileActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val surname = intent.getStringExtra("surname")
        val intentMail = intent.getStringExtra("mail")
        val mailAndPicHashMap =
            intent.getSerializableExtra("mailAndPicHashMap") as HashMap<String, Uri>

        showProfileViewModel = ViewModelProvider(this).get(ShowProfileViewModel::class.java)

        setHaveGottenData(name, surname, intentMail)
        setProfilePhoto(mailAndPicHashMap, intentMail)

        CoroutineScope(Dispatchers.Main).launch {
            val result = showProfileViewModel.checkFriendStatus(intentMail.toString()).await()
            if (!result)
                return@launch

            showStatusOnScreen()
        }

        binding.addFriendBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = showProfileViewModel.sendRequest(intentMail.toString()).await()
                if (!result)
                    return@launch
                binding.addFriendBtn.visibility = View.GONE
                binding.unFriendBtn.visibility = View.VISIBLE
                showProfileViewModel.setStatus(0)
                Toast.makeText(this@ShowProfileActivity, "İstek gönderildi", Toast.LENGTH_LONG)
                    .show()
                showStatusOnScreen()
            }
        }

        binding.unFriendBtn.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            if (showProfileViewModel.getStatus() == 0) {
                builder.setTitle("İsteği iptal etmek mi istiyorsunuz?")
                    .setNegativeButton("Evet") { dialog, which ->
                        unfriend(intentMail)
                    }.setPositiveButton("Hayır") { dialog, which ->

                    }
                    .show()
            } else {
                builder.setTitle("Arkadaşlıktan çıkarmak istediğinize emin misiniz?")
                    .setNegativeButton("Evet") { dialog, which ->
                        unfriend(intentMail)
                    }.setPositiveButton("Hayır") { dialog, which ->

                    }
                    .show()
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showStatusOnScreen() {
        when (showProfileViewModel.getStatus()) {

            0 -> {
                binding.friendStatusTv.text = "İstek gönderildi"
                binding.unFriendBtn.visibility = View.VISIBLE
            }

            1 -> {
                binding.friendStatusTv.text = "Arkadaşsınız"
                binding.unFriendBtn.visibility = View.VISIBLE
            }

            2 -> binding.friendStatusTv.text = "Siz"

            -1 -> {
                binding.friendStatusTv.text = "Arkadaş değilsiniz"
                binding.addFriendBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun unfriend(mail: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = showProfileViewModel.unFriend(mail.toString()).await()
            if (!result)
                return@launch
            Toast.makeText(this@ShowProfileActivity, "Başarılı", Toast.LENGTH_SHORT)
                .show()
            binding.unFriendBtn.visibility = View.GONE
            binding.addFriendBtn.visibility = View.VISIBLE
            binding.friendStatusTv.text = "Arkadaş değilsiniz"
            showProfileViewModel.setStatus(-1)
        }
    }


    private fun setProfilePhoto(mailAndPicHashMap: HashMap<String, Uri>, mail: String?) {
        binding.mailTv.text = mail
        Glide.with(this).load(mailAndPicHashMap[mail]).into(binding.profilepic)
    }

    private fun setHaveGottenData(name: String?, surname: String?, mail: String?) {

        if (name?.isNotEmpty() == true) {
            binding.nameTv.text = name
        }
        if (surname?.isNotEmpty() == true) {
            binding.surnameTv.text = surname
        }
        if (mail?.isNotEmpty() == true) {
            binding.mailTv.text = mail
        }
    }
}
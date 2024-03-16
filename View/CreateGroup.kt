package com.yucox.splitwise.View


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.textfield.TextInputLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.yucox.splitwise.R
import com.yucox.splitwise.ViewModel.CreateGroupViewModel
import com.yucox.splitwise.databinding.CreateGroupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateGroup : AppCompatActivity() {
    private var selectedUsers = mutableListOf<String>()

    private lateinit var adapter3: ArrayAdapter<String>
    private lateinit var adapter4: ArrayAdapter<String>
    private lateinit var adapter5: ArrayAdapter<String>
    private lateinit var adapter6: ArrayAdapter<String>
    private lateinit var adapter7: ArrayAdapter<String>
    private lateinit var adapter8: ArrayAdapter<String>
    lateinit var mAdView: AdView
    private lateinit var binding: CreateGroupBinding
    private lateinit var createGroupViewModel: CreateGroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        initBanner()

        createGroupViewModel = ViewModelProvider(this).get(CreateGroupViewModel::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            if (!createGroupViewModel.fetchFriendsMail().await())
                return@launch
            if (!createGroupViewModel.fetchUsersInfoToCreateGroup().await())
                return@launch
            initSelectUserAdapters()
        }

        binding.saveBtn.setOnClickListener {
            val groupName = binding.selectGroupname.text.toString()

            if (groupName.isBlank())
                return@setOnClickListener
            var createResult = false
            if (selectedUsers.size > 0)
                createResult = createGroupViewModel.handleGroup(groupName, selectedUsers)
            if (selectedUsers.size == 0)
                createResult = createGroupViewModel.handleGroupForSingle(groupName)
            if (!createResult)
                return@setOnClickListener

            CoroutineScope(Dispatchers.Main).launch {
                if (createGroupViewModel.checkGroupName(groupName).await()) {
                    println("burada")
                    Toast.makeText(
                        this@CreateGroup,
                        "Bu grup ismi mevcut, başka bir isim seçin.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                if (createGroupViewModel.saveGroup().await())
                    Toast.makeText(this@CreateGroup, "Başarıyla kaydedildi.", Toast.LENGTH_LONG)
                        .show()
                val intent = Intent(this@CreateGroup, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        addMoreUser()

        backToOldActivity()
    }

    private fun backToOldActivity() {
        binding.backToOldPageBtn.setOnClickListener {
            val intent = Intent(this@CreateGroup, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun addMoreUser() {
        binding.addMoreBtn2.setOnClickListener {
            binding.secondSelectLinear.visibility = View.VISIBLE
            binding.addMoreBtn2.visibility = View.GONE
            binding.addMoreBtn3.visibility = View.VISIBLE
        }
        binding.addMoreBtn3.setOnClickListener {
            binding.thirdSelectLinear.visibility = View.VISIBLE
            binding.addMoreBtn3.visibility = View.GONE
        }
    }

    private fun initSelectUserAdapters() {
        val nameAndSurnameList = createGroupViewModel.getNameAndSurnameList()
        val friendsInfoList = createGroupViewModel.getFriendInfoList()
        adapter3 = ArrayAdapter(this@CreateGroup, R.layout.list_item, nameAndSurnameList)
        binding.autoCompleteTextCreateGroup3.setAdapter(adapter3)
        binding.autoCompleteTextCreateGroup3.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                val selectedItem = adapterView.getItemAtPosition(i)
                for (user in friendsInfoList) {
                    if (selectedItem == "${user.name} ${user.surname}") {
                        if (selectedItem.toString() in selectedUsers) {
                            Toast.makeText(
                                this@CreateGroup,
                                "Sadece bir kez ekleyebilirsin",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.autoCompleteTextCreateGroup3.text.clear()
                        } else {
                            selectedUsers.add(selectedItem.toString())
                            binding.autoCompleteTextCreateGroup3.isEnabled = false
                        }
                    }
                }
            }


        adapter4 = ArrayAdapter(this@CreateGroup, R.layout.list_item, nameAndSurnameList)
        binding.autoCompleteTextCreateGroup4.setAdapter(adapter4)
        binding.autoCompleteTextCreateGroup4.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                val selectedItem = adapterView.getItemAtPosition(i)

                for (user in friendsInfoList) {
                    if (selectedItem == "${user.name} ${user.surname}") {
                        if (selectedItem.toString() in selectedUsers) {
                            Toast.makeText(
                                this@CreateGroup,
                                "Sadece bir kez ekleyebilirsin",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.autoCompleteTextCreateGroup4.text.clear()
                        } else {
                            selectedUsers.add(selectedItem.toString())
                            binding.autoCompleteTextCreateGroup4.isEnabled = false

                        }
                    }
                }
            }

        adapter5 = ArrayAdapter(this@CreateGroup, R.layout.list_item, nameAndSurnameList)
        binding.autoCompleteTextCreateGroup5.setAdapter(adapter5)
        binding.autoCompleteTextCreateGroup5.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                val selectedItem = adapterView.getItemAtPosition(i)

                for (user in friendsInfoList) {
                    if (selectedItem == "${user.name} ${user.surname}") {
                        if (selectedItem.toString() in selectedUsers) {
                            Toast.makeText(
                                this@CreateGroup,
                                "Sadece bir kez ekleyebilirsin",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.autoCompleteTextCreateGroup5.text.clear()
                        } else {
                            selectedUsers.add(selectedItem.toString())
                            binding.autoCompleteTextCreateGroup5.isEnabled = false

                        }
                    }
                }
            }

        adapter6 = ArrayAdapter(this@CreateGroup, R.layout.list_item, nameAndSurnameList)
        binding.autoCompleteTextCreateGroup6.setAdapter(adapter6)
        binding.autoCompleteTextCreateGroup6.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                val selectedItem = adapterView.getItemAtPosition(i)

                for (user in friendsInfoList) {
                    if (selectedItem == "${user.name} ${user.surname}") {
                        if (selectedItem.toString() in selectedUsers) {
                            Toast.makeText(
                                this@CreateGroup,
                                "Sadece bir kez ekleyebilirsin",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.autoCompleteTextCreateGroup6.text.clear()
                        } else {
                            selectedUsers.add(selectedItem.toString())
                            binding.autoCompleteTextCreateGroup6.isEnabled = false
                        }
                    }
                }
            }
        adapter7 = ArrayAdapter(this@CreateGroup, R.layout.list_item, nameAndSurnameList)

        binding.autoCompleteTextCreateGroup7.setAdapter(adapter7)
        binding.autoCompleteTextCreateGroup7.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                val selectedItem = adapterView.getItemAtPosition(i)

                for (user in friendsInfoList) {
                    if (selectedItem == "${user.name} ${user.surname}") {
                        if (selectedItem.toString() in selectedUsers) {
                            Toast.makeText(
                                this@CreateGroup,
                                "Sadece bir kez ekleyebilirsin",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.autoCompleteTextCreateGroup7.text.clear()
                        } else {
                            selectedUsers.add(selectedItem.toString())
                            binding.autoCompleteTextCreateGroup7.isEnabled = false
                        }
                    }
                }
            }

        adapter8 = ArrayAdapter(this, R.layout.list_item, nameAndSurnameList)
        binding.autoCompleteTextCreateGroup8.visibility = View.GONE
        var textInput = findViewById<TextInputLayout>(R.id.textInput1)
        textInput.visibility = View.GONE

        binding.addMoreBtn2.visibility = View.GONE
        binding.addMoreBtn.setOnClickListener {
            textInput.visibility = View.VISIBLE
            binding.autoCompleteTextCreateGroup8.visibility = View.VISIBLE
            binding.addMoreBtn2.visibility = View.VISIBLE
            binding.autoCompleteTextCreateGroup8.setAdapter(adapter8)
            binding.addMoreBtn.visibility = View.GONE
            binding.autoCompleteTextCreateGroup8.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, view, i, l ->

                    val selectedItem = adapterView.getItemAtPosition(i)

                    for (user in friendsInfoList) {
                        if (selectedItem == "${user.name} ${user.surname}") {
                            if (selectedItem.toString() in selectedUsers) {
                                Toast.makeText(
                                    this@CreateGroup,
                                    "Sadece bir kez ekleyebilirsin",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.autoCompleteTextCreateGroup8.text.clear()
                            } else {
                                selectedUsers.add(selectedItem.toString())
                                binding.autoCompleteTextCreateGroup8.isEnabled = false
                            }
                        }
                    }
                }
        }
    }

    private fun initBanner() {
        mAdView = findViewById(R.id.adView2)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                println("başarısız")
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                println("başarılı")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@CreateGroup, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}



package com.yucox.splitwise.View


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.yucox.splitwise.ViewModel.AccountViewModel
import com.yucox.splitwise.databinding.RegisterActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: RegisterActivityBinding
    private lateinit var accountViewModel: AccountViewModel
    private var userPfp: String? = ""
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.INVISIBLE

        accountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)

        binding.registerToLogin.setOnClickListener {
            goToLoginPage()
        }
        binding.uploadPfpBtn.setOnClickListener {
            openGallery()
        }

        binding.registerButton.setOnClickListener {
            if (!checkInfo(userPfp)) {
                return@setOnClickListener
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val pas = binding.registerPass.text.toString()
                    val user = accountViewModel.getUser()
                    val result = accountViewModel.createAccount(user, pas, userPfp.toString())
                    if (!result.await()) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Kayıt başarısız.",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        binding.progressBar.visibility = View.GONE
                        return@launch
                    }
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.backToLoginPage.setOnClickListener {
            backToPrevious()
        }

        registerGalleryLauncher()
    }

    private fun registerGalleryLauncher() {
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    userPfp = selectedImageUri.toString()
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(
                        rootView,
                        "Profil fotoğrafı başarıyla seçildi",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }


    private fun backToPrevious() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToLoginPage() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun checkInfo(userPfp: String?): Boolean {
        binding.progressBar.visibility = View.VISIBLE
        val name = binding.registerName.text.toString()
        val surname = binding.registerSurname.text.toString()
        val mail = binding.registerMail.text.toString()
        val user = accountViewModel.cleanAndSetUserInfo(name, surname, mail)

        if (accountViewModel.checkBlankArea(user)) {
            Toast.makeText(
                this@RegisterActivity,
                "Lütfen bütün boş alanları doldurun!",
                Toast.LENGTH_LONG
            ).show()
            binding.progressBar.visibility = View.INVISIBLE
            return false
        }
        return true
    }
}
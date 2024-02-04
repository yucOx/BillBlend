package com.yucox.splitwise.activity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.R.R.model.UserInfo
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.yucox.splitwise.R
import com.yucox.splitwise.databinding.RegisterActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: RegisterActivityBinding
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("UsersData")
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val auth1 = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.INVISIBLE
        var userPfp: String? = ""

        val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    userPfp = selectedImageUri.toString()
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView,"Profil fotoğrafı başarıyla seçildi",Snackbar.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()
                }
            }

        goToLoginPage()

        uploadPfp(galleryLauncher)

        register(userPfp)

        backToPrevious()

    }

    private fun uploadPfp(galleryLauncher: ActivityResultLauncher<Intent>) {
        binding.uploadPfpBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }
    }

    private fun backToPrevious() {
        binding.backToLoginPage.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun goToLoginPage() {
        binding.registerToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun register(userPfp: String?) {
        val name = findViewById<EditText>(R.id.registerName)
        val surname = findViewById<EditText>(R.id.registerSurname)
        val mail = findViewById<EditText>(R.id.registerMail)
        val pass = findViewById<EditText>(R.id.registerPass)
        binding.registerButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            var getName = name.text.toString()
            var getSurname = surname.text.toString()
            var getMail = mail.text.toString()
            var getPas = pass.text.toString()

            if (getName.startsWith(" ")) {
                getName = name.text.toString().replace(" ", "")
            }
            if (getSurname.startsWith(" ") || getSurname.endsWith("")) {
                getSurname = surname.text.toString().replace(" ", "")
            }
            if (getMail.startsWith(" ") || getMail.endsWith("")) {
                getMail = mail.text.toString().replace(" ", "")
            }
            if (getName.isBlank() || getSurname.isBlank() || getMail.isBlank() || getPas.isBlank()) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Lütfen bütün boş alanları doldurun!",
                    Toast.LENGTH_LONG
                ).show()
                binding.progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            var userInfo = UserInfo(getName, getSurname, getMail, "")
            var storageRef = firebaseStorage.getReference(userInfo.mail.toString())
            auth1.createUserWithEmailAndPassword(getMail, getPas)
                .addOnSuccessListener {
                    ref.push().setValue(userInfo)
                        .addOnSuccessListener {
                            if (userPfp.isNullOrEmpty() == false) {
                                storageRef.putFile(Uri.parse(userPfp))
                                    .addOnSuccessListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val intent =
                                                Intent(
                                                    this@RegisterActivity,
                                                    MainActivity::class.java
                                                )
                                            startActivity(intent)
                                            finish()
                                            binding.progressBar.visibility = View.INVISIBLE
                                        }
                                    }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val intent =
                                        Intent(this@RegisterActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    binding.progressBar.visibility = View.INVISIBLE
                                }
                            }
                        }
                }
                .addOnFailureListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Bu hesap zaten kayıtlı veya kaydedilirken bir hata meydana geldi..",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.progressBar.visibility = View.INVISIBLE
                    }
                }
        }
    }
}
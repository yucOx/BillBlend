package com.yucox.splitwise.activity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.R.R.model.UserInfo
import com.yucox.splitwise.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)
        var progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE
        var database = Firebase.database
        var ref = database.getReference("UsersData")
        var firebaseStorage = FirebaseStorage.getInstance()

        var loginBtn = findViewById<ImageView>(R.id.registerToLogin)
        loginBtn.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        var name = findViewById<EditText>(R.id.registerName)
        var surname = findViewById<EditText>(R.id.registerSurname)
        var mail = findViewById<EditText>(R.id.registerMail)
        var pass = findViewById<EditText>(R.id.registerPass)
        var registerBtn = findViewById<ImageView>(R.id.registerButton)

        var userPfp: String? = ""
        var showImage = findViewById<ImageView>(R.id.insertImage)
        var galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    showImage.setImageURI(selectedImageUri)
                    userPfp = selectedImageUri.toString()
                } else {
                    Toast.makeText(this, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()
                }
            }
        var selectImage = findViewById<ConstraintLayout>(R.id.tapForSelect)
        selectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }

        registerBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
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
                progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            var userInfo = UserInfo(getName, getSurname, getMail, "")
            var auth1 = FirebaseAuth.getInstance()
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
                                                Intent(this@RegisterActivity, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                            progressBar.visibility = View.INVISIBLE
                                        }
                                    }
                            } else{
                                CoroutineScope(Dispatchers.Main).launch {
                                    val intent =
                                        Intent(this@RegisterActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    progressBar.visibility = View.INVISIBLE
                                }
                            }
                        }
                }
                .addOnFailureListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@RegisterActivity,"Bu hesap zaten kayıtlı veya kaydedilirken bir hata meydana geldi..",Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
                }
        }
        var backToLoginBtn = findViewById<ImageView>(R.id.backToLoginPage)
        backToLoginBtn.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
package com.yucox.splitwise.activity


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.yucox.splitwise.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarLogin)
        progressBar.visibility = View.INVISIBLE

        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        logIn()

        goToRegisterPage()

    }

    private fun goToRegisterPage() {
        val registerBtn = findViewById<ImageView>(R.id.letmetoRegister)
        registerBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun logIn() {
        var mail = findViewById<EditText>(R.id.signinMail)
        var pass = findViewById<EditText>(R.id.password)
        var loginBtn = findViewById<ImageView>(R.id.loginButton)

        loginBtn.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.progressBarLogin)
            progressBar.visibility = View.VISIBLE
            val getMail = mail.text.toString()
            val getPass = pass.text.toString()
            if (getMail.isBlank()) {
                Toast.makeText(this@LoginActivity, "Mail alanı boş olmamalı!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
                progressBar.visibility = View.INVISIBLE
            }
            if (getPass.isBlank()) {
                Toast.makeText(this@LoginActivity, "Şifre alanı boş olmamalı!", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
                progressBar.visibility = View.INVISIBLE
            }
            auth.signInWithEmailAndPassword(getMail, getPass).addOnSuccessListener {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@LoginActivity,
                        "Giriş başarılı, ana ekrana yönlendiriliyorsunuz",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.INVISIBLE
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@LoginActivity, "Mail veya şifre hatalı!", Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}
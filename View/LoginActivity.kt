package com.yucox.splitwise.View


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.yucox.splitwise.R
import com.yucox.splitwise.ViewModel.AccountViewModel
import com.yucox.splitwise.databinding.LoginActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBarLogin.visibility = View.INVISIBLE

        accountViewModel = ViewModelProvider(this@LoginActivity).get(AccountViewModel::class.java)

        if (accountViewModel.isAnyoneIn()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener {
            if (!logIn())
                return@setOnClickListener
            else {
                CoroutineScope(Dispatchers.Main).launch {
                    val mail = binding.signinMail.text.toString()
                    val password = binding.password.text.toString()
                    if (accountViewModel.signIn(mail, password).await()) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        return@launch
                    }
                    Toast.makeText(
                        this@LoginActivity, "Mail veya şifre hatalı!", Toast.LENGTH_LONG
                    ).show()
                    binding.progressBarLogin.visibility = View.GONE
                    return@launch
                }
            }
        }

        binding.letmetoRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun logIn(): Boolean {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarLogin)
        progressBar.visibility = View.VISIBLE
        val getMail = binding.signinMail.text.toString()
        val getPass = binding.password.text.toString()
        if (getMail.isBlank()) {
            Toast.makeText(this@LoginActivity, "Mail alanı boş olmamalı!", Toast.LENGTH_SHORT)
                .show()
            progressBar.visibility = View.INVISIBLE
            return false
        }
        if (getPass.isBlank()) {
            Toast.makeText(this@LoginActivity, "Şifre alanı boş olmamalı!", Toast.LENGTH_LONG)
                .show()
            progressBar.visibility = View.INVISIBLE
            return false
        }
        return true
    }
}
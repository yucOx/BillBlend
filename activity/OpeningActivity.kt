package com.yucox.splitwise.activity



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.yucox.splitwise.R

class OpeningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.opening_activity)

        Handler().postDelayed(
            {
                var intent = Intent(this@OpeningActivity,LoginActivity::class.java)
                startActivity(intent)
                finish()
            },100)

    }
}
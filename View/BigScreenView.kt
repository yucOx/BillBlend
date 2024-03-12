package com.yucox.splitwise.View

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.yucox.splitwise.R

class BigScreenView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.big_screen_view_activity)

        val photo = intent.getStringExtra("img")

        info()

        setPhoto(photo)

        goToBack()

        rotatePhoto()

    }

    private fun info() {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(
            rootView,
            "Geri dönmek için tıklayın, döndürmek için basılı tutun",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Tamam") {}
            .show()
    }

    private fun goToBack() {
        val bigPhotoIv = findViewById<ImageView>(R.id.bigPhotoIv)
        bigPhotoIv.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {
                when (bigPhotoIv.rotation) {
                    0f -> {
                        bigPhotoIv.rotation += 90f
                    }

                    90f -> {
                        bigPhotoIv.rotation += 180f
                    }

                    else -> {
                        bigPhotoIv.rotation = 0f
                    }
                }
                return true
            }
        })
    }

    private fun setPhoto(photo: String?) {
        val bigPhotoIv = findViewById<ImageView>(R.id.bigPhotoIv)
        if (!photo.isNullOrEmpty()) {
            Glide.with(this).load(Uri.parse(photo)).centerInside().into(bigPhotoIv)
        } else {
            Glide.with(this).load(R.drawable.nouploadedphoto).centerInside().into(bigPhotoIv)
        }
    }

    private fun rotatePhoto() {
        val bigPhotoIv = findViewById<ImageView>(R.id.bigPhotoIv)
        bigPhotoIv.setOnClickListener {
            finish()
        }
    }
}
package com.yucox.splitwise.activity



import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.yucox.splitwise.R
import kotlin.random.Random

class MainTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_main)

        var handler = Handler()
        var database = Firebase.database
        var ref = database.getReference("UsersData")

        var imageView = findViewById<ImageView>(R.id.abo)

        var firebaseStorage = FirebaseStorage.getInstance()
        var ref1 = firebaseStorage.getReference("a@gmail.com")
        var profilePicList = ArrayList<Int>()
        profilePicList.add(R.drawable.neitzsche)
        var random = Random(100)
        println(random.nextInt(1,100))
        Glide.with(this@MainTest).load(profilePicList[0]).into(imageView)

        // Indirme URL'sini almak için ref1.downloadUrl metodu kullanılmalıdır.
       /* ref1.downloadUrl.addOnSuccessListener { uri ->
            val imageUri = uri.toString()
            println(imageUri)

            // Alınan indirme URL'sini kullanmak için burada işlemleri gerçekleştirin.
            Glide.with(this@MainTest).load(imageUri).into(imageView)

        }.addOnFailureListener { exception ->
            // URL alınamadı, hata mesajını buradan alabilirsiniz: exception.message
        }*/
    }
}

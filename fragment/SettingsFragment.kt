package com.yucox.splitwise.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yucox.splitwise.R
import com.yucox.splitwise.activity.LoginActivity
import de.hdodenhof.circleimageview.CircleImageView


class SettingsFragment : Fragment() {
    private lateinit var currentUserInfo : com.R.R.model.UserInfo
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        var nameText = view.findViewById<TextView>(R.id.nameTextsettingsfragment)
        var surnameText = view.findViewById<TextView>(R.id.surnameTextsettingsfragment)
        var mailText = view.findViewById<TextView>(R.id.mailTextsettingsfragment)

        var database = Firebase.database
        var ref = database.getReference("UsersData")
        var auth = FirebaseAuth.getInstance()
        var firebase = Firebase
        var firebaseStorageRef = firebase.storage.getReference(auth.currentUser?.email.toString())

        var progressBar = view.findViewById<ProgressBar>(R.id.progressBarsettingsgragment)
        progressBar.visibility = View.GONE

        var uploadProfile = view.findViewById<LinearLayout>(R.id.uploadPfpsettingsfragment)
        var deleteProfile = view.findViewById<LinearLayout>(R.id.deletePfpsettingsfragment)

            var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        progressBar.visibility = View.VISIBLE
                        if(firebaseStorageRef != null){
                           firebaseStorageRef.delete()
                        }
                        firebaseStorageRef.putFile(selectedImageUri).addOnSuccessListener {
                            getPfp(auth.currentUser?.email.toString())
                            Toast.makeText(requireContext(),"Profil fotoğrafı başarıyla değiştirildi.",Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        }
                    }
                    // Seçilen resmin Uri'si burada kullanılabilir
                } else {
                    Toast.makeText(context, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()
                }
            }
            uploadProfile.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                galleryLauncher.launch(intent)
            }
        deleteProfile.setOnClickListener{
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Profil fotoğrafını kaldırmak istiyor musun?")
            builder.setNegativeButton("Evet"){dialog,which ->
                if(firebaseStorageRef != null){
                    firebaseStorageRef.delete().addOnSuccessListener{
                        Toast.makeText(requireContext(),"Profil fotoğrafı başarıyla kaldırıldı.",Toast.LENGTH_SHORT).show()
                    }
                    ifPfpBlank()
                }else{
                    Toast.makeText(requireContext(),"İşlem başarısız, daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                }
            }
            builder.setPositiveButton("Hayır"){dialog,which ->}
                .show()

        }

        currentUserInfo = com.R.R.model.UserInfo()
        var listener = ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var currentUserMail = auth.currentUser?.email
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        var temp = snap.getValue<com.R.R.model.UserInfo>()
                        if(temp?.mail == currentUserMail){
                            println("burada")
                            println(currentUserInfo.name)
                            currentUserInfo.name = temp?.name
                            currentUserInfo.surname = temp?.surname
                            currentUserInfo.mail = temp?.mail
                            currentUserInfo.pfpUri = temp?.pfpUri

                            getPfp(currentUserMail.toString())
                        }
                    }
                }
                nameText?.text = currentUserInfo.name.toString()
                surnameText?.text = currentUserInfo.surname
                mailText?.text = currentUserInfo.mail
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        ref.addValueEventListener(listener)
        var logoutBtn = view.findViewById<ImageView>(R.id.logoutBtnsettingsfragment)
        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()

        }

        return view
    }
    fun ifPfpBlank(){
        var imgHolder : ArrayList<Int> = ArrayList<Int>()
        imgHolder.add(R.drawable.luffy)
        var pfp = view?.findViewById<CircleImageView>(R.id.pfpsettingsfragment)
        if(isAdded) {
            Glide.with(requireContext()).load(imgHolder[0]).into(pfp!!)
        }
    }
    fun getPfp(userMail : String){
        var firebase = Firebase
        var firebaseStorage = firebase.storage
        if(isAdded){
        firebaseStorage.getReference(currentUserInfo.mail.toString()).downloadUrl.addOnSuccessListener { uri ->
            var imgUri = uri.toString()
            Handler().post {
                if (isAdded) {
                    var pfp = view?.findViewById<CircleImageView>(R.id.pfpsettingsfragment)
                    if (imgUri != null) {
                        Glide.with(requireContext()).load(imgUri).into(pfp!!)
                    }
                }
            }
        }.addOnFailureListener {
            Handler().post {
                ifPfpBlank()
            }
        }
        }
    }
}
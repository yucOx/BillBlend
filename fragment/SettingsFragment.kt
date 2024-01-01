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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        var nameText = view.findViewById<TextView>(R.id.nameTextsettingsfragment)
        var surnameText = view.findViewById<TextView>(R.id.surnameTextsettingsfragment)
        var mailText = view.findViewById<TextView>(R.id.mailTextsettingsfragment)

        var auth = FirebaseAuth.getInstance()
        var firebaseStorageRef = Firebase.storage.getReference(auth.currentUser?.email.toString())

        var progressBar = view.findViewById<ProgressBar>(R.id.progressBarsettingsgragment)
        var pfp = view.findViewById<CircleImageView>(R.id.pfpsettingsfragment)
        progressBar.visibility = View.GONE

        val haveGottenPfp = arguments?.getString("pfp")
        var haveGottenName = arguments?.getString("name")
        var haveGottenSurname = arguments?.getString("surname")
        var haveGottenMail = arguments?.getString("mail")

        setHaveGottenData(haveGottenPfp,haveGottenName,haveGottenSurname,haveGottenMail,nameText,surnameText,mailText,pfp)

        var galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        progressBar.visibility = View.VISIBLE
                        firebaseStorageRef.putFile(selectedImageUri).addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    requireContext(),
                                    "Profil fotoğrafı başarıyla değiştirildi.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()
                }
            }
        pfp.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }

        var logoutBtn = view.findViewById<ImageView>(R.id.logoutBtnsettingsfragment)
        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }

    private fun setHaveGottenData(
        haveGottenPfp: String?,
        haveGottenName: String?,
        haveGottenSurname: String?,
        haveGottenMail: String?,
        nameText: TextView?,
        surnameText: TextView?,
        mailText: TextView?,
        pfp: ImageView?
    ) {
        if (haveGottenPfp.isNullOrEmpty() == false)
            Glide.with(requireContext()).load(haveGottenPfp).into(pfp!!)
        else
            Glide.with(requireContext()).load(R.drawable.splitwisecat).into(pfp!!)
        nameText?.text = haveGottenName
        surnameText?.text = haveGottenSurname
        mailText?.text = haveGottenMail
    }
}
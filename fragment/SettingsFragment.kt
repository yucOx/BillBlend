package com.yucox.splitwise.fragment


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val nameText = view.findViewById<TextView>(R.id.nameTextsettingsfragment)
        val surnameText = view.findViewById<TextView>(R.id.surnameTextsettingsfragment)
        val mailText = view.findViewById<TextView>(R.id.mailTextsettingsfragment)

        val auth = FirebaseAuth.getInstance()
        val firebaseStorageRef = Firebase.storage.getReference(auth.currentUser?.email.toString())

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarsettingsgragment)
        val pfp = view.findViewById<CircleImageView>(R.id.pfpsettingsfragment)
        progressBar.visibility = View.GONE

        val haveGottenPfp = arguments?.getString("pfp")
        val haveGottenName = arguments?.getString("name")
        val haveGottenSurname = arguments?.getString("surname")
        val haveGottenMail = arguments?.getString("mail")

        setHaveGottenData(haveGottenPfp,haveGottenName,haveGottenSurname,haveGottenMail,nameText,surnameText,mailText,pfp)

        var galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    Glide.with(requireContext()).load(selectedImageUri).into(pfp)
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
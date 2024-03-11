package com.yucox.splitwise.Fragment


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
import com.yucox.splitwise.View.LoginActivity
import com.yucox.splitwise.databinding.FragmentSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val auth = FirebaseAuth.getInstance()
        val firebaseStorageRef = Firebase.storage.getReference(auth.currentUser?.email.toString())

        binding!!.progressBarsettingsgragment.visibility = View.GONE

        val intentPfp = arguments?.getString("pfp")
        val intentName = arguments?.getString("name")
        val intentSurname = arguments?.getString("surname")
        val intentMail = arguments?.getString("mail")

        if (!intentPfp.isNullOrEmpty())
            Glide.with(requireContext()).load(intentPfp).into(binding!!.pfpsettingsfragment)
        else
            Glide.with(requireContext()).load(R.drawable.splitwisecat)
                .into(binding!!.pfpsettingsfragment)
        binding!!.nameTextsettingsfragment.text = intentName
        binding!!.surnameTextsettingsfragment.text = intentSurname
        binding!!.mailTextsettingsfragment.text = intentMail


        val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    val selectedImageUri: Uri? = data?.data
                    Glide.with(requireContext()).load(selectedImageUri)
                        .into(binding!!.pfpsettingsfragment)
                    if (selectedImageUri == null)
                        return@registerForActivityResult

                    binding!!.progressBarsettingsgragment.visibility = View.VISIBLE
                    firebaseStorageRef.putFile(selectedImageUri).addOnSuccessListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                requireContext(),
                                "Profil fotoğrafı başarıyla değiştirildi.",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding!!.progressBarsettingsgragment.visibility = View.GONE
                        }
                    }
                } else
                    Toast.makeText(context, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()

            }
        binding!!.pfpsettingsfragment.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }

        binding!!.logoutBtnsettingsfragment.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return binding?.root
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
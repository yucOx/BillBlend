
class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        var progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        var database = Firebase.database
        var ref = database.getReference("UsersData")
        var auth = FirebaseAuth.getInstance()
        var refCheck = database.getReference("MailCheck")
        val sharedPref = this.getPreferences(MODE_PRIVATE)
        var firebaseStorage = FirebaseStorage.getInstance()
        val editor = sharedPref.edit()


        var loginBtn = findViewById<TextView>(R.id.registerToLogin)
        loginBtn.setOnClickListener {
            val intent = Intent(this@RegisterActivity,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        var name = findViewById<EditText>(R.id.registerName)
        var surname = findViewById<EditText>(R.id.registerSurname)
        var mail = findViewById<EditText>(R.id.registerMail)
        var pass = findViewById<EditText>(R.id.registerPass)
        var registerBtn = findViewById<ImageView>(R.id.registerButton)


        var userPfp : String? = ""
        var selectImageBtn = findViewById<Button>(R.id.insertImageBtn)
        var showImage = findViewById<ImageView>(R.id.insertImage)
        var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                showImage.setImageURI(selectedImageUri)
                userPfp = selectedImageUri.toString()
                // Seçilen resmin Uri'si burada kullanılabilir
            } else {
                userPfp = "0"
            Toast.makeText(this, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()
            }
        }
        selectImageBtn.setOnClickListener {
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

            var ismailMatched = 0
            if (getName.isBlank() || getSurname.isBlank() || getMail.isBlank() || getPas.isBlank()) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Lütfen bütün boş alanları doldurun!",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.INVISIBLE
            } else if (!getMail.endsWith("gmail.com") && !getMail.endsWith("hotmail.com")) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Mail adresinizi doğru formatta giriniz.               Örneğin: yucox.29@gmail.com",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.INVISIBLE
            } else {
                var userInfo = UserInfo(getName, getSurname, getMail,"")

                var checkDataMail: String? = null
                var handler = Handler()
                var runnable = object : Runnable{
                    override fun run() {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@RegisterActivity,
                            "Başarıyla kaydedildi.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                var runnable2 = object : Runnable{
                    override fun run() {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@RegisterActivity,
                            "Mail adresi önceden kayıt edilmiş.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                var a = snap.child("mail")
                                    .getValue<String>(String::class.java)
                                if (getMail == a) {
                                    checkDataMail = a
                                }
                            }
                        }
                        if (checkDataMail == getMail) {
                            handler.postDelayed(runnable2,1500)
                            println("Kayıtlı")
                        } else {
                            println(getMail)
                            println(getPas)
                            if(getPas.length <= 6) {
                                Toast.makeText(this@RegisterActivity,"Şifre en az 7 haneli olmalıdır",Toast.LENGTH_LONG).show()
                                progressBar.visibility = View.INVISIBLE
                                return
                            }
                            var auth1 = FirebaseAuth.getInstance()
                            refCheck.push().setValue(getMail)
                            var storageRef = firebaseStorage.getReference(userInfo.mail.toString())
                            if(userPfp != "0"){
                                storageRef.putFile(Uri.parse(userPfp))
                            }
                            ref.push().setValue(userInfo)
                                .addOnSuccessListener {
                                    handler.postDelayed(runnable,2500)
                                }
                            auth1.createUserWithEmailAndPassword(getMail,getPas).addOnSuccessListener {
                                val intent = Intent(this@RegisterActivity,MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }

            /* else {
                var checkMe = 0
                var handler = Handler()

                fun callmeBack() {
                    var userInfo = UserInfo(getName, getSurname, getMail)
                    if (checkMe == 0) {
                        refCheck.push().setValue(getMail)
                        ref.push().setValue(userInfo)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Başarıyla kaydedildi.",
                                    Toast.LENGTH_LONG
                                ).show()
                                println("kaydedildi")
                                Thread.sleep(2000)
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Veri kaydedilemedi.",
                                    Toast.LENGTH_LONG
                                ).show()
                                progressBar.visibility = View.GONE
                            }
                    }
                }
                var checkDataMail : String? = null
                var runnable2 = Runnable{
                    kotlin.run {
                        println("wait a second")
                        progressBar.visibility = View.GONE
                    }
                }
                var runnable = object : Runnable {
                    override fun run() {
                        println("workin")
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for (snap in snapshot.children) {
                                        var a = snap.child("mail")
                                            .getValue<String>(String::class.java)
                                        if (getMail == a) {
                                            checkDataMail = a
                                        }
                                    }
                                }
                                if(checkDataMail == getMail){
                                    Toast.makeText(this@RegisterActivity,"Bu mail daha önceden kayıt edilmiş.",Toast.LENGTH_LONG).show()
                                    handler.postDelayed(runnable2,3000)
                                    progressBar.visibility = View.VISIBLE
                                }else{
                                    handler.postDelayed(runnable2,3000)
                                    callmeBack()
                                    println("bekle")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
                handler.post(runnable)
                println("handler 1 çağrısı")
            }
        }*/
        }
    }
}

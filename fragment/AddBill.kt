package com.yucox.splitwise.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.R.R.model.Group
import com.R.R.model.UserInfo
import com.R.R.model.WhoHowmuch
import com.yucox.splitwise.R


class AddBill : Fragment() {
    private lateinit var database: FirebaseDatabase
    private lateinit var runnableForSave: Runnable
    var handler = Handler()
    private lateinit var whoBoughtForSave : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_bill_fragment, container, false)
        val groupName = arguments?.getString("GroupName")

        var groupUsers = ArrayList<UserInfo>()
        var group = ArrayList<Group>()

        database = Firebase.database
        var ref = database.getReference("Groups")
        var refgroupusersInfo = database.getReference("UsersData")
        var auth = Firebase.auth
        var whoBought: String? = ""

        var getonlyNameAndSurname = mutableListOf<String>()

        var listenerGroupMembers =
            refgroupusersInfo.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var i = 0
                    if (snapshot.exists()) {
                        for (snap in snapshot.children) {
                            var temp = snap.getValue(UserInfo::class.java)
                            for (user in group.distinct()) {
                                if (user?.email == temp?.mail) {
                                    if (user.email != auth.currentUser?.email) {
                                        getonlyNameAndSurname.add(i, "${user.name} ${user.surname}")
                                        i++
                                    } else {
                                        whoBought = "${user.name} ${user.surname}"
                                    }
                                    groupUsers.add(temp!!)
                                }
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        var progressBar = view.findViewById<ProgressBar>(R.id.progressBar2)
        progressBar.visibility = View.GONE

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (snap.exists()) {
                            for (realsnap in snap.children) {
                                var temp = realsnap.getValue(Group::class.java)
                                if (temp?.GroupName == groupName) {
                                    group.add(temp!!)
                                }
                            }
                        }
                    }
                }
                refgroupusersInfo.addValueEventListener(listenerGroupMembers)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        var selectedImageView = view.findViewById<ImageView>(R.id.selectedBillImage_addbillfragment)
        var getSelectedImage: String? = ""
        var galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data: Intent? = result.data
                    var selectedImageUri = data?.data
                    selectedImageView.setImageURI(selectedImageUri)
                    getSelectedImage = selectedImageUri.toString()
                    // Seçilen resmin Uri'si burada kullanılabilir
                } else {
                    Toast.makeText(context, "Hiçbir resim seçilmedi", Toast.LENGTH_SHORT).show()
                }
            }

        var bigScreen = view.findViewById<LinearLayout>(R.id.showinbigScreen)
        var imageOnBigScreen = view.findViewById<ImageView>(R.id.imageOnBigScreen)
        bigScreen.visibility = View.GONE
        var zoomIn = view.findViewById<LinearLayout>(R.id.zoomIn)
        zoomIn.setOnClickListener {
            bigScreen.visibility = View.VISIBLE
            Glide.with(requireContext()).load(getSelectedImage).into(imageOnBigScreen)
        }
        var zoomOut = view.findViewById<ImageView>(R.id.zoomOut)
        zoomOut.setOnClickListener {
            bigScreen.visibility = View.GONE
        }

        var photoOfBill = view.findViewById<LinearLayout>(R.id.photoOfBill_addbillfragment)
        photoOfBill.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }


        var billName = view.findViewById<TextView>(R.id.addtheBillname_addbillfragment)
        var addPrice = view.findViewById<TextView>(R.id.addprice_addbillfragment)
        var savetheBill = view.findViewById<ImageView>(R.id.savetheBill)
        addPrice.setOnClickListener {

        }

        var specialPriceForEveryoneBtn =
            view.findViewById<Button>(R.id.specialPriceForEveryPersonBtn)
        var specialpriceLinear = view.findViewById<LinearLayout>(R.id.specialpriceLinear)

        var partofSpecialLinear2 = view.findViewById<LinearLayout>(R.id.partofSpecialLinear2)

        var closeTab = view.findViewById<ImageView>(R.id.closeTheSpecialLinearBtn)
        closeTab.setOnClickListener {
            specialpriceLinear.visibility = View.GONE
            var linearLayout6 = view.findViewById<LinearLayout>(R.id.linearLayout6)
            linearLayout6.visibility = View.VISIBLE
        }

        var cake2 = view.findViewById<LinearLayout>(R.id.cake2)
        cake2.visibility = View.GONE
        var cake3 = view.findViewById<LinearLayout>(R.id.cake3)
        cake3.visibility = View.GONE
        var cake4 = view.findViewById<LinearLayout>(R.id.cake4)
        cake4.visibility = View.GONE
        var cake5 = view.findViewById<LinearLayout>(R.id.cake5)
        cake5.visibility = View.GONE
        var cake6 = view.findViewById<LinearLayout>(R.id.cake6)
        cake6.visibility = View.GONE

        specialpriceLinear.visibility = View.GONE
        partofSpecialLinear2.visibility = View.GONE
        var linearLayout6 = view.findViewById<LinearLayout>(R.id.linearLayout6)
        specialPriceForEveryoneBtn.setOnClickListener {
            specialpriceLinear.visibility = View.VISIBLE
            linearLayout6.visibility = View.GONE
        }
        var addmoreSpecialPriceBtn = view.findViewById<ImageView>(R.id.addmoreSpecialPrice)
        var specialpriceBorderCounter = 1
        addmoreSpecialPriceBtn.setOnClickListener {
            specialpriceBorderCounter++
            if (specialpriceBorderCounter == 2) {
                cake2.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 3) {
                cake3.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 4) {
                partofSpecialLinear2.visibility = View.VISIBLE
                cake4.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 5) {
                cake5.visibility = View.VISIBLE
            } else if (specialpriceBorderCounter == 6) {
                cake6.visibility = View.VISIBLE
            }
        }


        val autoComplete = view.findViewById<AutoCompleteTextView>(R.id.selectSpecial)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, getonlyNameAndSurname)
        autoComplete.setAdapter(adapter)
        var user1WhoWillPay: String? = ""
        var selectedItem1: Any?
        var checkIsNameRepeat = mutableListOf<String>()
        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem1 = adapterView.getItemAtPosition(i)
                if (selectedItem1 in checkIsNameRepeat) {
                    autoComplete.text.clear()
                    Toast.makeText(
                        requireContext(),
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user1WhoWillPay = selectedItem1.toString()
                    checkIsNameRepeat.add(user1WhoWillPay.toString())
                    autoComplete.isEnabled = false
                }
            }
        val autoComplete2 = view.findViewById<AutoCompleteTextView>(R.id.selectSpecial2)
        val adapter2 = ArrayAdapter(requireContext(), R.layout.list_item, getonlyNameAndSurname)
        autoComplete2.setAdapter(adapter2)
        var user2WhoWillPay: String? = ""
        var selectedItem2: Any?
        autoComplete2.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem2 = adapterView.getItemAtPosition(i)
                if (selectedItem2 in checkIsNameRepeat) {
                    autoComplete2.text.clear()
                    Toast.makeText(
                        requireContext(),
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user2WhoWillPay = selectedItem2.toString()
                    checkIsNameRepeat.add(user2WhoWillPay.toString())
                    autoComplete2.isEnabled = false

                }
            }
        val autoComplete3 = view.findViewById<AutoCompleteTextView>(R.id.selectSpecial3)
        val adapter3 = ArrayAdapter(requireContext(), R.layout.list_item, getonlyNameAndSurname)
        autoComplete3.setAdapter(adapter3)
        var user3WhoWillPay: String? = ""
        var selectedItem3: Any?
        autoComplete3.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem3 = adapterView.getItemAtPosition(i)
                if (selectedItem3 in checkIsNameRepeat) {
                    autoComplete3.text.clear()
                    Toast.makeText(
                        requireContext(),
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user3WhoWillPay = selectedItem3.toString()
                    checkIsNameRepeat.add(user3WhoWillPay.toString())
                    autoComplete3.isEnabled = false
                }
            }
        val autoComplete4 = view.findViewById<AutoCompleteTextView>(R.id.selectSpecial4)
        val adapter4 = ArrayAdapter(requireContext(), R.layout.list_item, getonlyNameAndSurname)
        autoComplete4.setAdapter(adapter4)
        var user4WhoWillPay: String? = ""
        var selectedItem4: Any?
        autoComplete4.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem4 = adapterView.getItemAtPosition(i)
                if (selectedItem4 in checkIsNameRepeat) {
                    autoComplete4.text.clear()
                    Toast.makeText(
                        requireContext(),
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user4WhoWillPay = selectedItem4.toString()
                    checkIsNameRepeat.add(user4WhoWillPay.toString())
                    autoComplete4.isEnabled = false
                }
            }
        val autoComplete5 = view.findViewById<AutoCompleteTextView>(R.id.selectSpecial5)
        val adapter5 = ArrayAdapter(requireContext(), R.layout.list_item, getonlyNameAndSurname)
        autoComplete5.setAdapter(adapter5)
        var user5WhoWillPay: String? = ""
        var selectedItem5: Any?
        autoComplete5.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem5 = adapterView.getItemAtPosition(i)
                if (selectedItem5 in checkIsNameRepeat) {
                    autoComplete5.text.clear()
                    Toast.makeText(
                        requireContext(),
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user5WhoWillPay = selectedItem5.toString()
                    checkIsNameRepeat.add(user5WhoWillPay.toString())
                    autoComplete5.isEnabled = false
                }
            }
        val autoComplete6 = view.findViewById<AutoCompleteTextView>(R.id.selectSpecial6)
        val adapter6 = ArrayAdapter(requireContext(), R.layout.list_item, getonlyNameAndSurname)
        autoComplete6.setAdapter(adapter6)
        var user6WhoWillPay: String? = ""
        var selectedItem6: Any?
        autoComplete6.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->

                selectedItem6 = adapterView.getItemAtPosition(i)
                if (selectedItem6 in checkIsNameRepeat) {
                    autoComplete6.text.clear()
                    Toast.makeText(
                        requireContext(),
                        "Aynı kişiyi sadece bir kez seçebilirsiniz!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    user6WhoWillPay = selectedItem6.toString()
                    checkIsNameRepeat.add(user6WhoWillPay.toString())
                    autoComplete6.isEnabled = false
                }
            }
        var getHowMuchSpecialPrice = view.findViewById<EditText>(R.id.editTextNumber)
        var getHowMuchSpecialPrice2 = view.findViewById<EditText>(R.id.editTextNumber2)
        var getHowMuchSpecialPrice3 = view.findViewById<EditText>(R.id.editTextNumber3)
        var getHowMuchSpecialPrice4 = view.findViewById<EditText>(R.id.editTextNumber4)
        var getHowMuchSpecialPrice5 = view.findViewById<EditText>(R.id.editTextNumber5)
        var getHowMuchSpecialPrice6 = view.findViewById<EditText>(R.id.editTextNumber6)

        var whowillPaySeperateCounter = 0
        var deleteAllSelected = view.findViewById<ImageView>(R.id.deleteAllSpecialPricesBtn)
        deleteAllSelected.setOnClickListener {
            autoComplete.text.clear()
            autoComplete2.text.clear()
            autoComplete3.text.clear()
            autoComplete4.text.clear()
            autoComplete5.text.clear()
            autoComplete6.text.clear()

            checkIsNameRepeat.clear()
            user1WhoWillPay = ""
            user2WhoWillPay = ""
            user3WhoWillPay = ""
            user4WhoWillPay = ""
            user5WhoWillPay = ""
            user6WhoWillPay = ""
            autoComplete.isEnabled= true
            autoComplete2.isEnabled= true
            autoComplete3.isEnabled= true
            autoComplete4.isEnabled= true
            autoComplete5.isEnabled= true
            autoComplete6.isEnabled= true

            whowillPaySeperateCounter = 0
            Toast.makeText(requireContext(), "Temizlendi", Toast.LENGTH_SHORT).show()
        }

        savetheBill.setOnClickListener {
            var whoHowmuch = ArrayList<WhoHowmuch>()

            val price1 = getHowMuchSpecialPrice.text.toString().toDoubleOrNull() ?: null
            val price2 = getHowMuchSpecialPrice2.text.toString().toDoubleOrNull() ?: null
            val price3 = getHowMuchSpecialPrice3.text.toString().toDoubleOrNull() ?: null
            val price4 = getHowMuchSpecialPrice4.text.toString().toDoubleOrNull() ?: null
            val price5 = getHowMuchSpecialPrice5.text.toString().toDoubleOrNull() ?: null
            val price6 = getHowMuchSpecialPrice6.text.toString().toDoubleOrNull() ?: null
            var howmuch = 0.0
            if (addPrice.text.toString().isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Lütfen boş alanları doldurunuz.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            howmuch = addPrice.text.toString().toDouble()
            var totalPrice = 0.0

            if (user1WhoWillPay?.isBlank() == false && price1 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user1WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user1WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price1,
                            howmuch,
                            billName.text.toString()
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price1 + totalPrice
            }
            if (user2WhoWillPay?.isBlank() == false && price2 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user2WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user2WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price2,
                            howmuch,
                            billName.text.toString()
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price2 + totalPrice
            }
            if (user3WhoWillPay?.isBlank() == false && price3 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user3WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user3WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price3,
                            howmuch,
                            billName.text.toString()
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price3 + totalPrice

            }
            if (user4WhoWillPay?.isBlank() == false && price4 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user4WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user4WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price4,
                            howmuch,
                            billName.text.toString()
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price4 + totalPrice
            }
            if (user5WhoWillPay?.isBlank() == false && price5 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user5WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user5WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price5,
                            howmuch,
                            billName.text.toString()
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price5 + totalPrice
            }
            if (user6WhoWillPay?.isBlank() == false && price6 != null) {
                var cleanName: String? = ""
                for (user in group) {
                    if (user6WhoWillPay == "${user.name} ${user.surname}") {
                        cleanName = user6WhoWillPay
                    }
                }
                if (cleanName != null) {
                    whoHowmuch.add(
                        WhoHowmuch(
                            cleanName,
                            groupName,
                            0,
                            auth.currentUser?.email,
                            price6,
                            howmuch,
                            billName.text.toString()
                        )
                    )
                }
                whowillPaySeperateCounter++
                totalPrice = price6 + totalPrice
            }


            var ref = database.getReference("Bills")
            var auth = Firebase.auth
            if (billName.text.toString().isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Lütfen fatura ismini giriniz.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (addPrice.text.toString().isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Fatura fiyatı boş olmamalıdır.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (!billName.text.toString().isBlank() && !addPrice.text.toString().isBlank()) {
                if (addPrice.text.toString().toInt() < 0) {
                    Toast.makeText(
                        requireContext(),
                        "Fatura tutarı 1den küçük olamaz.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                var cleanandSplitted = howmuch - totalPrice
                var willHowMuchPay: Double = 0.0
                if (groupUsers.size - whowillPaySeperateCounter - 1 != 0) {
                    willHowMuchPay =
                        cleanandSplitted / ((groupUsers.size + 1) - whowillPaySeperateCounter - 1)
                }
                var getNamesFromData = mutableListOf<String>()
                for (a in groupUsers) {
                    if (a.mail != auth.currentUser?.email) {
                        getNamesFromData.add("${a.name} ${a.surname}")
                    }
                }
                var getNamesFromwhoHowMuchWillPay = mutableListOf<String>()
                for (a in whoHowmuch) {
                    getNamesFromwhoHowMuchWillPay.add("${a.whoWillPay}")
                }
                for (temp in getNamesFromwhoHowMuchWillPay) {
                    if (temp in getNamesFromData) {

                    } else {
                        whoHowmuch.add(
                            WhoHowmuch(
                                temp,
                                groupName,
                                0,
                                auth.currentUser?.email,
                                willHowMuchPay,
                                howmuch,
                                billName.text.toString()
                            )
                        )
                    }
                }
                for (a in getNamesFromData) {
                    if (a in getNamesFromwhoHowMuchWillPay) {

                    } else {
                        whoHowmuch.add(
                            WhoHowmuch(
                                a,
                                groupName,
                                0,
                                auth.currentUser?.email,
                                willHowMuchPay,
                                howmuch,
                                billName.text.toString()
                            )
                        )
                    }
                }
                var refOfBills = database.getReference("Bills")
                var getOldBill = ArrayList<WhoHowmuch>()
                var isBillNameActive = 0
                var counterSave = 0
                refOfBills.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (snap in snapshot.children) {
                                if (snap.exists()) {
                                    for (rsnap in snap.children) {
                                        var temp = rsnap.getValue(WhoHowmuch::class.java)
                                        if (groupName == temp?.groupName) {
                                            getOldBill.add(temp!!)
                                        }
                                    }
                                }
                            }
                        }
                        var getBillName = view.findViewById<TextView>(R.id.addtheBillname_addbillfragment)
                        for(a in getOldBill){
                            if(a.billname == getBillName.text.toString() && isBillNameActive == 0){
                                isBillNameActive = 1
                                Toast.makeText(requireContext(),"Bu fatura zaten kayıtlı.",Toast.LENGTH_SHORT).show()
                            }
                        }
                        if(isBillNameActive == 0){
                            refOfBills.push().setValue(whoHowmuch)
                            if(getSelectedImage?.isBlank() == false){
                                var getForData = getBillName.text.toString()
                                var storageRef = Firebase.storage.getReference("$groupName")
                                var childRef = storageRef.child("${getForData}")

                                var progressBar = view.findViewById<ProgressBar>(R.id.progressBar2)
                                progressBar.visibility = View.VISIBLE
                                childRef.child("${auth.currentUser?.email}").putFile(Uri.parse(getSelectedImage))
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(),"Başarıyla kaydedildi.",Toast.LENGTH_SHORT).show()
                                        progressBar.visibility = View.GONE
                                    }
                            }else{
                                var getForData = getBillName.text.toString()
                                var storageRef = Firebase.storage.getReference("$groupName")
                                var childRef = storageRef.child("${getForData}")
                                Toast.makeText(requireContext(),"Başarıyla kaydedildi.",Toast.LENGTH_SHORT).show()
                            }
                            isBillNameActive = 1
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
        return view
    }
}


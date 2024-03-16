package com.R.R.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class BillInfo(
    var whoWillPay: String? = "",
    var groupName: String? = "",
    var whohasPaid: Int = 0,
    var whoBought: String? = "",
    var howmuchWillpay: Double = 0.0,
    var totalPrice: Double = 0.0,
    var billname: String? = "",
    var photoLocation: String? = "",
    var snapKeyOfGroup: String? = "",
    val createTime: Date? = null
)

class BillRepository {
    private var _billKey = ""

    fun divideAndConquerToBills(
        database: FirebaseDatabase,
        _snapKey: String?
    ): Task<Boolean> {
        val refBills = database.getReference("Bills")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        for (rsnap in snap.children) {
                            val checkSnapKey = rsnap.child("snapKeyOfGroup").getValue()
                            if (checkSnapKey?.equals(_snapKey) == false)
                                continue

                            val checkPhotoLocation =
                                rsnap.child("photoLocation").getValue().toString()
                            if (checkPhotoLocation.isEmpty())
                                continue

                            deleteBillsAndPhotos(checkPhotoLocation, snap.key.toString())
                        }
                    }
                }
                taskCompletionSource.setResult(true)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun deleteBillsAndPhotos(photoLocation: String, snapKeyBill: String) {
        Firebase.storage.getReference(photoLocation)
            .delete()
        Firebase.database.getReference("Bills")
            .child(snapKeyBill)
            .removeValue()
    }

    fun fetchBills(
        database: FirebaseDatabase,
        groupName: String?,
        photoLocationHashMap: HashMap<String, String>,
        billsArray: ArrayList<BillInfo>,
        billNamesHash: HashSet<String>,
        snapKey: String?
    ): Task<Boolean> {
        val refBills = database.getReference("Bills")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (!snap.exists())
                            continue

                        for (realSnap in snap.children) {
                            val temp = realSnap.getValue(BillInfo::class.java)
                            if (temp?.groupName == groupName && temp?.snapKeyOfGroup == snapKey) {
                                if (temp?.whohasPaid == 2) {
                                    photoLocationHashMap.put(
                                        temp.billname.toString(),
                                        temp.photoLocation.toString()
                                    )
                                }
                                if (temp?.billname !in billNamesHash) {
                                    billsArray.add(temp!!)
                                    billNamesHash.add(temp?.billname.toString())
                                }
                            }
                        }
                    }
                }
                if (billsArray.size == 0) {
                    taskCompletionSource.setResult(false)
                } else {
                    taskCompletionSource.setResult(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun fetchBillDetails(
        database: FirebaseDatabase,
        theBill: BillInfo,
        billDetails: ArrayList<BillInfo>
    ): Task<Boolean> {
        val refOfBills = database.getReference("Bills")
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        refOfBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                    return

                for (snap in snapshot.children) {
                    if (!snap.exists())
                        continue
                    for (rSnap in snap.children) {
                        val temp = rSnap.getValue(BillInfo::class.java)
                        if (temp?.billname == theBill.billname &&
                            temp?.groupName == theBill.groupName &&
                            temp?.snapKeyOfGroup == theBill.snapKeyOfGroup
                        )
                            billDetails.add(temp!!)
                    }
                }
                taskCompletionSource.setResult(true)
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun deleteToBill(
        database: FirebaseDatabase,
        theBill: BillInfo,
        billDetails: ArrayList<BillInfo>
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refOfBills = database.getReference("Bills")
        val storage = FirebaseStorage.getInstance()

        refOfBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deleteTasks = mutableListOf<Task<Void>>()
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        for (rsnap in snap.children) {
                            val temp = rsnap.getValue(BillInfo::class.java)
                            if (temp?.billname == billDetails[0].billname &&
                                temp?.groupName == theBill.groupName &&
                                temp?.snapKeyOfGroup == theBill.snapKeyOfGroup
                            ) {
                                val uniqueId = snap.key.toString()
                                val deleteTask = refOfBills.child(uniqueId).removeValue()
                                deleteTasks.add(deleteTask)
                                if (theBill.photoLocation!!.isNotEmpty()) {
                                    val photoDeleteTask =
                                        storage.getReference(theBill.photoLocation!!)
                                            .delete()
                                    deleteTasks.add(photoDeleteTask)
                                }
                            }
                        }
                    }
                }
                Tasks.whenAll()
                    .addOnSuccessListener {
                        taskCompletionSource.setResult(true)
                    }
                    .addOnFailureListener {
                        taskCompletionSource.setResult(false)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                taskCompletionSource.setResult(false)
            }
        })
        return taskCompletionSource.task
    }

    fun checkBillName(
        billName: String,
        onComplete: (Boolean) -> Unit,
        groupName: String,
        database: FirebaseDatabase
    ) {
        val refOfBills = database.getReference("Bills")
        var check = false
        refOfBills.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snap in snapshot.children) {
                        if (!snap.exists())
                            continue

                        for (rsnap in snap.children) {
                            if (rsnap.child("groupName").getValue().toString()
                                    .equals(groupName) && rsnap.child("billname").getValue()
                                    .toString() == billName
                            ) {
                                check = true
                                break
                            }
                        }

                    }
                }
                onComplete(check)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun saveTheBill(
        selectedImage: String,
        billName: String,
        time: Date,
        database: FirebaseDatabase,
        whoWillPayArray: ArrayList<BillInfo>,
        snapKey: String,
        groupName: String
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refOfBills = database.getReference("Bills")
        _billKey = refOfBills.push().key.toString()

        whoWillPayArray.add(
            BillInfo(
                "",
                groupName,
                2,
                "",
                0.0,
                0.0,
                billName,
                _billKey,
                snapKey,
                time
            )
        )
        refOfBills.child(_billKey).setValue(whoWillPayArray)
            .addOnSuccessListener {
                if (selectedImage.isBlank())
                    taskCompletionSource.setResult(true)
                else {
                    Firebase.storage.getReference(_billKey).putFile(Uri.parse(selectedImage))
                        .addOnSuccessListener {
                            taskCompletionSource.setResult(true)
                        }
                        .addOnFailureListener {
                            taskCompletionSource.setResult(true)
                        }
                }
            }
        return taskCompletionSource.task
    }

    fun saveTheBillForOne(
        selectedImage: String,
        billName: String,
        price: String,
        time: Date,
        database: FirebaseDatabase,
        whoWillPayArray: ArrayList<BillInfo>,
        mainUserInfo: User,
        snapKey: String,
        groupName: String
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val refOfBills = database.getReference("Bills")

        _billKey = refOfBills.push().key.toString()
        whoWillPayArray.add(
            BillInfo(
                "${mainUserInfo.name} ${mainUserInfo.surname}",
                groupName,
                0,
                mainUserInfo.mail,
                price.toDouble(),
                price.toDouble(),
                billName,
                "",
                snapKey,
                time
            )
        )
        whoWillPayArray.add(
            BillInfo(
                "",
                groupName,
                2,
                "",
                0.0,
                0.0,
                billName,
                _billKey,
                snapKey,
                time
            )
        )
        refOfBills.child(_billKey).setValue(whoWillPayArray)
            .addOnSuccessListener {
                if (!selectedImage.isBlank()) {
                    val storageRef = Firebase.storage.getReference(_billKey)
                    storageRef.putFile(Uri.parse(selectedImage))
                        .addOnSuccessListener {
                            taskCompletionSource.setResult(true)
                        }
                        .addOnFailureListener {
                            taskCompletionSource.setResult(true)
                        }
                } else
                    taskCompletionSource.setResult(true)
            }
        return taskCompletionSource.task
    }

    fun uploadPhoto(photo: Uri, photoLocation: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()

        Firebase.storage.getReference(photoLocation)
            .putFile(photo)
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)

            }
            .addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

}
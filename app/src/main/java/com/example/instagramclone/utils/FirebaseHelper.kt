package com.example.instagramclone.utils

import android.app.Activity
import android.net.Uri
import com.example.instagramclone.activities.showToast
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseHelper(private val activity: Activity) {
    val mAuth: FirebaseAuth =
        FirebaseAuth.getInstance()
    val mDatabase: DatabaseReference = FirebaseDatabase.getInstance()
        .reference
    val mStorage: StorageReference = FirebaseStorage.getInstance()
        .reference

    fun updateUser(
        updates: Map<String, Any?>,
        onSuccess: () -> Unit
    ) {
        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun updateEmail(email: String, onSuccess: () -> Unit) {
        mAuth.currentUser!!.updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun uploadUserPhoto(
        photo: Uri,
        onSuccess: (photoUrl: String) -> Unit
    ) {
        val ref = mStorage.child("users/${mAuth.currentUser!!.uid}/photo")
        ref.putFile(photo)
            .addOnCompleteListener { it ->
            if (it.isSuccessful) {
                ref.downloadUrl.addOnCompleteListener{
                    val photoUrl = it.result.toString()
                    mDatabase.child("users/${mAuth.currentUser!!.uid}/photo").setValue(photoUrl)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                onSuccess(photoUrl)
                            } else {
                                activity.showToast(it.exception!!.message!!)
                            }
                        }
                }
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }


    fun updateUserPhoto(
        photoUrl: String,
        onSuccess: () -> Unit
    ) {
        mDatabase.child("users/${mAuth.currentUser!!.uid}/photo").setValue(photoUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun currentUserReference(): DatabaseReference =
        mDatabase.child("users").child(mAuth.currentUser!!.uid)
}
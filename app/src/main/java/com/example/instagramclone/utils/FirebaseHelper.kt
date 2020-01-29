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
    val auth: FirebaseAuth =
        FirebaseAuth.getInstance()
    val database: DatabaseReference = FirebaseDatabase.getInstance()
        .reference
    val storage: StorageReference = FirebaseStorage.getInstance()
        .reference

    fun updateUser(
        updates: Map<String, Any?>,
        onSuccess: () -> Unit
    ) {
        database.child("users").child(auth.currentUser!!.uid).updateChildren(updates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun updateEmail(email: String, onSuccess: () -> Unit) {
        auth.currentUser!!.updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        auth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
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
        val ref = storage.child("users/${auth.currentUser!!.uid}/photo")
        ref.putFile(photo)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        val photoUrl = it.result.toString()
                        database.child("users/${auth.currentUser!!.uid}/photo").setValue(photoUrl)
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
        database.child("users/${auth.currentUser!!.uid}/photo").setValue(photoUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun currentUserReference(): DatabaseReference =
        database.child("users").child(auth.currentUser!!.uid)
}
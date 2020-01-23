package com.example.instagramclone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        closeImage.setOnClickListener { finish() }
        saveImage.setOnClickListener { updateProfile() }

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        mDatabase.child("users").child(mAuth.currentUser!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                nameInput.setText(mUser.name, TextView.BufferType.EDITABLE)
                usernameInput.setText(mUser.userName, TextView.BufferType.EDITABLE)
                websiteInput.setText(mUser.website, TextView.BufferType.EDITABLE)
                bioInput.setText(mUser.bio, TextView.BufferType.EDITABLE)
                emailInput.setText(mUser.email, TextView.BufferType.EDITABLE)
                phoneInput.setText(mUser.phone.toString(), TextView.BufferType.EDITABLE)
            })
    }

    private fun updateProfile() {
        mPendingUser = User(
            name = nameInput.text.toString(),
            userName = usernameInput.text.toString(),
            website = websiteInput.text.toString(),
            bio = bioInput.text.toString(),
            email = emailInput.text.toString(),
            phone = phoneInput.text.toString().toLong()
        )
        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser.email) {
                updateUser(mPendingUser)
            } else {
                PasswordDialog().show(supportFragmentManager, "password dialog")
            }
        } else {
            showToast(error)
        }
    }

    private fun updateUser(user: User) {
        val updateMap = mutableMapOf<String, Any>()
        if (user.name != mUser.name) updateMap["name"] = user.name
        if (user.userName != mUser.userName) updateMap["userName"] = user.userName
        if (user.website != mUser.website) updateMap["website"] = user.website
        if (user.bio != mUser.bio) updateMap["bio"] = user.bio
        if (user.email != mUser.email) updateMap["email"] = user.email
        if (user.phone != mUser.phone) updateMap["phone"] = user.phone

        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updateMap)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    showToast("Profile saved")
                    finish()
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
    }

    private fun validate(user: User): String? =
        when {
            user.name.isEmpty() -> "Please enter name"
            user.userName.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }

    override fun onPasswordConfirm(password: String) {
        val credential = EmailAuthProvider.getCredential(mUser.email, password)
        mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener{
            if (it.isSuccessful) {
                mAuth.currentUser!!.updateEmail(mPendingUser.email).addOnCompleteListener{
                    if (it.isSuccessful) {
                        updateUser(mPendingUser)
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }
}


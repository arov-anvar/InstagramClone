package com.example.instagramclone.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.view.ValueEventListenerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        closeImage.setOnClickListener {
            finish()
        }

        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                val user = it.getValue(User::class.java)
                nameInput.setText(user!!.name, TextView.BufferType.EDITABLE)
                usernameInput.setText(user.userName, TextView.BufferType.EDITABLE)
                websiteInput.setText(user.website, TextView.BufferType.EDITABLE)
                bioInput.setText(user.bio, TextView.BufferType.EDITABLE)
                emailInput.setText(user.email, TextView.BufferType.EDITABLE)
                phoneInput.setText(user.phone.toString(), TextView.BufferType.EDITABLE)
            })
    }
}


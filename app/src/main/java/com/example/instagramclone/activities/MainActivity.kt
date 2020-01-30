package com.example.instagramclone.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.instagramclone.R
import com.example.instagramclone.utils.FirebaseHelper
import com.example.instagramclone.utils.ValueEventListenerAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_likes.*

class MainActivity : BaseActivity(0) {

    private val TAG = "MainActivity"
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        setupBottomNavigation()
        Log.d(TAG, "oncreate")

        mFirebase = FirebaseHelper(this)
        outPutBtn.setOnClickListener{
            mFirebase.auth.signOut()
        }
        mFirebase.auth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        mFirebase.database.child("feed-posts").child(mFirebase.auth.currentUser!!.uid)
            .addValueEventListener(ValueEventListenerAdapter{
                val posts = it.children.map { it.getValue(FeedPost::class.java)!! }
            })
    }

    override fun onStart() {
        super.onStart()
        if (mFirebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

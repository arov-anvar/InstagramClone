package com.example.instagramclone.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_likes.*

class MainActivity : BaseActivity(0) {

    private val TAG = "MainActivity"
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        setupBottomNavigation()
        Log.d(TAG, "oncreate")

        mAuth = FirebaseAuth.getInstance()
        outPutBtn.setOnClickListener{
            mAuth.signOut()
        }
        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
//        auth.signInWithEmailAndPassword("arovanvar1@gmail.com", "Password1234")
//            .addOnCompleteListener{
//                if (it.isSuccessful) {
//                    Log.d(TAG, "singIn: success")
//                } else {
//                    Log.e(TAG, "sing: failer", it.exception)
//                }
//            }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

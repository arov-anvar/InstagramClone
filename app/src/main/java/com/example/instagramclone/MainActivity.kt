package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(0) {

    private val TAG = "MainActivity"
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        setupBottomNavigation()
        Log.d(TAG, "oncreate")

        mAuth = FirebaseAuth.getInstance()
        mAuth.signOut()
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

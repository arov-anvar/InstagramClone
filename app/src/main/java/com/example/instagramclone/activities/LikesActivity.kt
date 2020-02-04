package com.example.instagramclone.activities

import android.os.Bundle
import com.example.instagramclone.R
import com.example.instagramclone.utils.FirebaseHelper
import kotlinx.android.synthetic.main.activity_likes.*

class LikesActivity : BaseActivity(3) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        setupBottomNavigation()
        val mAuth = FirebaseHelper(this).auth
        outPutBtn.setOnClickListener {
            mAuth.signOut()
        }
    }
}

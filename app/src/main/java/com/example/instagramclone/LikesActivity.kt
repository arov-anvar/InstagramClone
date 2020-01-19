package com.example.instagramclone

import android.os.Bundle

class LikesActivity : BaseActivity(3) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        setupBottomNavigation()
    }
}

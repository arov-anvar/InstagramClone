package com.example.instagramclone.activities

import android.os.Bundle
import com.example.instagramclone.R

class ShareActivity : BaseActivity(2) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        setupBottomNavigation()
    }
}

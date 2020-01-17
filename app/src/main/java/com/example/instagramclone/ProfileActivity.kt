package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ProfileActivity : BaseActivity(4) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNavigation()
    }
}

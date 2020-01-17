package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class LikesActivity : BaseActivity(3) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNavigation()
    }
}

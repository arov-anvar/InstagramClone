package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SearchActivity : BaseActivity(1) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNavigation()
    }
}

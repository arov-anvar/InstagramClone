package com.example.instagramclone.activities

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.R
import com.example.instagramclone.utils.FirebaseHelper
import kotlinx.android.synthetic.main.activity_profile_settings.*

class ProfileSettingActivity : AppCompatActivity(){
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_profile_settings)

        mFirebase = FirebaseHelper(this)
        signOutText.setOnClickListener{ mFirebase.auth.signOut() }
        backImage.setOnClickListener { finish() }
    }
}

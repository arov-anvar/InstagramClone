package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.size
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView.setIconSize(29f, 29f)
        bottomNavigationView.setTextVisibility(false)
        bottomNavigationView.enableItemShiftingMode(false)
        bottomNavigationView.enableShiftingMode(false)
        bottomNavigationView.enableAnimation(false)
        for(i in 0 until bottomNavigationView.menu.size()) {
            bottomNavigationView.setIconTintList(i, null)
        }
    }
}

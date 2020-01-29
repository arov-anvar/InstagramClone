package com.example.instagramclone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.utils.CameraHelper
import com.example.instagramclone.utils.FirebaseHelper
import kotlinx.android.synthetic.main.activity_share.*

class ShareActivity : BaseActivity(2) {
    private lateinit var mCameraHelper: CameraHelper
    private lateinit var mFirebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        mFirebaseHelper = FirebaseHelper(this)
        mCameraHelper = CameraHelper(this)
        mCameraHelper.takeCameraPicture()


        backImage.setOnClickListener{ finish() }
        shareText.setOnClickListener{ share() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCameraHelper.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this).load(mCameraHelper.imageUri).centerCrop().into(postImage)
            } else {
                finish()
            }
        }
    }

    private fun share() {
        val imageUri = mCameraHelper.imageUri
        if (mCameraHelper.imageUri != null) {
            val uid = mFirebaseHelper.auth.currentUser!!.uid
            val ref = mFirebaseHelper.storage.child("users").child(uid).child("images")
                .child(imageUri!!.lastPathSegment!!);
            ref.putFile(imageUri).addOnCompleteListener{
                    if (it.isSuccessful) {
                        ref.downloadUrl.addOnCompleteListener{
                            mFirebaseHelper.database.child("images").child(uid).push()
                                .setValue(it.result.toString())
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        startActivity(Intent(this,
                                            ProfileActivity::class.java))
                                        finish()
                                    } else {
                                        showToast(it.exception!!.message!!)
                                    }
                                }
                        }
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
        }
    }
}

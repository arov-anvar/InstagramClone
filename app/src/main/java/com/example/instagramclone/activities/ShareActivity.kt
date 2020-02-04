package com.example.instagramclone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.utils.CameraHelper
import com.example.instagramclone.utils.FirebaseHelper
import com.example.instagramclone.utils.ValueEventListenerAdapter
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.activity_share.*
import java.util.*

class ShareActivity : BaseActivity(2) {
    private lateinit var mCameraHelper: CameraHelper
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        mFirebaseHelper = FirebaseHelper(this)
        mCameraHelper = CameraHelper(this)
        mCameraHelper.takeCameraPicture()


        backImage.setOnClickListener{ finish() }
        shareText.setOnClickListener{ share() }

        mFirebaseHelper.currentUserReference().addValueEventListener(ValueEventListenerAdapter{
            mUser = it.asUser()!!
        })
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
                .child(imageUri!!.lastPathSegment!!)
            ref.putFile(imageUri).addOnCompleteListener{
                    if (it.isSuccessful) {
                        ref.downloadUrl.addOnCompleteListener{
                            val imageDownloadUrl = it.result.toString()
                            mFirebaseHelper.database.child("images").child(uid).push()
                                .setValue(it.result.toString())
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        mFirebaseHelper.database.child("feed-posts")
                                            .child(uid).push().setValue(mkFeedPost(uid, imageDownloadUrl))
                                            .addOnCompleteListener{
                                                if (it.isSuccessful) {
                                                    startActivity(Intent(this,
                                                        ProfileActivity::class.java))
                                                    finish()
                                                }
                                            }

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

    private fun mkFeedPost(uid: String, imageDownloadUrl: String): FeedPost {
        return FeedPost(
            uid = uid,
            username = imageDownloadUrl,
            image = imageDownloadUrl,
            caption = captionInput.text.toString(),
            photo = mUser.photo
        )
    }
}

data class FeedPost(val uid: String = "", val username: String = "", val image: String = "",
                    val likeCount: Int = 0, val commentsCount: Int = 0, val caption: String = "",
                    val comments: List<Comment> = emptyList(), val timestamp: Any = ServerValue.TIMESTAMP,
                    val photo: String? = "") {
    fun timestampDate(): Date = Date(timestamp as Long)
}

data class Comment(val uid: String, val username: String, val text: String)
package com.example.instagramclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.utils.CameraPictureTaker
import com.example.instagramclone.utils.FirebaseHelper
import com.example.instagramclone.utils.ValueEventListenerAdapter
import com.example.instagramclone.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private val TAG: String = "EditProfile"

    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var cameraPictureTaker: CameraPictureTaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        cameraPictureTaker =
            CameraPictureTaker(this)

        closeImage.setOnClickListener { finish() }
        saveImage.setOnClickListener { updateProfile() }
        changePhotoText.setOnClickListener { cameraPictureTaker.takeCameraPicture() }

        mFirebaseHelper = FirebaseHelper(this)


        mFirebaseHelper.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                nameInput.setText(mUser.name)
                usernameInput.setText(mUser.userName)
                websiteInput.setText(mUser.website)
                bioInput.setText(mUser.bio)
                emailInput.setText(mUser.email)
                phoneInput.setText(mUser.phone?.toString())
                profileImageView.loadUserPhoto(mUser.photo)
            })
    }

    private fun readInputs(): User {
        return User(
            name = nameInput.text.toString(),
            userName = usernameInput.text.toString(),
            email = emailInput.text.toString(),
            website = websiteInput.text.toStringOrNull(),
            bio = bioInput.text.toStringOrNull(),
            phone = phoneInput.text.toString().toLongOrNull()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == cameraPictureTaker.REQUEST_CODE && resultCode == RESULT_OK) {
            mFirebaseHelper.uploadUserPhoto(cameraPictureTaker.imageUri!!) {
                mFirebaseHelper.updateUserPhoto(it) {
                    mUser = mUser.copy(photo = it)
                    profileImageView.loadUserPhoto(mUser.photo)
                }
            }
        }
    }

    private fun updateProfile() {
        mPendingUser = readInputs()
        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser.email) {
                updateUser(mPendingUser)
            } else {
                PasswordDialog().show(supportFragmentManager, "password dialog")
            }
        } else {
            showToast(error)
        }
    }

    private fun updateUser(user: User) {
        val updateMap = mutableMapOf<String, Any?>()
        if (user.name != mUser.name) updateMap["name"] = user.name
        if (user.userName != mUser.userName) updateMap["userName"] = user.userName
        if (user.website != mUser.website) updateMap["website"] = user.website
        if (user.bio != mUser.bio) updateMap["bio"] = user.bio
        if (user.email != mUser.email) updateMap["email"] = user.email
        if (user.phone != mUser.phone) updateMap["phone"] = user.phone

        mFirebaseHelper.updateUser(updateMap) {
            showToast("Profile saved")
            finish()
        }
    }

    private fun validate(user: User): String? =
        when {
            user.name.isEmpty() -> "Please enter name"
            user.userName.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }

    override fun onPasswordConfirm(password: String) {
        if (password.isEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mFirebaseHelper.reauthenticate(credential) {
                mFirebaseHelper.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast("You should enter your password")
        }
    }
}
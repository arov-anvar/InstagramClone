package com.example.instagramclone.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.core.content.FileProvider
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.views.PasswordDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private val TAKE_PICTURE_REQUEST_CODE = 1
    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private val TAG: String = "EditProfile"

    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var mImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        closeImage.setOnClickListener { finish() }
        saveImage.setOnClickListener { updateProfile() }
        changePhotoText.setOnClickListener{ takeCameraPicture() }

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference

        mDatabase.child("users").child(mAuth.currentUser!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                nameInput.setText(mUser.name, TextView.BufferType.EDITABLE)
                usernameInput.setText(mUser.userName, TextView.BufferType.EDITABLE)
                websiteInput.setText(mUser.website, TextView.BufferType.EDITABLE)
                bioInput.setText(mUser.bio, TextView.BufferType.EDITABLE)
                emailInput.setText(mUser.email, TextView.BufferType.EDITABLE)
                phoneInput.setText(mUser.phone.toString(), TextView.BufferType.EDITABLE)
            })
    }

    private fun readInputs(): User {
        val phoneStr = phoneInput.text.toString()
        return User(
            name = nameInput.text.toString(),
            userName = usernameInput.text.toString(),
            website = websiteInput.text.toString(),
            bio = bioInput.text.toString(),
            email = emailInput.text.toString(),
            phone = if (phoneStr.isEmpty()) 0 else phoneStr.toLong()
        )
    }

    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(this,
                "com.example.instagramclone.fileprovider",
                imageFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uid = mAuth.currentUser!!.uid
            val ref = mStorage.child("users/$uid/photo")
            ref.putFile(mImageUri).addOnCompleteListener{ it ->
                if(it.isSuccessful){
                    ref.downloadUrl.addOnCompleteListener {
                        val photoUrl = it.result.toString()
                        mDatabase.child("users/$uid/photo").setValue(photoUrl).addOnCompleteListener{
                            if (it.isSuccessful){
                                mUser = mUser.copy(photo = photoUrl)
                            }else{
                                showToast(it.exception!!.message!!)
                            }
                        }
                    }

                }else{
                    showToast(it.exception!!.message!!)
                }
            }
            mStorage.child("users/$uid/photo").putFile(mImageUri).addOnCompleteListener {
                if (it.isSuccessful) {
                    mDatabase.child("users/$uid/photo").setValue(it.result.toString())//.result.downloadUrl.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d(TAG, "onActivityResult: photo saved successfully")
                            } else {
                                showToast(it.exception!!.message!!)
                            }
                        }
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
        // смотреть здесь
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
        val updateMap = mutableMapOf<String, Any>()
        if (user.name != mUser.name) updateMap["name"] = user.name
        if (user.userName != mUser.userName) updateMap["userName"] = user.userName
        if (user.website != mUser.website) updateMap["website"] = user.website
        if (user.bio != mUser.bio) updateMap["bio"] = user.bio
        if (user.email != mUser.email) updateMap["email"] = user.email
        if (user.phone != mUser.phone) updateMap["phone"] = user.phone

        mDatabase.updateUser(mAuth.currentUser!!.uid, updateMap) {
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
            mAuth.currentUser!!.reauthenticate(credential) {
                mAuth.currentUser!!.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast("You should enter your password")
        }
    }

    private fun DatabaseReference.updateUser(uid: String, updates: Map<String, Any>,
                                             onSuccess: () -> Unit) {
        child("users").child(mAuth.currentUser!!.uid).updateChildren(updates)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
    }

    private fun FirebaseUser.updateEmail(email: String, onSuccess: () -> Unit) {
        updateEmail(email).addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }
    private fun FirebaseUser.reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        reauthenticate(credential).addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }
}


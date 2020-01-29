package com.example.instagramclone.model

import android.provider.ContactsContract

data class User(val name: String = "", val userName: String = "", val email: String = "",
                val website: String? = null, val bio: String? = null, val phone: Long? = null,
                val photo: String? = null)
package com.example.instagramclone.model

import android.provider.ContactsContract

data class User(val name: String = "", val userName: String = "", val website: String = "",
                val bio: String = "", val email: String = "", val phone: Long = 0L)
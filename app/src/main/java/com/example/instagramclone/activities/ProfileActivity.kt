package com.example.instagramclone.activities

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.utils.FirebaseHelper
import com.example.instagramclone.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity(4) {

    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()

        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        settingImage.setOnClickListener {
            val intent = Intent(this, ProfileSettingActivity::class.java)
            startActivity(intent)
        }

        addFriendsImage.setOnClickListener {
            val intent = Intent(this, AddFriendsActivity::class.java)
            startActivity(intent)
        }

        mFirebaseHelper = FirebaseHelper(this)
        mFirebaseHelper.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.asUser()!!
            profileImageView.loadUserPhoto(mUser.photo)
            userNameTextView.text = mUser.userName
            postsCountText.text = "0"
            followersCountText.text = mUser.followers.count().toString()
            followingCountText.text = mUser.follows.count().toString()
            getImage()
        })

        imagesRecycler.layoutManager = GridLayoutManager(this, 3)
    }

    private fun getImage() {
        mFirebaseHelper.database.child("feed-posts").child(mUser.uid)
            .addValueEventListener(ValueEventListenerAdapter {
                val posts = it.children.map { it.getValue(FeedPost::class.java) }
                    .sortedByDescending { it!!.timestampDate() }
                val images : ArrayList<String> = ArrayList()
                for (item in posts) {
                    images.add(item!!.image)
                }
                imagesRecycler.adapter = ImagesAdapter(images)
            })
    }
}

class ImagesAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    class ViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.image_item, parent, false) as ImageView
        )
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ImagesAdapter.ViewHolder, position: Int) {
        holder.image.loadImage(images[position])
    }

}

class SquareImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}

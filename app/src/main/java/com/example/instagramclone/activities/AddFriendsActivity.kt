package com.example.instagramclone.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.utils.FirebaseHelper
import com.example.instagramclone.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.add_friends_item.view.*

class AddFriendsActivity : AppCompatActivity(), FriendsAdapter.Listener {

    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_add_friends)

        mFirebase = FirebaseHelper(this)
        mAdapter = FriendsAdapter(this)

        val uid = mFirebase.auth.currentUser!!.uid

        addFriendsRecycler.adapter = mAdapter
        addFriendsRecycler.layoutManager = LinearLayoutManager(this)

        mFirebase.database.child("users").addValueEventListener(ValueEventListenerAdapter {
            val allUsers = it.children.map { it.getValue(User::class.java)!!.copy(uid = it.key) }
            // функция возвращает два значения первое то которое равно Uid, а остальные которые
            // не равны uid возвращаются в виде списка
            val (userList, otherUsersList) = allUsers.partition { it.uid == uid }
            mUser = userList.first()
            mUsers = otherUsersList
            mAdapter.update(mUsers, mUser.follows)
        })

    }

    override fun follow(uid: String) {
        setFollow(uid, true) {
            mAdapter.followed(uid)
        }
    }

    override fun unfollow(uid: String) {
        setFollow(uid, false) {
            mAdapter.unFollowed(uid)
        }
    }

    private fun setFollow(uid: String, follow: Boolean, onSuccess: () -> Unit) {
        val followTask = mFirebase.database.child("users").child(mUser.uid!!).child("follows")
            .child(uid)
        val setFollow = if (follow) followTask.setValue(true) else followTask.removeValue()
        val followerTask = mFirebase.database.child("users").child(uid).child("followers")
            .child(mUser.uid!!)
        val setFollower = if (follow) followerTask.setValue(true) else followerTask.removeValue()

        setFollow.continueWithTask({setFollower}).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }
}

class FriendsAdapter(private val listener: FriendsAdapter.Listener) :
    RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private var mPositions = mapOf<String, Int>()
    private var mFollows = mapOf<String, Boolean>()
    private var mUsers = listOf<User>()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface Listener {
        fun follow(uid: String)
        fun unfollow(uid: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.add_friends_item, parent, false)
        return view
    }

    override fun getItemCount(): Int = mUsers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val user = mUsers[position]
            view.photoImage.loadImage(user.photo!!)
            view.userNameText.text = user.userName
            view.nameText.text = user.name
            view.followButton.setOnClickListener { listener.follow(user.uid!!) }
            view.unfollowButton.setOnClickListener { listener.unfollow(user.uid!!) }

            val follows = mFollows[user.uid] ?: false
            if (follows) {
                view.followButton.visibility = View.GONE
                view.unfollowButton.visibility = View.VISIBLE
            } else {
                view.followButton.visibility = View.VISIBLE
                view.unfollowButton.visibility = View.GONE
            }
        }
    }

    fun update(users: List<User>, follows: Map<String, Boolean>) {
        mUsers = users
        mPositions = users.withIndex().map { (idx, user) -> user.uid!! to idx }.toMap()
        mFollows = follows
        notifyDataSetChanged()
    }

    fun followed(uid: String) {
        mFollows += (uid to true)
        notifyItemChanged(mPositions[uid]!!)
    }

    fun unFollowed(uid: String) {
        mFollows -= uid
        notifyItemChanged(mPositions[uid]!!)
    }

}
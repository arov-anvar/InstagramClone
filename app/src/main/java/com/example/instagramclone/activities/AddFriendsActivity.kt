package com.example.instagramclone.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.example.instagramclone.utils.FirebaseHelper
import com.example.instagramclone.utils.TaskSourceOnCompleteListener
import com.example.instagramclone.utils.ValueEventListenerAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.android.synthetic.main.add_friends_item.view.*

class AddFriendsActivity : AppCompatActivity(), FriendsAdapter.Listener {

    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)

        backImage.setOnClickListener { finish() }

        mFirebase = FirebaseHelper(this)
        mAdapter = FriendsAdapter(this, mFirebase.auth.uid.toString())
        val uid = mFirebase.auth.currentUser!!.uid

        mFirebase.database.child("users").addValueEventListener(ValueEventListenerAdapter {
            val allUsers = it.children.map { it.asUser()!! }
            // функция возвращает два значения первое то которое равно Uid, а остальные которые
            // не равны uid возвращаются в виде списка
            val (userList, otherUsersList) = allUsers.partition { it.uid == uid }
            mUser = userList.first()
            mUsers = otherUsersList
            mAdapter.update(mUsers, mUser.follows)
        })

        addFriendsRecycler.adapter = mAdapter
        addFriendsRecycler.layoutManager = LinearLayoutManager(this)
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
        fun DatabaseReference.setValueTrueOrRemove(value: Boolean) =
            if (value) setValue(true) else removeValue()

        val followTask = mFirebase.database.child("users").child(mUser.uid).child("follow")
            .child(uid).setValueTrueOrRemove(follow)
        val followersTask = mFirebase.database.child("users").child(uid).child("followers")
            .child(mUser.uid).setValueTrueOrRemove(follow)

        val feedPostsTask = task<Void> {taskSource ->
            mFirebase.database.child("feed-posts").child(uid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                    val postsMap = if (follow) {
                        it.children.map { it.key to it.value }.toMap()
                    } else {
                        it.children.map { it.key to null }.toMap()
                    }
                    mFirebase.database.child("feed-posts").child(mUser.uid).updateChildren(postsMap)
                        .addOnCompleteListener(
                            TaskSourceOnCompleteListener(taskSource)
                        )
                })
        }

        Tasks.whenAll(followTask, followersTask, feedPostsTask).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }
}

class FriendsAdapter(private val listener: Listener, private val uidAuthUser: String) :
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_friends_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mUsers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val user = mUsers[position]
            view.photoImage.loadUserPhoto(user.photo ?: "")
            view.userNameText.text = user.userName
            view.nameText.text = user.name
            view.followButton.setOnClickListener { listener.follow(user.uid) }
            view.unfollowButton.setOnClickListener { listener.unfollow(user.uid) }

            if (user.followers.containsKey(uidAuthUser)) {
                view.followButton.visibility = View.GONE
                view.unfollowButton.visibility = View.VISIBLE
            } else {
                view.followButton.visibility = View.VISIBLE
                view.unfollowButton.visibility = View.GONE
            }
//            val follows = mFollows[user.uid] ?: false
//            if (follows) {
//                view.followButton.visibility = View.GONE
//                view.unfollowButton.visibility = View.VISIBLE
//            } else {
//                view.followButton.visibility = View.VISIBLE
//                view.unfollowButton.visibility = View.GONE
//            }
        }
    }

    fun update(users: List<User>, follows: Map<String, Boolean>) {
        mUsers = users
        mPositions = users.withIndex().map { (idx, user) -> user.uid to idx }.toMap()
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
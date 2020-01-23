package com.example.instagramclone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_email.emailInput
import kotlinx.android.synthetic.main.fragment_register_namepass.*
import kotlinx.android.synthetic.main.fragment_register_namepass.passwordInput
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class RegisterActivity : AppCompatActivity(), NamePassFragment.Listener, EmailFragment.Listener{
    private val TAG = "RegisterActivity"
    private var mEmail: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.frameLayout, EmailFragment()).commit()
        }
    }

    override fun onNext(email: String) {
        if (email.isNotEmpty()) {
            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener{
                if (it.isSuccessful) {
                    if (it.result?.signInMethods?.isEmpty() != false) {
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, NamePassFragment())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        showToast("This email already exists")
                    }
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
        } else {
            showToast("Please enter email")
        }
    }

    override fun onRegister(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty()) {
            val email = mEmail
            if (email != null) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{
                        if (it.isSuccessful) {
                            val user = mkUser(fullName, email)
                            val reference = mDatabase.child("users").child(it.result?.user?.uid.toString())
                            reference.setValue(user)
                                .addOnCompleteListener{
                                    if (it.isSuccessful) {
                                        startHomeActivity()
                                    } else {
                                        unknownRegisterError(it)
                                    }
                                }
                        } else{
                            unknownRegisterError(it)
                        }
                    }
            } else {
                Log.e(TAG, "onRegister: email is null")
                supportFragmentManager.popBackStack()
            }
        } else{

        }
    }

    private fun unknownRegisterError(it: Task<*>) {
        Log.e(TAG, "failed to create user profile", it.exception)
        Toast.makeText(this, "Something wrong happened. Please try again later", Toast.LENGTH_SHORT)
            .show()
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun mkUser (fullName: String, email: String): User {
        val username = mkUsername(fullName)
        return User(name = fullName, userName = username, email = email)
    }

    private fun mkUsername(fullName: String): String =
        fullName.toLowerCase().replace(" ", ".")
}

class EmailFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener{
        fun onNext(email: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnAndInputs(nextButton, emailInput)
        nextButton.setOnClickListener{
            val email = emailInput.text.toString()
            mListener.onNext(email)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

class NamePassFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener{
        fun onRegister(fullName: String, password: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_namepass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateBtnAndInputs(registerBtn, fullNameInput, passwordInput)
        registerBtn.setOnClickListener{
            val fullName = fullNameInput.text.toString()
            val password = passwordInput.text.toString()
            mListener.onRegister(fullName, password)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

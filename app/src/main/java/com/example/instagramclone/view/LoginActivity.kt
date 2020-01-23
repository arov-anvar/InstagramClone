package com.example.instagramclone.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class LoginActivity : AppCompatActivity(), KeyboardVisibilityEventListener, TextWatcher,
    View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        KeyboardVisibilityEvent.setEventListener(this, this)
        loginButton.isEnabled = false
        emailInput.addTextChangedListener(this)
        passwordInput.addTextChangedListener(this)
        loginButton.setOnClickListener(this)
        createAccountTextView.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen) {
            scrollView.scrollTo(0, scrollView.bottom)
            createAccountTextView.visibility = View.GONE
        } else {
            scrollView.scrollTo(0, scrollView.top)
            createAccountTextView.visibility = View.VISIBLE
        }
    }

    override fun afterTextChanged(s: Editable?) {
        // кнопака loginButton будет доступна тольно когда
        // emailInput и passwordInput будут не пустыми
        loginButton.isEnabled = validate(emailInput.text.toString(), passwordInput.text.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun onClick(v: View) {
        when(v.id) {
            R.id.loginButton -> {
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()
                if (validate(email, password)) {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                        if (it.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this, "plese enter email and password", Toast.LENGTH_LONG).show()
                }
            }
            R.id.createAccountTextView -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }
    }

    private fun validate(email: String, password: String) =
        email.isNotEmpty() && password.isNotEmpty()
}

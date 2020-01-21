package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_login.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class LoginActivity : AppCompatActivity(), KeyboardVisibilityEventListener, TextWatcher {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        KeyboardVisibilityEvent.setEventListener(this, this)
        loginButton.isEnabled = false
        emailInput.addTextChangedListener(this)
        passwordInput.addTextChangedListener(this)
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
        loginButton.isEnabled =
            emailInput.text.toString().isNotEmpty() &&
                    passwordInput.text.toString().isNotEmpty()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

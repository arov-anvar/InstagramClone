package com.example.instagramclone.views

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.view.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class KeyboardAwareScrollView(context: Context, attrs: AttributeSet) : ScrollView(context, attrs),
    KeyboardVisibilityEventListener {
    init {
        isFillViewport = true
        isVerticalScrollBarEnabled = false

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        KeyboardVisibilityEvent.setEventListener(context as Activity, this)
    }

    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen) {
            scrollView.scrollTo(0, scrollView.bottom)
        } else {
            scrollView.scrollTo(0, scrollView.top)
        }
    }
}
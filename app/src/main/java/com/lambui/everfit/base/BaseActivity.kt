package com.lambui.everfit.base

import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.lambui.everfit.utils.view.checkClickInsideView
import com.lambui.everfit.utils.view.hideKeyboard

abstract class BaseActivity : AppCompatActivity() {

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return super.dispatchKeyEvent(event)

    }

    private var touchDownTime = 0L
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val view = currentFocus
        val isTouchEvent = super.dispatchTouchEvent(event)
        if (event == null) return isTouchEvent
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchDownTime = SystemClock.elapsedRealtime()
            MotionEvent.ACTION_UP ->
                if (SystemClock.elapsedRealtime() - touchDownTime <= 200 && view is EditText) {
                    if (currentFocus !is EditText) {
                        currentFocus?.clearFocus()
                        view.hideKeyboard()
                    } else {
                        currentFocus?.let {
                            if (!it.checkClickInsideView(event)) {
                                (it.rootView as? ViewGroup)?.run {
                                    val descendant = descendantFocusability
                                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                                    it.clearFocus()
                                    descendantFocusability = descendant
                                }
                                view.hideKeyboard()
                            }
                        }
                    }
                }
        }
        return isTouchEvent
    }
}
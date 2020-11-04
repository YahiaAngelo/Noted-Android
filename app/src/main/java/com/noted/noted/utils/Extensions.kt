package com.noted.noted.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Build
import android.os.Build.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.TintAwareDrawable
import androidx.core.view.ViewCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Extensions(){

    inline fun SharedPreferences.edit(changes: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
        edit().changes().apply()
    }

    fun ImageView.tintSrc(@ColorRes colorRes: Int) {
        val drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorRes))
        setImageDrawable(drawable)
        if (drawable is TintAwareDrawable) invalidate() // Because in this case setImageDrawable will not call invalidate()
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    /**
     * Compat version of setExactAndAllowWhileIdle()
     */
    fun AlarmManager.setExactAndAllowWhileIdleCompat(alarmType: Int, timeMillis: Long, pendingIntent: PendingIntent) {
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            // This version added Doze
            setExactAndAllowWhileIdle(alarmType, timeMillis, pendingIntent)

        } else
            // This version changed set() to be inexact
            setExact(alarmType, timeMillis, pendingIntent)
    }

    /**
     * Helps to set clickable part in text.
     *
     * Don't forget to set android:textColorLink="@color/link" (click selector) and
     * android:textColorHighlight="@color/window_background" (background color while clicks)
     * in the TextView where you will use this.
     */
    fun SpannableString.withClickableSpan(clickablePart: String, onClickListener: () -> Unit): SpannableString {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) = onClickListener.invoke()
        }
        val clickablePartStart = indexOf(clickablePart)
        setSpan(clickableSpan,
            clickablePartStart,
            clickablePartStart + clickablePart.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return this
    }

    /**
     * Helps to get Map, List, Set or other generic type from Json using Gson.
     */
    inline fun <reified T: Any> Gson.fromJsonToGeneric(json: String): T {
        val type = object : TypeToken<T>() {}.type
        return fromJson(json, type)
    }




    fun View.visible(visible: Boolean, useGone: Boolean = true) {
        this.visibility = if (visible) View.VISIBLE else if (useGone) View.GONE else View.INVISIBLE
    }

    // Helps to set status bar color with api version check
    fun Activity.setStatusBarColor(@ColorRes colorRes: Int): Unit {
        window.statusBarColor = ContextCompat.getColor(this, colorRes)
    }

    // Adds flags to make window fullscreen
    fun Activity.setFullscreenLayoutFlags() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    // Adds window insets to the view while entire activity is fullscreen.
    fun View.applyWindowInsets(applyTopInset: Boolean = true, applyOtherInsets: Boolean = true): Unit {
        if (applyTopInset || applyOtherInsets) {
            ViewCompat.setOnApplyWindowInsetsListener(
                this
            ) { view, insets ->
                // Set padding for needed insets
                view.setPadding(
                    if (applyOtherInsets) insets.systemWindowInsetLeft else view.paddingLeft,
                    if (applyTopInset) insets.systemWindowInsetTop else view.paddingTop,
                    if (applyOtherInsets) insets.systemWindowInsetRight else view.paddingRight,
                    if (applyOtherInsets) insets.systemWindowInsetBottom else view.paddingBottom
                )

                // Return without consumed insets
                insets.replaceSystemWindowInsets(
                    if (applyOtherInsets) 0 else insets.systemWindowInsetLeft,
                    if (applyTopInset) 0 else insets.systemWindowInsetTop,
                    if (applyOtherInsets) 0 else insets.systemWindowInsetRight,
                    if (applyOtherInsets) 0 else insets.systemWindowInsetBottom
                )
            }
        } else {
            // Listener is not needed
            ViewCompat.setOnApplyWindowInsetsListener(this, null)
        }
    }

    fun Activity.getScreenHeight(): Int {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return size.y
    }

    fun String.onlyDigits(): String = replace(Regex("\\D*"), "")

    fun View.showKeyboard(show: Boolean) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (show) {
            if (requestFocus()) imm.showSoftInput(this, 0)
        } else {
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }

}
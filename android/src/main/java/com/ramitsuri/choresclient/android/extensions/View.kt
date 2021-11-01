package com.ramitsuri.choresclient.android.extensions

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController

fun View.setVisibility(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun Window.invertInsets(darkTheme: Boolean) {
    if (Build.VERSION.SDK_INT >= 30) {
        val statusBar = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        val navBar = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        if (!darkTheme) {
            insetsController?.setSystemBarsAppearance(statusBar, statusBar)
            insetsController?.setSystemBarsAppearance(navBar, navBar)
        } else {
            insetsController?.setSystemBarsAppearance(0, statusBar)
            insetsController?.setSystemBarsAppearance(0, navBar)
        }
    } else {
        val flags =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        if (!darkTheme) {
            decorView.systemUiVisibility = decorView.systemUiVisibility or flags
        } else {
            decorView.systemUiVisibility = (decorView.systemUiVisibility.inv() or flags).inv()
        }
    }
}
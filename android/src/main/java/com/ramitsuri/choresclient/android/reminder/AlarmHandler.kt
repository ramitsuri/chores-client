package com.ramitsuri.choresclient.android.reminder

interface AlarmHandler {

    fun schedule(requestCode: Int, time: Long)

    fun cancel(requestCode: Int)
}
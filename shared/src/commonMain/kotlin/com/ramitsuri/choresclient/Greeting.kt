package com.ramitsuri.choresclient

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}
package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.resources.LocalizedString

sealed class TextValue {
    data class ForString(val value: String, val args: List<String> = listOf()) : TextValue()
    data class ForKey(val key: LocalizedString, val args: List<String> = listOf()) : TextValue()

    fun addAdditionalArgs(vararg args: String): TextValue {
        return when (this) {
            is ForKey -> {
                val newArgs = this.args.toMutableList().plus(args)
                ForKey(this.key, newArgs)
            }
            is ForString -> {
                val newArgs = this.args.toMutableList().plus(args)
                ForString(this.value, newArgs)
            }
        }
    }
}
package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.resources.LocalizedString

sealed class TextValue {
    class ForString(val value: String, vararg val args: String) : TextValue()
    class ForKey(val key: LocalizedString, vararg val args: String) : TextValue()

    fun addAdditionalArgs(vararg args: String): TextValue {
        return when (this) {
            is ForKey -> {
                val newArgs = this.args.toMutableList().plus(args).toTypedArray()
                ForKey(this.key, *newArgs)
            }
            is ForString -> {
                val newArgs = this.args.toMutableList().plus(args).toTypedArray()
                ForString(this.value, *newArgs)
            }
        }
    }
}
package com.ramitsuri.choresclient.model.enums

enum class CreateType(val key: Int) {
    UNKNOWN(0),
    MANUAL(1),
    AUTO(2);

    companion object {
        fun fromKey(key: Int): CreateType {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return UNKNOWN
        }
    }
}
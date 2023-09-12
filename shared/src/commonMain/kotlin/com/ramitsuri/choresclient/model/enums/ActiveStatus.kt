package com.ramitsuri.choresclient.model.enums

enum class ActiveStatus(val key: Int) {
    UNKNOWN(0),
    ACTIVE(1),
    INACTIVE(2), // This entity is no longer being used
    PAUSED(3);

    companion object {
        fun fromKey(key: Int): ActiveStatus {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return UNKNOWN
        }
    }
}
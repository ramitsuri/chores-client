package com.ramitsuri.choresclient.model.enums

enum class ProgressStatus(val key: Int) {
    UNKNOWN(0),
    TODO(1),
    IN_PROGRESS(2),
    DONE(3),
    WONT_DO(4);

    companion object {
        fun fromKey(key: Int): ProgressStatus {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return UNKNOWN
        }
    }
}
package com.ramitsuri.choresclient.model.enums

enum class RepeatUnit(val key: Int) {
    NONE(0),
    DAY(1),
    WEEK(2),
    MONTH(3),
    HOUR(4),
    YEAR(5),
    ON_COMPLETE(6);

    companion object {
        fun fromKey(key: Int): RepeatUnit {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return NONE
        }
    }
}
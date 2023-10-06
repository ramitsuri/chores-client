package com.ramitsuri.choresclient.model.push

data class PushMessagePayload(
    val action: PushMessageAction,
    val wontDoByOthers: List<String>,
    val doneByOthers: List<String>,
) {
    companion object {
        fun fromMap(map: Map<String, String>): PushMessagePayload? {
            val stringAction = map["action"] ?: return null
            val stringWontDoByOthers = map["wont_do_by_others"] ?: return null
            val stringDoneByOthers = map["done_by_others"] ?: return null

            val action = PushMessageAction.fromStringValue(stringAction) ?: return null
            val wontDoByOthers = stringWontDoByOthers
                .split(";;;")
                .filter { it.isNotEmpty() }
            val doneByOthers = stringDoneByOthers
                .split(";;;")
                .filter { it.isNotEmpty() }

            return PushMessagePayload(
                action = action,
                wontDoByOthers = wontDoByOthers,
                doneByOthers = doneByOthers
            )
        }
    }
}

package com.ramitsuri.choresclient.notification

import kotlin.reflect.KClass

data class NotificationChannelInfo(
    val id: String,
    val name: String,
    val description: String,
    val importance: Importance
)

data class NotificationInfo(
    val id: Int,
    val channelId: String,
    val priority: Priority,
    val title: String,
    val body: String?,
    val additionalText: String?,
    val iconResId: Int,
    val actions: List<NotificationActionInfo>? = null,
    val actionExtras: Map<String, Any>? = null
)

data class NotificationActionInfo(
    val action: String,
    val textResId: Int,
    val intentReceiverClass: KClass<*>
)

enum class Importance(private val key: Int) {
    NONE(0),
    MIN(1),
    LOW(2),
    DEFAULT(3),
    HIGH(4),
    MAX(5);
}

enum class Priority(private val platformValue: Int) {
    DEFAULT(0),
    LOW(-1),
    MIN(-2),
    HIGH(1),
    MAX(2);

    // Returns the platformValue for now but could change with future Android versions
    fun toPlatformValue(): Int {
        return platformValue
    }
}
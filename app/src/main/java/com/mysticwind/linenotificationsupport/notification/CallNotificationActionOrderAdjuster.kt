package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.LineNotification

object CallNotificationActionOrderAdjuster {

    @JvmStatic
    fun adjust(lineNotification: LineNotification, shouldReverseActionOrder: Boolean): LineNotification {
        if (!shouldReverseActionOrder) {
            return lineNotification
        }
        if (lineNotification.actions.size < 2) {
            return lineNotification
        }
        val actions = ArrayList(lineNotification.actions)
        val firstAction = actions[0]
        actions[0] = actions[1]
        actions[1] = firstAction
        return lineNotification.toBuilder()
            .clearActions()
            .actions(actions)
            .build()
    }
}

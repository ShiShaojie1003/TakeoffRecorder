package me.neko.nzhelper.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationUtil {
    // A new id lets existing installs receive the HIGH importance required by
    // HyperOS focus notifications; Android channel importance is immutable.
    const val CHANNEL_ID = "timer_focus_channel"
    const val CHANNEL_NAME = "计时服务"

    fun createChannel(context: Context) {
        val chan = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(chan)
    }
}

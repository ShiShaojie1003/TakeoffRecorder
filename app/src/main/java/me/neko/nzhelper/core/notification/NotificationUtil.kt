package me.neko.nzhelper.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationUtil {
    // Keep this id stable: Android persists every channel id across upgrades.
    const val CHANNEL_ID = "timer_channel"
    const val CHANNEL_NAME = "计时服务"

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Remove channel ids used by earlier builds. Also clean up any other
        // stale channel with the same app-owned name, leaving one category.
        manager.notificationChannels
            .filter { it.id != CHANNEL_ID && it.name.toString() == CHANNEL_NAME }
            .forEach { manager.deleteNotificationChannel(it.id) }

        val chan = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setShowBadge(false)
            description = "显示计时状态，支持 Android 实况通知与超级岛展示"
        }
        manager.createNotificationChannel(chan)
    }
}

package me.neko.nzhelper.core.notification

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/** Android 16+ promoted ongoing notification (Live Update) capability helpers. */
object LiveUpdateManager {
    // Android's Live Update guide exposes this as
    // Settings.ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS, but some Android 16
    // SDK stubs do not contain that field yet. Using the documented action
    // value keeps the source compatible with those SDK installations.
    private const val ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS =
        "android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS"

    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

    fun canPost(context: Context): Boolean {
        if (!isSupported()) return false
        return context.getSystemService(NotificationManager::class.java)
            .canPostPromotedNotifications()
    }

    fun openSettings(context: Context) {
        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = if (isSupported()) {
            Intent(ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS, packageUri).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(intent) }
            .recoverCatching {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
    }
}

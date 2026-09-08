package me.neko.nzhelper.core.notification

import android.app.Notification
import android.app.PendingIntent
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject

/** Xiaomi HyperOS 2 focus notification / HyperOS 3 Super Island extension. */
object XiaomiIslandNotification {
    private const val EXTRA_FOCUS_PARAM = "miui.focus.param"
    private const val EXTRA_FOCUS_ACTIONS = "miui.focus.actions"
    private const val ACTION_TOGGLE = "miui.focus.action.timer_toggle"
    private const val ACTION_STOP = "miui.focus.action.timer_stop"

    /**
     * Adds vendor extras to an otherwise normal Android notification.
     * Unknown extras are ignored on Android and on non-Xiaomi devices.
     */
    fun apply(
        notification: Notification,
        elapsedSeconds: Int,
        isRunning: Boolean,
        toggleTitle: String,
        toggleIntent: PendingIntent,
        stopIntent: PendingIntent
    ) {
        val now = System.currentTimeMillis()
        val startedAt = now - elapsedSeconds.coerceAtLeast(0) * 1_000L
        val elapsedText = formatElapsed(elapsedSeconds)

        val paramV2 = JSONObject()
            .put("protocol", 1)
            .put("business", "timer")
            .put("enableFloat", true)
            .put("updatable", true)
            .put("ticker", if (isRunning) "计时中 $elapsedText" else "计时已暂停 $elapsedText")
            .put("aodTitle", if (isRunning) "计时中" else "计时已暂停")
            .put(
                "baseInfo",
                JSONObject()
                    .put("type", 1)
                    .put("title", if (isRunning) "计时进行中" else "计时已暂停")
                    .put("content", elapsedText)
            )
            .put(
                "actions",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("type", 0)
                            .put("action", ACTION_TOGGLE)
                            .put("actionTitle", toggleTitle)
                    )
                    .put(
                        JSONObject()
                            .put("type", 0)
                            .put("action", ACTION_STOP)
                            .put("actionTitle", "停止")
                    )
            )
            .put(
                "param_island",
                JSONObject()
                    .put("islandProperty", 2)
                    .put("islandTimeout", 60 * 60)
                    .put(
                        "bigIslandArea",
                        JSONObject().put(
                            "sameWidthDigitInfo",
                            timerOrText(isRunning, startedAt, now, elapsedText)
                        )
                    )
                    .put(
                        "smallIslandArea",
                        JSONObject().put(
                            "sameWidthDigitInfo",
                            timerOrText(isRunning, startedAt, now, elapsedText)
                        )
                    )
            )

        val nativeToggleAction = notification.actions?.firstOrNull()
            ?: Notification.Action.Builder(
                notification.smallIcon,
                toggleTitle,
                toggleIntent
            ).build()
        val nativeStopAction = notification.actions?.getOrNull(1)
            ?: Notification.Action.Builder(
                notification.smallIcon,
                "停止",
                stopIntent
            ).build()
        val actions = Bundle().apply {
            putParcelable(ACTION_TOGGLE, nativeToggleAction)
            putParcelable(ACTION_STOP, nativeStopAction)
        }
        notification.extras.putBundle(EXTRA_FOCUS_ACTIONS, actions)
        notification.extras.putString(
            EXTRA_FOCUS_PARAM,
            JSONObject().put("param_v2", paramV2).toString()
        )
    }

    private fun timerOrText(
        isRunning: Boolean,
        startedAt: Long,
        now: Long,
        elapsedText: String
    ): JSONObject = if (isRunning) {
        JSONObject().put(
            "timerInfo",
            JSONObject()
                .put("timerType", 1)
                .put("timerWhen", startedAt)
                .put("timerSystemCurrent", now)
        )
    } else {
        JSONObject().put("title", elapsedText)
    }

    private fun formatElapsed(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val secs = safe % 60
        return "%02d:%02d:%02d".format(hours, minutes, secs)
    }
}

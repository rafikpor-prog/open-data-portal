package pl.radiodriver.app.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import pl.radiodriver.app.playback.RadioPlaybackService

object SleepTimer {
    private const val ACTION_STOP = "pl.radiodriver.app.STOP_AFTER_SLEEP_TIMER"
    private const val REQUEST = 3030

    fun schedule(context: Context, minutes: Int) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val at = System.currentTimeMillis() + minutes.coerceAtLeast(1) * 60_000L
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(context))
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context))
    }

    private fun pending(context: Context) = PendingIntent.getBroadcast(
        context,
        REQUEST,
        Intent(context, SleepTimerReceiver::class.java).setAction(ACTION_STOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

class SleepTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        val token = SessionToken(context, ComponentName(context, RadioPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            runCatching {
                val controller = future.get()
                controller.stop()
                controller.release()
            }
            result.finish()
        }, context.mainExecutor)
    }
}

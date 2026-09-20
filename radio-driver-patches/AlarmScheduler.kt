package pl.radiodriver.app.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import pl.radiodriver.app.data.local.ScheduleEntity
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(item: ScheduleEntity) {
        if (!item.enabled || item.id == 0L) return
        val triggerAt = nextTrigger(item) ?: return
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(item.id, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        )
    }

    fun cancel(id: Long) {
        alarmManager.cancel(pendingIntent(id, PendingIntent.FLAG_NO_CREATE) ?: return)
    }

    fun nextTrigger(item: ScheduleEntity, nowMillis: Long = System.currentTimeMillis()): Long? {
        if (item.daysMask == 0) return null
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        for (addDays in 0..7) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, addDays)
                set(Calendar.HOUR_OF_DAY, item.hour.coerceIn(0, 23))
                set(Calendar.MINUTE, item.minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.timeInMillis <= nowMillis) continue
            if (isDayEnabled(item.daysMask, candidate.get(Calendar.DAY_OF_WEEK))) return candidate.timeInMillis
        }
        return null
    }

    private fun isDayEnabled(mask: Int, calendarDay: Int): Boolean {
        val bit = when (calendarDay) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> return false
        }
        return mask and (1 shl bit) != 0
    }

    private fun pendingIntent(id: Long, flags: Int): PendingIntent? {
        val intent = Intent(context, ScheduleReceiver::class.java).putExtra(EXTRA_SCHEDULE_ID, id)
        return PendingIntent.getBroadcast(
            context,
            (id xor (id ushr 32)).toInt(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object { const val EXTRA_SCHEDULE_ID = "schedule_id" }
}

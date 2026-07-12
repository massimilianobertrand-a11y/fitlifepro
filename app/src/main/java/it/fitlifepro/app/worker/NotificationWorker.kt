
package it.fitlifepro.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import it.fitlifepro.app.FitLifeApplication.*
import it.fitlifepro.app.MainActivity
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title   = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val channel = inputData.getString(KEY_CHANNEL) ?: CH_HYDRATION
        val notifId = inputData.getInt(KEY_ID, System.currentTimeMillis().toInt())

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            applicationContext, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .build()

        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(notifId, notification)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE   = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_CHANNEL = "channel"
        const val KEY_ID      = "notif_id"

        fun scheduleOnce(
            context: Context,
            tag: String,
            delayMs: Long,
            title: String,
            message: String,
            channel: String
        ) {
            val data = workDataOf(
                KEY_TITLE to title,
                KEY_MESSAGE to message,
                KEY_CHANNEL to channel,
                KEY_ID to tag.hashCode()
            )
            val req = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(data)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, req)
        }

        fun cancelByTag(context: Context, tag: String) =
            WorkManager.getInstance(context).cancelAllWorkByTag(tag)
    }
}


package it.fitlifepro.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reschedule notifications after device reboot */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // WorkManager rescheduling is handled automatically by WorkManager.
            // Any periodic worker or one-time worker with constraints is re-enqueued.
        }
    }
}

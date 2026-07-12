
package it.fitlifepro.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FitLifeApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            listOf(
                NotificationChannel(CH_WORKOUT,  "Allenamento",  NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CH_NUTRITION, "Nutrizione",   NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CH_SUPPS,     "Integratori",  NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CH_HYDRATION, "Idratazione",  NotificationManager.IMPORTANCE_DEFAULT),
            ).forEach { manager.createNotificationChannel(it) }
        }
    }

    companion object {
        const val CH_WORKOUT  = "ch_workout"
        const val CH_NUTRITION= "ch_nutrition"
        const val CH_SUPPS    = "ch_supps"
        const val CH_HYDRATION= "ch_hydration"
    }
}

package it.fitlifepro.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.fitlifepro.app.FitLifeApplication.Companion.CH_HYDRATION
import it.fitlifepro.app.FitLifeApplication.Companion.CH_NUTRITION
import it.fitlifepro.app.FitLifeApplication.Companion.CH_SUPPS
import it.fitlifepro.app.FitLifeApplication.Companion.CH_WORKOUT
import it.fitlifepro.app.worker.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class NotifConfig(
    val id: String,
    val label: String,
    val emoji: String,
    val channel: String,
    val defaultHour: Int,
    val defaultMinute: Int,
    val enabled: Boolean = false,
    val hour: Int = 0,
    val minute: Int = 0
)

data class NotificationsUiState(
    val configs: List<NotifConfig> = listOf(
        NotifConfig("workout",   "Promemoria allenamento", "🏋️", CH_WORKOUT,   7,  0),
        NotifConfig("colazione", "Colazione",              "🥣", CH_NUTRITION,  7, 30),
        NotifConfig("pranzo",    "Pranzo",                 "🍽️", CH_NUTRITION, 12, 30),
        NotifConfig("cena",      "Cena",                   "🍴", CH_NUTRITION,  19, 30),
        NotifConfig("supps_am",  "Integratori mattina",    "💊", CH_SUPPS,      8,  0),
        NotifConfig("supps_pm",  "Integratori sera",       "💊", CH_SUPPS,     21,  0),
        NotifConfig("water_1",   "Idratazione mattina",    "💧", CH_HYDRATION, 10,  0),
        NotifConfig("water_2",   "Idratazione pomeriggio",  "💧", CH_HYDRATION, 15,  0),
        NotifConfig("water_3",   "Idratazione sera",       "💧", CH_HYDRATION, 20,  0),
    )
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    fun toggle(config: NotifConfig) {
        val updated = config.copy(
            enabled = !config.enabled,
            hour = if (config.hour == 0 && config.minute == 0) config.defaultHour else config.hour,
            minute = if (config.hour == 0 && config.minute == 0) config.defaultMinute else config.minute
        )
        updateConfig(updated)
        if (updated.enabled) scheduleNotification(updated)
        else NotificationWorker.cancelByTag(context, updated.id)
    }

    fun setTime(config: NotifConfig, hour: Int, minute: Int) {
        val updated = config.copy(hour = hour, minute = minute)
        updateConfig(updated)
        if (updated.enabled) scheduleNotification(updated)
    }

    private fun updateConfig(config: NotifConfig) {
        _state.update { st ->
            st.copy(configs = st.configs.map { if (it.id == config.id) config else it })
        }
    }

    private fun scheduleNotification(config: NotifConfig) {
        val nowMs = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, config.hour)
            set(java.util.Calendar.MINUTE, config.minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= nowMs) cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val delayMs = cal.timeInMillis - nowMs
        val messages = mapOf(
            "workout"   to "È ora di allenarti! 💪 Buona sessione!",
            "colazione" to "Buongiorno! Non dimenticare la colazione 🥣",
            "pranzo"    to "Ora di pranzo! 🍽️ Segui il tuo piano nutrizionale",
            "cena"      to "È ora di cena! 🍴 Prepara il tuo pasto pianificato",
            "supps_am"  to "Prendi i tuoi integratori del mattino 💊",
            "supps_pm"  to "Prendi i tuoi integratori serali 💊",
            "water_1"   to "Ricordati di bere! Obiettivo idratazione 💧",
            "water_2"   to "Idratati! A che punto sei con l'acqua oggi? 💧",
            "water_3"   to "Ultimo promemoria acqua della giornata 💧"
        )
        NotificationWorker.scheduleOnce(
            context = context,
            tag = config.id,
            delayMs = delayMs,
            title = config.label,
            message = messages[config.id] ?: config.label,
            channel = config.channel
        )
    }

    fun enableAll() = _state.value.configs.forEach { if (!it.enabled) toggle(it) }
    fun disableAll() = _state.value.configs.forEach { if (it.enabled) toggle(it) }
}

package it.fitlifepro.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import it.fitlifepro.app.FitLifeApplication.Companion.CH_HYDRATION
import it.fitlifepro.app.FitLifeApplication.Companion.CH_NUTRITION
import it.fitlifepro.app.FitLifeApplication.Companion.CH_SUPPS
import it.fitlifepro.app.FitLifeApplication.Companion.CH_WORKOUT
import it.fitlifepro.app.data.repository.FitLifeRepository
import it.fitlifepro.app.worker.NotificationWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotifConfig(
    val id: String,
    val label: String,
    val emoji: String,
    val channel: String,
    val defaultHour: Int,
    val defaultMinute: Int,
    val enabled: Boolean = false,
    val hour: Int = -1,      // -1 = not yet loaded from Excel
    val minute: Int = -1
) {
    val displayHour: Int get() = if (hour < 0) defaultHour else hour
    val displayMinute: Int get() = if (minute < 0) defaultMinute else minute
}

data class NotificationsUiState(
    val configs: List<NotifConfig> = defaultConfigs(),
    val isLoading: Boolean = true
)

private fun defaultConfigs() = listOf(
    NotifConfig("workout",   "Promemoria allenamento",  "🏋️", CH_WORKOUT,    7,  0),
    NotifConfig("colazione", "Colazione",               "🥣", CH_NUTRITION,   7, 30),
    NotifConfig("pranzo",    "Pranzo",                  "🍽️", CH_NUTRITION,  12, 30),
    NotifConfig("cena",      "Cena",                    "🍴", CH_NUTRITION,  19, 30),
    NotifConfig("supps_am",  "Integratori mattina",     "💊", CH_SUPPS,       8,  0),
    NotifConfig("supps_pm",  "Integratori sera",        "💊", CH_SUPPS,      21,  0),
    NotifConfig("water_1",   "Idratazione mattina",     "💧", CH_HYDRATION,  10,  0),
    NotifConfig("water_2",   "Idratazione pomeriggio",  "💧", CH_HYDRATION,  15,  0),
    NotifConfig("water_3",   "Idratazione sera",        "💧", CH_HYDRATION,  20,  0)
)

private val NOTIF_MESSAGES = mapOf(
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

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: FitLifeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init { loadTimesFromExcel() }

    /**
     * Reads meal/supplement/training times from the active program in the DB
     * and pre-fills the notification schedule with those times.
     */
    private fun loadTimesFromExcel() = viewModelScope.launch {
        repo.activeProgram.flatMapLatest { program ->
            if (program == null) {
                _state.update { it.copy(isLoading = false) }
                return@flatMapLatest kotlinx.coroutines.flow.emptyFlow()
            }
            combine(
                repo.getMealPlan(program.id),
                repo.getSupplements(program.id),
                repo.getTrainingDays(program.id)
            ) { meals, supps, days -> Triple(meals, supps, days) }
        }.collect { (meals, supps, days) ->
            val updated = _state.value.configs.map { cfg ->
                val t = when (cfg.id) {
                    "colazione" -> meals.firstOrNull { it.mealType.equals("Colazione", true) }?.timeHHMM
                    "pranzo"    -> meals.firstOrNull { it.mealType.equals("Pranzo", true) }?.timeHHMM
                    "cena"      -> meals.firstOrNull { it.mealType.equals("Cena", true) }?.timeHHMM
                    "supps_am"  -> supps.firstOrNull { it.timing.equals("Mattina", true) }?.timeHHMM
                    "supps_pm"  -> supps.firstOrNull { it.timing.equals("Sera", true) }?.timeHHMM
                    "workout"   -> days.firstOrNull()?.timeHHMM
                    else        -> null
                }
                val (h, m) = parseTime(t)
                if (h >= 0) cfg.copy(hour = h, minute = m) else cfg
            }
            _state.update { it.copy(configs = updated, isLoading = false) }
        }
    }

    private fun parseTime(hhmm: String?): Pair<Int, Int> {
        if (hhmm.isNullOrBlank()) return Pair(-1, -1)
        val parts = hhmm.split(":")
        if (parts.size < 2) return Pair(-1, -1)
        val h = parts[0].trim().toIntOrNull() ?: return Pair(-1, -1)
        val m = parts[1].trim().toIntOrNull() ?: return Pair(-1, -1)
        return Pair(h, m)
    }

    fun toggle(config: NotifConfig) {
        val updated = config.copy(enabled = !config.enabled)
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
        NotificationWorker.scheduleDaily(
            context = context,
            tag = config.id,
            hour = config.displayHour,
            minute = config.displayMinute,
            title = config.label,
            message = NOTIF_MESSAGES[config.id] ?: config.label,
            channel = config.channel
        )
    }

    fun enableAll() = _state.value.configs.forEach { if (!it.enabled) toggle(it) }
    fun disableAll() = _state.value.configs.forEach { if (it.enabled) toggle(it) }
}

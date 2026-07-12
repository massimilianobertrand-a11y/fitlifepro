
package it.fitlifepro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.fitlifepro.app.data.model.*
import it.fitlifepro.app.data.repository.FitLifeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HydrationUiState(
    val totalMl: Int = 0,
    val goalMl: Int = 3000,
    val logs: List<HydrationLog> = emptyList(),
    val config: HydrationConfig? = null,
    val reminders: List<HydrationReminder> = emptyList()
)

@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val repo: FitLifeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HydrationUiState())
    val state: StateFlow<HydrationUiState> = _state.asStateFlow()
    val today: String = LocalDate.now().toString()

    fun loadForProgram(programId: Long) = viewModelScope.launch {
        combine(
            repo.getHydrationTotal(today),
            repo.getHydrationLog(today),
            repo.getHydrationConfig(programId)
        ) { total, logs, cfg ->
            HydrationUiState(totalMl = total, goalMl = cfg?.dailyGoalMl ?: 3000, logs = logs, config = cfg)
        }.collect { _state.value = it }
    }

    fun logWater(ml: Int) = viewModelScope.launch { repo.logWater(ml) }
    fun deleteEntry(id: Long) = viewModelScope.launch { repo.deleteHydrationEntry(id) }
}

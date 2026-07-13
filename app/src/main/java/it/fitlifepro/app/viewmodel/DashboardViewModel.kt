
package it.fitlifepro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.fitlifepro.app.data.model.*
import it.fitlifepro.app.data.repository.FitLifeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val program: Program? = null,
    val todayDay: TrainingDay? = null,
    val todayMeals: List<MealPlan> = emptyList(),
    val todaySupplements: List<Supplement> = emptyList(),
    val waterTodayMl: Int = 0,
    val waterGoalMl: Int = 3000,
    val completedSessions: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: FitLifeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    val today: String = LocalDate.now().toString()
    val todayDayName: String = LocalDate.now().dayOfWeek
        .getDisplayName(TextStyle.FULL, Locale.ITALIAN)
        .replaceFirstChar { it.uppercase() }

    init { load() }

    fun load() = viewModelScope.launch {
        repo.activeProgram.collect { program ->
            if (program == null) {
                _state.update { it.copy(isLoading = false) }
                return@collect
            }
            combine(
                repo.getTrainingDays(program.id),
                repo.getMealsByDay(program.id, todayDayName),
                repo.getSupplements(program.id),
                repo.getHydrationTotal(today),
                repo.getHydrationConfig(program.id)
            ) { days, meals, supps, water, hydCfg ->
                val todayDay = days.find { it.dayOfWeek.equals(todayDayName, true) }
                DashboardUiState(
                    program = program,
                    todayDay = todayDay,
                    todayMeals = meals,
                    todaySupplements = supps.filter { filterSuppForToday(it, todayDay != null) },
                    waterTodayMl = water,
                    waterGoalMl = (hydCfg?.dailyGoalMl ?: 3000) +
                        if (todayDay != null) hydCfg?.extraTrainingMl ?: 0 else 0,
                    completedSessions = 0,
                    isLoading = false
                )
            }.combine(repo.getRecentSessions(30)) { s, sessions ->
                s.copy(completedSessions = sessions.count { it.completed })
            }.collect { s -> _state.value = s }
        }
    }

    private fun filterSuppForToday(s: Supplement, isTrainingDay: Boolean): Boolean = when (s.days) {
        "Solo_allenamento" -> isTrainingDay
        "Solo_riposo" -> !isTrainingDay
        else -> true
    }
}

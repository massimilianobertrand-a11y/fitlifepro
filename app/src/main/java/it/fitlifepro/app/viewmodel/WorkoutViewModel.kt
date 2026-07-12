
package it.fitlifepro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.fitlifepro.app.data.model.*
import it.fitlifepro.app.data.repository.FitLifeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WorkoutPhase { IDLE, ACTIVE, REST, DONE }

data class WorkoutUiState(
    val trainingDays: List<TrainingDay> = emptyList(),
    val selectedDay: TrainingDay? = null,
    val exercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSet: Int = 1,
    val phase: WorkoutPhase = WorkoutPhase.IDLE,
    val restSecondsLeft: Int = 0,
    val sessionId: Long? = null,
    val performedSets: List<PerformedSet> = emptyList(),
    val elapsedSeconds: Int = 0
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repo: FitLifeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state.asStateFlow()

    private var restJob: Job? = null
    private var timerJob: Job? = null
    private var programId: Long = -1L

    fun loadProgram(programId: Long) {
        this.programId = programId
        viewModelScope.launch {
            repo.getTrainingDays(programId).collect { days ->
                _state.update { it.copy(trainingDays = days) }
            }
        }
    }

    fun selectDay(day: TrainingDay) = viewModelScope.launch {
        val exercises = repo.getExercisesSync(day.id)
        _state.update { it.copy(selectedDay = day, exercises = exercises, currentExerciseIndex = 0, currentSet = 1) }
    }

    fun startSession() = viewModelScope.launch {
        val day = _state.value.selectedDay ?: return@launch
        val id = repo.startSession(day.id)
        _state.update { it.copy(sessionId = id, phase = WorkoutPhase.ACTIVE, elapsedSeconds = 0) }
        startElapsedTimer()
    }

    fun logSet(weightKg: Float, repsActual: Int) = viewModelScope.launch {
        val st = _state.value
        val ex = st.exercises.getOrNull(st.currentExerciseIndex) ?: return@launch
        val sid = st.sessionId ?: return@launch
        repo.logSet(PerformedSet(
            sessionId = sid, exerciseName = ex.name,
            setNumber = st.currentSet, repsActual = repsActual,
            weightActual = weightKg, restSec = ex.restSec
        ))
        startRest(ex.restSec)
        val nextSet = if (st.currentSet >= ex.sets) {
            _state.update { it.copy(currentExerciseIndex = it.currentExerciseIndex + 1, currentSet = 1) }
            1
        } else {
            st.currentSet + 1
        }
        if (_state.value.currentExerciseIndex >= st.exercises.size) {
            finishSession()
        } else {
            _state.update { it.copy(currentSet = nextSet) }
        }
    }

    fun skipRest() { restJob?.cancel(); _state.update { it.copy(phase = WorkoutPhase.ACTIVE, restSecondsLeft = 0) } }

    fun finishSession() = viewModelScope.launch {
        timerJob?.cancel(); restJob?.cancel()
        val sid = _state.value.sessionId ?: return@launch
        val session = repo.getRecentSessions(1).first().firstOrNull { it.id == sid } ?: return@launch
        repo.completeSession(session)
        _state.update { it.copy(phase = WorkoutPhase.DONE) }
    }

    private fun startRest(seconds: Int) {
        restJob?.cancel()
        _state.update { it.copy(phase = WorkoutPhase.REST, restSecondsLeft = seconds) }
        restJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _state.update { it.copy(restSecondsLeft = i) }
                delay(1000)
            }
            _state.update { it.copy(phase = WorkoutPhase.ACTIVE, restSecondsLeft = 0) }
        }
    }

    private fun startElapsedTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) { delay(1000); _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) } }
        }
    }
}

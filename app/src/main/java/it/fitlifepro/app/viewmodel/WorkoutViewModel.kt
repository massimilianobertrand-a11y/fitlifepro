
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
    val currentExerciseId: Long? = null,
    val currentSet: Int = 1,
    val completedExerciseIds: Set<Long> = emptySet(),
    val phase: WorkoutPhase = WorkoutPhase.IDLE,
    val restSecondsLeft: Int = 0,
    val sessionId: Long? = null,
    val performedSets: List<PerformedSet> = emptyList(),
    val elapsedSeconds: Int = 0
) {
    val currentExercise: Exercise? get() = exercises.firstOrNull { it.id == currentExerciseId }
    val remainingExercises: List<Exercise> get() = exercises.filter { it.id !in completedExerciseIds }
    val completedCount: Int get() = completedExerciseIds.size
}

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
        _state.update { it.copy(
            selectedDay = day,
            exercises = exercises,
            currentExerciseId = exercises.firstOrNull()?.id,
            currentSet = 1,
            completedExerciseIds = emptySet()
        )}
    }

    fun startSession() = viewModelScope.launch {
        val day = _state.value.selectedDay ?: return@launch
        val id = repo.startSession(day.id)
        _state.update { it.copy(sessionId = id, phase = WorkoutPhase.ACTIVE, elapsedSeconds = 0) }
        startElapsedTimer()
    }

    /** Salta a un esercizio specifico (macchinario libero) */
    fun jumpToExercise(exercise: Exercise) {
        restJob?.cancel()
        _state.update { it.copy(
            currentExerciseId = exercise.id,
            currentSet = 1,
            phase = WorkoutPhase.ACTIVE,
            restSecondsLeft = 0
        )}
    }

    fun logSet(weightKg: Float, repsActual: Int) = viewModelScope.launch {
        val st = _state.value
        val ex = st.currentExercise ?: return@launch
        val sid = st.sessionId ?: return@launch
        repo.logSet(PerformedSet(
            sessionId = sid, exerciseName = ex.name,
            setNumber = st.currentSet, repsActual = repsActual,
            weightActual = weightKg, restSec = ex.restSec
        ))
        startRest(ex.restSec)
        if (st.currentSet >= ex.sets) {
            // Esercizio completato — aggiungilo ai completati
            val newCompleted = st.completedExerciseIds + ex.id
            val remaining = st.exercises.filter { it.id !in newCompleted }
            if (remaining.isEmpty()) {
                _state.update { it.copy(completedExerciseIds = newCompleted) }
                finishSession()
            } else {
                // Auto-seleziona il prossimo esercizio rimanente
                _state.update { it.copy(
                    completedExerciseIds = newCompleted,
                    currentExerciseId = remaining.first().id,
                    currentSet = 1
                )}
            }
        } else {
            _state.update { it.copy(currentSet = st.currentSet + 1) }
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

    fun updateExerciseMedia(exercise: Exercise, url: String) = viewModelScope.launch {
        val updated = exercise.copy(videoUrl = url)
        repo.updateExercise(updated)
        // Refresh the exercises list in state
        _state.update { st ->
            st.copy(exercises = st.exercises.map { if (it.id == exercise.id) updated else it })
        }
    }

    private fun startElapsedTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) { delay(1000); _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) } }
        }
    }
}

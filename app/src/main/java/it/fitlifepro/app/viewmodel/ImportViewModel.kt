
package it.fitlifepro.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.fitlifepro.app.data.model.*
import it.fitlifepro.app.data.repository.FitLifeRepository
import it.fitlifepro.app.excel.ExcelImporter
import it.fitlifepro.app.excel.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Loading : ImportUiState()
    data class Preview(val result: ImportResult) : ImportUiState()
    data class Success(val programName: String) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val repo: FitLifeRepository,
    private val importer: ExcelImporter
) : ViewModel() {

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    fun parseFile(context: Context, uri: Uri) = viewModelScope.launch {
        _state.value = ImportUiState.Loading
        val result = withContext(Dispatchers.IO) { importer.import(context, uri) }
        _state.value = if (result.errors.isEmpty() || result.program.name != "?")
            ImportUiState.Preview(result)
        else
            ImportUiState.Error(result.errors.joinToString("\n"))
    }

    fun confirmImport(result: ImportResult) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            // Deactivate all, save new program
            val programId = repo.saveProgram(result.program.copy(isActive = true))
            // Save training days, map exercises by day name
            val dayNameToId = mutableMapOf<String, Long>()
            result.trainingDays.forEach { day ->
                val id = repo.saveTrainingDay(day.copy(programId = programId))
                dayNameToId[day.dayOfWeek] = id
            }
            // Save exercises — match by day name stored in notes marker
            result.exercises.forEach { ex ->
                val dayName = ex.notes.removePrefix("day:")
                val dayId = dayNameToId[dayName] ?: return@forEach
                repo.saveExercise(ex.copy(trainingDayId = dayId, notes = ""))
            }
            // Save meals
            repo.saveMeals(result.meals.map { it.copy(programId = programId) })
            // Save supplements
            repo.saveSupplements(result.supplements.map { it.copy(programId = programId) })
            // Save hydration config + reminders
            result.hydrationConfig?.let { cfg ->
                val cfgId = repo.saveHydrationConfig(cfg.copy(programId = programId))
                repo.saveHydrationReminders(result.hydrationReminders.map { it.copy(configId = cfgId) })
            }
            // Activate program
            repo.activateProgram(programId)
        }
        _state.value = ImportUiState.Success(result.program.name)
    }

    fun reset() { _state.value = ImportUiState.Idle }
}

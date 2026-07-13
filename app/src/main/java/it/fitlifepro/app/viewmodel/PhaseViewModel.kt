package it.fitlifepro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.fitlifepro.app.data.model.Program
import it.fitlifepro.app.data.repository.FitLifeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhaseUiState(
    val allPhases: List<Program> = emptyList(),
    val activePhase: Program? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class PhaseViewModel @Inject constructor(
    private val repo: FitLifeRepository
) : ViewModel() {

    val state: StateFlow<PhaseUiState> = combine(
        repo.allPrograms,
        repo.activeProgram
    ) { all, active ->
        PhaseUiState(
            allPhases = all,
            activePhase = active,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PhaseUiState(isLoading = true)
    )

    fun switchPhase(programId: Long) = viewModelScope.launch {
        repo.activateProgram(programId)
    }

    fun deletePhase(program: Program) = viewModelScope.launch {
        repo.deleteProgram(program)
    }
}

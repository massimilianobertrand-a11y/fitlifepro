
package it.fitlifepro.app.ui.screens.supplements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.fitlifepro.app.data.model.Supplement
import it.fitlifepro.app.data.repository.FitLifeRepository
import it.fitlifepro.app.ui.components.*
import it.fitlifepro.app.ui.theme.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SupplementsState(val supplements: List<Supplement> = emptyList(), val takenIds: Set<Long> = emptySet())

@HiltViewModel
class SupplementsViewModel @Inject constructor(private val repo: FitLifeRepository) : ViewModel() {
    private val _state = MutableStateFlow(SupplementsState())
    val state: StateFlow<SupplementsState> = _state.asStateFlow()
    val today = LocalDate.now().toString()

    fun load(programId: Long) = viewModelScope.launch {
        combine(repo.getSupplements(programId), repo.getSupplementLog(today)) { supps, logs ->
            SupplementsState(supps, logs.filter { it.taken }.map { it.supplementId }.toSet())
        }.collect { _state.value = it }
    }
    fun toggle(suppId: Long) = viewModelScope.launch {
        if (suppId in _state.value.takenIds) repo.unlogSupplement(today, suppId)
        else repo.logSupplement(suppId, today)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementsScreen(programId: Long, vm: SupplementsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    LaunchedEffect(programId) { vm.load(programId) }

    val timings = state.supplements.map { it.timing }.distinct().sorted()
    val takenCount = state.takenIds.size
    val totalCount = state.supplements.size

    Scaffold(topBar = {
        TopAppBar(title = { Text("Integratori 💊") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple500,
                titleContentColor = Color.White))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SectionCard {
                    LinearProgressRow("Presi oggi", takenCount, totalCount, Purple500)
                }
            }
            timings.forEach { timing ->
                val group = state.supplements.filter { it.timing == timing }
                item {
                    SectionHeader(timing.replace("_"," ").replaceFirstChar { it.uppercase() },
                        Purple500, Icons.Default.Schedule)
                }
                items(group) { supp ->
                    val taken = supp.id in state.takenIds
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(supp.name, style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (taken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.width(6.dp))
                                    if (supp.brand.isNotBlank())
                                        Text(supp.brand, style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline)
                                }
                                Text("${supp.dosage} · ${supp.timeHHMM} · ${supp.takeWith}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Purple500.copy(alpha = 0.8f))
                                if (supp.notes.isNotBlank())
                                    Text(supp.notes, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline)
                            }
                            Checkbox(checked = taken, onCheckedChange = { vm.toggle(supp.id) },
                                colors = CheckboxDefaults.colors(checkedColor = Purple500))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

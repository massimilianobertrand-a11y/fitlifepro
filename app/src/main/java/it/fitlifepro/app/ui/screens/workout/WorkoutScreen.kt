
package it.fitlifepro.app.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.fitlifepro.app.ui.components.*
import it.fitlifepro.app.ui.theme.*
import it.fitlifepro.app.viewmodel.WorkoutPhase
import it.fitlifepro.app.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(programId: Long, vm: WorkoutViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }

    LaunchedEffect(programId) { vm.loadProgram(programId) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Allenamento") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Orange500,
                titleContentColor = androidx.compose.ui.graphics.Color.White)
        )
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Day selector
            if (state.phase == WorkoutPhase.IDLE) {
                item {
                    SectionCard {
                        SectionHeader("Seleziona sessione", Orange500, Icons.Default.CalendarToday)
                        Spacer(Modifier.height(12.dp))
                        state.trainingDays.forEach { day ->
                            OutlinedButton(
                                onClick = { vm.selectDay(day) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.FitnessCenter, null, tint = Orange500)
                                Spacer(Modifier.width(8.dp))
                                Text("${day.dayOfWeek} — ${day.sessionType}")
                                Spacer(Modifier.weight(1f))
                                Text("${day.timeHHMM}", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
                state.selectedDay?.let { day ->
                    item {
                        SectionCard {
                            SectionHeader("Esercizi — ${day.dayOfWeek}", Orange500, Icons.Default.List)
                            Spacer(Modifier.height(12.dp))
                            state.exercises.forEach { ex ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("${ex.order}.", color = Orange500,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.width(24.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(ex.name, style = MaterialTheme.typography.bodyLarge)
                                        Text("${ex.sets}×${ex.reps} · ${ex.weightKg}kg · ${ex.restSec}s riposo",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { vm.startSession() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Inizia Allenamento")
                            }
                        }
                    }
                }
            }

            // Active workout
            if (state.phase == WorkoutPhase.ACTIVE || state.phase == WorkoutPhase.REST) {
                val ex = state.exercises.getOrNull(state.currentExerciseIndex)
                item {
                    SectionCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("⏱ ${state.elapsedSeconds / 60}m ${state.elapsedSeconds % 60}s",
                                color = Orange500, style = MaterialTheme.typography.labelLarge)
                            Text("Es. ${state.currentExerciseIndex + 1}/${state.exercises.size}",
                                color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(8.dp))
                        ex?.let {
                            Text(it.name, style = MaterialTheme.typography.headlineMedium, color = Orange500)
                            Text("${it.muscleGroup} · Serie ${state.currentSet}/${it.sets}",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(12.dp))
                            if (state.phase == WorkoutPhase.REST) {
                                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("RECUPERO", style = MaterialTheme.typography.labelLarge, color = Blue500)
                                    Text("${state.restSecondsLeft}s", style = MaterialTheme.typography.headlineLarge,
                                        color = Blue500)
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = { vm.skipRest() }) { Text("Salta recupero") }
                                }
                            } else {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        label = { Text("Kg") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = repsInput,
                                        onValueChange = { repsInput = it },
                                        label = { Text("Rip.") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        vm.logSet(
                                            weightInput.toFloatOrNull() ?: it.weightKg,
                                            repsInput.toIntOrNull() ?: it.reps
                                        )
                                        weightInput = ""; repsInput = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                                ) {
                                    Icon(Icons.Default.Check, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Serie completata")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { vm.finishSession() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Termina sessione") }
                    }
                }
            }

            // Done
            if (state.phase == WorkoutPhase.DONE) {
                item {
                    SectionCard {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Amber500, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Sessione completata! 💪", style = MaterialTheme.typography.headlineMedium)
                            Text("Durata: ${state.elapsedSeconds / 60} minuti",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

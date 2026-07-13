
package it.fitlifepro.app.ui.screens.workout

import android.content.Context
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.fitlifepro.app.data.model.Exercise
import it.fitlifepro.app.ui.components.*
import it.fitlifepro.app.ui.theme.*
import it.fitlifepro.app.viewmodel.WorkoutEvent
import it.fitlifepro.app.viewmodel.WorkoutPhase
import it.fitlifepro.app.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(programId: Long, vm: WorkoutViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var mediaSheetExercise by remember { mutableStateOf<Exercise?>(null) }
    var showExercisePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(programId) { vm.loadProgram(programId) }

    // Audio + vibrazione al termine del recupero
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is WorkoutEvent.RestEnded -> {
                    // Vibrazione: 3 impulsi brevi
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(
                            longArrayOf(0, 200, 100, 200, 100, 400), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 200, 100, 200, 100, 400), -1)
                    }
                    // Beep audio: 3 toni brevi
                    try {
                        val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                        kotlinx.coroutines.delay(350)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                        kotlinx.coroutines.delay(350)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
                        kotlinx.coroutines.delay(500)
                        tg.release()
                    } catch (_: Exception) { }
                }
            }
        }
    }

    // Media bottom sheet
    mediaSheetExercise?.let { ex ->
        ExerciseMediaSheet(
            exercise = ex,
            onDismiss = { mediaSheetExercise = null },
            onSave = { exercise, url ->
                vm.updateExerciseMedia(exercise, url)
                mediaSheetExercise = null
            }
        )
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Allenamento") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Orange500,
                titleContentColor = Color.White
            )
        )
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── IDLE: Day selector + exercise list ──────────────────────
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
                                Text(day.timeHHMM, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                state.selectedDay?.let { day ->
                    item {
                        SectionCard {
                            SectionHeader(
                                "Esercizi — ${day.dayOfWeek}",
                                Orange500,
                                Icons.AutoMirrored.Filled.List
                            )
                            Spacer(Modifier.height(12.dp))
                            state.exercises.forEach { ex ->
                                ExerciseRow(
                                    exercise = ex,
                                    onMediaClick = { mediaSheetExercise = ex }
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
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

            // ── ACTIVE / REST ───────────────────────────────────────────
            if (state.phase == WorkoutPhase.ACTIVE || state.phase == WorkoutPhase.REST) {
                val ex = state.currentExercise
                item {
                    SectionCard {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⏱ ${state.elapsedSeconds / 60}m ${state.elapsedSeconds % 60}s",
                                color = Orange500,
                                style = MaterialTheme.typography.labelLarge
                            )
                            // Bottone cambia esercizio
                            OutlinedButton(
                                onClick = { showExercisePicker = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${state.completedCount}/${state.exercises.size}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Exercise picker sheet
                        if (showExercisePicker) {
                            ExercisePickerSheet(
                                remainingExercises = state.remainingExercises,
                                currentExerciseId = state.currentExerciseId,
                                completedExerciseIds = state.completedExerciseIds,
                                allExercises = state.exercises,
                                onPick = { picked ->
                                    vm.jumpToExercise(picked)
                                    showExercisePicker = false
                                },
                                onDismiss = { showExercisePicker = false }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        ex?.let {
                            // Exercise name row with media button
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        it.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Orange500
                                    )
                                    Text(
                                        "${it.muscleGroup} · Serie ${state.currentSet}/${it.sets}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                // Media icon button — orange if media exists, grey otherwise
                                IconButton(onClick = { mediaSheetExercise = ex }) {
                                    Icon(
                                        imageVector = if (it.videoUrl.isNotBlank())
                                            Icons.Default.SmartDisplay
                                        else
                                            Icons.Default.VideoLibrary,
                                        contentDescription = "Media esercizio",
                                        tint = if (it.videoUrl.isNotBlank()) Orange500
                                               else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))

                            // Media preview inline — sempre visibile se assegnato
                            ExerciseMediaPreview(
                                videoUrl = it.videoUrl,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (it.videoUrl.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                            }

                            if (state.phase == WorkoutPhase.REST) {
                                Column(
                                    Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "RECUPERO",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Blue500
                                    )
                                    Text(
                                        "${state.restSecondsLeft}s",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Blue500
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = { vm.skipRest() }) {
                                        Text("Salta recupero")
                                    }
                                }
                            } else {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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

            // ── DONE ────────────────────────────────────────────────────
            if (state.phase == WorkoutPhase.DONE) {
                item {
                    SectionCard {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents, null,
                                tint = Amber500,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Sessione completata! 💪",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                "Durata: ${state.elapsedSeconds / 60} minuti",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/** Riga singolo esercizio con indicatore media e pulsante 🎬 */
@Composable
private fun ExerciseRow(
    exercise: Exercise,
    onMediaClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Order number
        Text(
            "${exercise.order}.",
            color = Orange500,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(28.dp)
        )
        // Name + detail
        Column(Modifier.weight(1f)) {
            Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${exercise.sets}×${exercise.reps} · ${exercise.weightKg}kg · ${exercise.restSec}s riposo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        // Media button — filled orange if URL exists, outline grey if not
        IconButton(
            onClick = onMediaClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (exercise.videoUrl.isNotBlank())
                    Icons.Default.SmartDisplay
                else
                    Icons.Default.VideoLibrary,
                contentDescription = if (exercise.videoUrl.isNotBlank())
                    "Video assegnato — tocca per modificare"
                else
                    "Assegna video o immagine",
                tint = if (exercise.videoUrl.isNotBlank()) Orange500
                       else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

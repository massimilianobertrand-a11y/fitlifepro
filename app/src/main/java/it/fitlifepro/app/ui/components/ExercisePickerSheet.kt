package it.fitlifepro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.fitlifepro.app.data.model.Exercise
import it.fitlifepro.app.ui.theme.*

/**
 * Bottom sheet per scegliere un esercizio alternativo durante la sessione.
 * Mostra tutti gli esercizi della sessione NON ancora completati.
 * L'esercizio corrente è evidenziato ma non selezionabile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    remainingExercises: List<Exercise>,
    currentExerciseId: Long?,
    completedExerciseIds: Set<Long>,
    allExercises: List<Exercise>,
    onPick: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = Orange500,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Scegli esercizio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${remainingExercises.size} rimanenti · ${completedExerciseIds.size}/${allExercises.size} completati",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { if (allExercises.isEmpty()) 0f else completedExerciseIds.size.toFloat() / allExercises.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Green500,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Esercizi rimanenti (selezionabili)
                items(remainingExercises, key = { it.id }) { ex ->
                    val isCurrent = ex.id == currentExerciseId
                    ExercisePickerRow(
                        exercise = ex,
                        isCurrent = isCurrent,
                        isCompleted = false,
                        onClick = if (isCurrent) null else ({ onPick(ex) })
                    )
                }

                // Separatore se ci sono completati
                if (completedExerciseIds.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                "  Completati  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                    }
                    items(
                        allExercises.filter { it.id in completedExerciseIds },
                        key = { "done_${it.id}" }
                    ) { ex ->
                        ExercisePickerRow(
                            exercise = ex,
                            isCurrent = false,
                            isCompleted = true,
                            onClick = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerRow(
    exercise: Exercise,
    isCurrent: Boolean,
    isCompleted: Boolean,
    onClick: (() -> Unit)?
) {
    val containerColor = when {
        isCurrent -> Orange500.copy(alpha = 0.12f)
        isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = when {
        isCurrent -> Orange500
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon
        Icon(
            imageVector = when {
                isCompleted -> Icons.Default.CheckCircle
                isCurrent -> Icons.Default.PlayCircle
                else -> Icons.Default.FitnessCenter
            },
            contentDescription = null,
            tint = when {
                isCompleted -> Green500
                isCurrent -> Orange500
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                exercise.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${exercise.muscleGroup} · ${exercise.sets}×${exercise.reps} · ${exercise.weightKg}kg",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isCurrent) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Orange500.copy(alpha = 0.2f)
            ) {
                Text(
                    "IN CORSO",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Orange500,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (!isCompleted) {
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Seleziona",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

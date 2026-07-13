package it.fitlifepro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.fitlifepro.app.data.model.Program
import it.fitlifepro.app.ui.theme.*

/**
 * Banner chip always visible at the top of the app showing the active phase.
 * Tapping it opens [PhaseSwitcherSheet].
 */
@Composable
fun ActivePhaseChip(
    activePhase: Program?,
    phaseCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePhase == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Indigo500,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "FASE ATTIVA",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = activePhase.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (phaseCount > 1) {
                    Badge(
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White
                    ) {
                        Text("$phaseCount", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Cambia fase",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Bottom sheet listing all phases with ability to switch or delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseSwitcherSheet(
    phases: List<Program>,
    activePhaseId: Long?,
    onSwitchPhase: (Long) -> Unit,
    onDeletePhase: (Program) -> Unit,
    onDismiss: () -> Unit
) {
    var phaseToDelete by remember { mutableStateOf<Program?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = null,
                    tint = Indigo500,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Le tue Fasi di Allenamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Seleziona una fase per attivarla",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 46.dp, bottom = 8.dp)
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            if (phases.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nessuna fase disponibile.\nImporta un file Excel per iniziare.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(phases, key = { it.id }) { phase ->
                        PhaseItem(
                            phase = phase,
                            isActive = phase.id == activePhaseId,
                            onSelect = {
                                onSwitchPhase(phase.id)
                                onDismiss()
                            },
                            onDelete = {
                                phaseToDelete = phase
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    phaseToDelete?.let { phase ->
        AlertDialog(
            onDismissRequest = { phaseToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Elimina fase") },
            text = {
                Text(
                    "Vuoi eliminare la fase \"${phase.name}\"?\n" +
                    "Tutti i dati associati (allenamenti, pasti, integratori) saranno eliminati definitivamente."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePhase(phase)
                        phaseToDelete = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { phaseToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun PhaseItem(
    phase: Program,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = if (isActive)
        Indigo500.copy(alpha = 0.1f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val borderColor = if (isActive) Indigo500 else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Active indicator dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Indigo500 else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = phase.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) Indigo500 else MaterialTheme.colorScheme.onSurface
                    )
                    if (isActive) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Indigo500,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ATTIVA",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = buildString {
                        append(phase.goal.replaceFirstChar { it.uppercase() })
                        if (phase.startDate.isNotEmpty()) append(" · ${phase.startDate}")
                        if (phase.athleteName.isNotEmpty()) append(" · ${phase.athleteName}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Delete button (only if not the only phase)
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Elimina",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

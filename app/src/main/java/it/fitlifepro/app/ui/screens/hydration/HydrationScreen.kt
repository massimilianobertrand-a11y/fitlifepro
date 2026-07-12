
package it.fitlifepro.app.ui.screens.hydration

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
import it.fitlifepro.app.viewmodel.HydrationViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(programId: Long, vm: HydrationViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val amounts = listOf(150, 250, 350, 500)

    LaunchedEffect(programId) { vm.loadForProgram(programId) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Idratazione 💧") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue500,
                titleContentColor = androidx.compose.ui.graphics.Color.White)
        )
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    LinearProgressRow("Acqua oggi", state.totalMl, state.goalMl, Blue500, "ml")
                    Spacer(Modifier.height(8.dp))
                    val pct = if (state.goalMl > 0) (state.totalMl * 100 / state.goalMl) else 0
                    Text("${state.totalMl} ml · $pct% dell'obiettivo",
                        style = MaterialTheme.typography.bodyMedium, color = Blue500)
                }
            }
            item {
                SectionCard {
                    SectionHeader("Aggiungi acqua", Blue500, Icons.Default.Add)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        amounts.forEach { ml ->
                            Button(
                                onClick = { vm.logWater(ml) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) { Text("${ml}ml", style = MaterialTheme.typography.labelMedium) }
                        }
                    }
                }
            }
            if (state.logs.isNotEmpty()) {
                item { SectionHeader("Log di oggi", Blue500, Icons.Default.History) }
                items(state.logs.reversed()) { log ->
                    SectionCard {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Water, null, tint = Blue500, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("${log.amountMl} ml", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            val time = java.time.Instant.ofEpochMilli(log.timestamp)
                                .atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            IconButton(onClick = { vm.deleteEntry(log.id) }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

package it.fitlifepro.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import it.fitlifepro.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: DashboardViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FitLife Pro", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Indigo500)
            }
            return@Scaffold
        }
        if (state.program == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.UploadFile, null, tint = Indigo500, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Nessun programma attivo", style = MaterialTheme.typography.titleMedium)
                    Text("Importa il tuo Excel per iniziare",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Ciao, ${state.program?.athleteName ?: ""}!",
                    style = MaterialTheme.typography.headlineMedium)
                Text(state.program?.name ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline)
            }
            item {
                SectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatChip(Icons.Default.FitnessCenter, "${state.completedSessions}",
                            "Sessioni", Orange500, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        StatChip(Icons.Default.MonitorWeight, "${state.program?.weightKg ?: 0} kg",
                            "Peso", Green500, Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        StatChip(Icons.Default.Water, "${state.waterTodayMl / 1000f} L",
                            "Acqua", Blue500, Modifier.weight(1f))
                    }
                }
            }
            item {
                SectionCard {
                    SectionHeader("Oggi — ${vm.todayDayName}", Indigo500, Icons.Default.CalendarToday)
                    Spacer(Modifier.height(12.dp))
                    if (state.todayDay != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, null, tint = Orange500,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(state.todayDay!!.sessionType,
                                    style = MaterialTheme.typography.bodyLarge, color = Orange500)
                                Text("Ore ${state.todayDay!!.timeHHMM} · ${state.todayDay!!.durationMin} min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    } else {
                        Text("Giorno di riposo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    if (state.todayMeals.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("${state.todayMeals.size} pasti pianificati",
                            style = MaterialTheme.typography.bodyMedium, color = Green500)
                    }
                    if (state.todaySupplements.isNotEmpty()) {
                        Text("${state.todaySupplements.size} integratori da assumere",
                            style = MaterialTheme.typography.bodyMedium, color = Purple500)
                    }
                }
            }
            item {
                SectionCard {
                    SectionHeader("Idratazione", Blue500, Icons.Default.Water)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressRow(
                        label = "Acqua oggi",
                        current = state.waterTodayMl,
                        goal = state.waterGoalMl,
                        color = Blue500,
                        unit = "ml"
                    )
                    Spacer(Modifier.height(4.dp))
                    val pct = if (state.waterGoalMl > 0) (state.waterTodayMl * 100 / state.waterGoalMl) else 0
                    Text("$pct% dell’obiettivo giornaliero",
                        style = MaterialTheme.typography.bodyMedium, color = Blue500)
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

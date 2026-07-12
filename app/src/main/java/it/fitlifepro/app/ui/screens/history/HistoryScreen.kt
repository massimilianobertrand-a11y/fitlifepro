package it.fitlifepro.app.ui.screens.history

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
import it.fitlifepro.app.data.repository.FitLifeRepository
import it.fitlifepro.app.ui.components.*
import it.fitlifepro.app.ui.theme.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.fitlifepro.app.data.model.WorkoutSession
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val repo: FitLifeRepository) : ViewModel() {
    val sessions: StateFlow<List<WorkoutSession>> = repo.getRecentSessions(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: HistoryViewModel = hiltViewModel()) {
    val sessions by vm.sessions.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Storico") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Amber500,
                titleContentColor = androidx.compose.ui.graphics.Color.White
            )
        )
    }) { padding ->
        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("Nessuna sessione registrata
Avvia il tuo primo allenamento!",
                    Icons.Default.FitnessCenter)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (session.completed) Icons.Default.CheckCircle
                                else Icons.Default.RadioButtonUnchecked,
                                null,
                                tint = if (session.completed) Green500 else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(session.date, style = MaterialTheme.typography.bodyLarge)
                                val dur = session.endTime?.let {
                                    ((it - session.startTime) / 60000).toInt()
                                }
                                Text(
                                    buildString {
                                        if (dur != null) append("${dur} min")
                                        session.avgHeartRate?.let { append(" · FC $it bpm") }
                                        session.caloriesBurned?.let { append(" · $it kcal") }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (session.completed)
                                Icon(Icons.Default.EmojiEvents, null,
                                    tint = Amber500, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

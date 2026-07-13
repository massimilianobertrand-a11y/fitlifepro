package it.fitlifepro.app.ui.screens.notifications

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.fitlifepro.app.ui.theme.*
import it.fitlifepro.app.viewmodel.NotifConfig
import it.fitlifepro.app.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(vm: NotificationsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🔔 Notifiche", style = MaterialTheme.typography.titleLarge)
                        val active = state.configs.count { it.enabled }
                        Text(
                            "$active/${state.configs.size} attive",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (state.configs.any { !it.enabled }) vm.enableAll()
                        else vm.disableAll()
                    }) {
                        Text(
                            if (state.configs.any { !it.enabled }) "Attiva tutte"
                            else "Disattiva tutte",
                            color = Orange500
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Grouped by channel
            val grouped = state.configs.groupBy { it.channel }
            val channelLabels = mapOf(
                "ch_workout"  to "🏋️ Allenamento",
                "ch_nutrition" to "🍽️ Nutrizione",
                "ch_supps"    to "💊 Integratori",
                "ch_hydration" to "💧 Idratazione"
            )
            grouped.forEach { (channel, configs) ->
                item(key = "hdr_$channel") {
                    Text(
                        channelLabels[channel] ?: channel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Orange500,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                items(configs, key = { it.id }) { config ->
                    NotifConfigCard(
                        config = config,
                        onToggle = { vm.toggle(config) },
                        onTimeClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> vm.setTime(config, hour, minute) },
                                if (config.hour == 0) config.defaultHour else config.hour,
                                if (config.minute == 0) config.defaultMinute else config.minute,
                                true
                            ).show()
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info, null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Le notifiche vengono schedulate per il giorno stesso. " +
                            "Riapri l'app ogni giorno per rinnovarle, " +
                            "oppure abilita il boot receiver per il reschedule automatico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifConfigCard(
    config: NotifConfig,
    onToggle: () -> Unit,
    onTimeClick: () -> Unit
) {
    val displayHour = if (config.hour == 0 && !config.enabled) config.defaultHour else config.hour
    val displayMin  = if (config.minute == 0 && !config.enabled) config.defaultMinute else config.minute

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (config.enabled)
                Orange500.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(config.emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    config.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (config.enabled) FontWeight.SemiBold else FontWeight.Normal
                )
                if (config.enabled) {
                    TextButton(
                        onClick = onTimeClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule, null,
                            modifier = Modifier.size(14.dp),
                            tint = Orange500
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "%02d:%02d".format(displayHour, displayMin),
                            style = MaterialTheme.typography.labelMedium,
                            color = Orange500
                        )
                    }
                } else {
                    Text(
                        "%02d:%02d".format(displayHour, displayMin),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Switch(
                checked = config.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = Orange500, checkedTrackColor = Orange500.copy(alpha = 0.4f))
            )
        }
    }
}

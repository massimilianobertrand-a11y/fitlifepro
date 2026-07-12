
package it.fitlifepro.app.ui.screens.`import`

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.fitlifepro.app.ui.components.SectionCard
import it.fitlifepro.app.ui.components.SectionHeader
import it.fitlifepro.app.ui.theme.*
import it.fitlifepro.app.viewmodel.ImportUiState
import it.fitlifepro.app.viewmodel.ImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(onImportSuccess: () -> Unit, vm: ImportViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.parseFile(context, it) }
    }

    LaunchedEffect(state) {
        if (state is ImportUiState.Success) {
            onImportSuccess()
            vm.reset()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Importa Programma") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Indigo500,
                titleContentColor = androidx.compose.ui.graphics.Color.White))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val s = state) {
                is ImportUiState.Idle -> {
                    item {
                        SectionCard {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.UploadFile, null, tint = Indigo500, modifier = Modifier.size(64.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("Importa il tuo programma Excel", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Text("Seleziona il file FitLife_Pro_Programma_Template.xlsx compilato con il tuo programma di allenamento, piano alimentare e integratori.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500)) {
                                    Icon(Icons.Default.FolderOpen, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Scegli file Excel")
                                }
                            }
                        }
                    }
                    item {
                        SectionCard {
                            SectionHeader("Struttura attesa", Indigo500, Icons.Default.TableChart)
                            Spacer(Modifier.height(8.dp))
                            listOf("PROGRAMMA","ALLENAMENTO","ESERCIZI","MENU_SETTIMANALE","INTEGRATORI","IDRATAZIONE")
                                .forEachIndexed { i, sheet ->
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("${i+1}", color = Indigo500,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.width(20.dp))
                                        Icon(Icons.Default.TableView, null, tint = Indigo500, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(sheet, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                        }
                    }
                }
                is ImportUiState.Loading -> {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Indigo500)
                                Spacer(Modifier.height(12.dp))
                                Text("Analisi del file in corso...")
                            }
                        }
                    }
                }
                is ImportUiState.Preview -> {
                    item {
                        SectionCard {
                            SectionHeader("Anteprima importazione", Green500, Icons.Default.Preview)
                            Spacer(Modifier.height(12.dp))
                            val r = s.result
                            listOf(
                                "Programma" to r.program.name,
                                "Obiettivo" to r.program.goal,
                                "Atleta" to r.program.athleteName,
                                "Giorni allenamento" to "${r.trainingDays.size}",
                                "Esercizi totali" to "${r.exercises.size}",
                                "Pasti pianificati" to "${r.meals.size}",
                                "Integratori" to "${r.supplements.size}",
                                "Reminder idratazione" to "${r.hydrationReminders.size}"
                            ).forEach { (k, v) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                    Text(k, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline)
                                    Text(v, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            if (r.errors.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text("Avvisi: ${r.errors.joinToString("; ")}",
                                    style = MaterialTheme.typography.bodyMedium, color = Amber500)
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { vm.reset() }, modifier = Modifier.weight(1f)) {
                                    Text("Annulla")
                                }
                                Button(onClick = { vm.confirmImport(r) }, modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green500)) {
                                    Icon(Icons.Default.Check, null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Conferma")
                                }
                            }
                        }
                    }
                }
                is ImportUiState.Error -> {
                    item {
                        SectionCard {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Errore importazione", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                            Text(s.message, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { vm.reset() }) { Text("Riprova") }
                        }
                    }
                }
                else -> {}
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

package it.fitlifepro.app.ui.screens.shopping

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.fitlifepro.app.ui.theme.*
import it.fitlifepro.app.viewmodel.ShoppingItem
import it.fitlifepro.app.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(vm: ShoppingListViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🛒 Lista della Spesa", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${state.checkedCount}/${state.totalCount} articoli",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.resetAll() }) {
                        Icon(Icons.Default.RestartAlt, "Deseleziona tutto",
                            tint = MaterialTheme.colorScheme.outline)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange500)
            }
            return@Scaffold
        }

        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Nessun piano nutrizionale importato",
                        style = MaterialTheme.typography.bodyLarge)
                    Text("Importa un file Excel con il piano pasti",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
            return@Scaffold
        }

        Column(Modifier.padding(padding)) {
            // Barra di progresso
            if (state.totalCount > 0) {
                LinearProgressIndicator(
                    progress = { state.checkedCount.toFloat() / state.totalCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(8.dp),
                    color = Green500,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Lista normalizzata per alimento con quantità totale
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.items, key = { it.name }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = { vm.toggleItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingItem, onToggle: () -> Unit) {
    val bgColor by animateColorAsState(
        if (item.checked)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "item_bg"
    )

    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Green500)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (item.checked) FontWeight.Normal else FontWeight.Medium,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                color = if (item.checked)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // Quantità totale settimanale
            if (item.totalGrams > 0) {
                Text(
                    "${item.totalGrams}g",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.checked)
                        Orange500.copy(alpha = 0.4f)
                    else
                        Orange500,
                    fontWeight = FontWeight.Bold
                )
            }
            if (item.checked) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.CheckCircle, null,
                    tint = Green500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

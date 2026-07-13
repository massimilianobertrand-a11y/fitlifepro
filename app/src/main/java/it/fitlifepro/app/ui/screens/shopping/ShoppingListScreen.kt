package it.fitlifepro.app.ui.screens.shopping

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@Composable
fun ShoppingListScreen(vm: ShoppingListViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🛒 Lista della Spesa", style = MaterialTheme.typography.titleLarge)
                        val done = state.items.count { it.checked }
                        val total = state.filtered.size
                        Text(
                            "${done}/${total} articoli",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.uncheckAll() }) {
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
            // Filtro per giorno
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.days) { day ->
                    FilterChip(
                        selected = state.filterDay == day,
                        onClick = { vm.setFilterDay(day) },
                        label = { Text(day, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange500,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            // Progresso
            val doneCount = state.filtered.count { it.checked }
            val totalCount = state.filtered.size
            if (totalCount > 0) {
                LinearProgressIndicator(
                    progress = { doneCount.toFloat() / totalCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(6.dp),
                    color = Green500,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Lista raggruppata per pasto
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.groupedFiltered.forEach { (mealType, items) ->
                    item(key = "header_$mealType") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                mealTypeEmoji(mealType),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                mealType,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Orange500
                            )
                            Spacer(Modifier.width(8.dp))
                            val doneMeal = items.count { it.checked }
                            Text(
                                "$doneMeal/${items.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    items(items, key = { "${it.name}_${it.mealType}_${it.dayOfWeek}" }) { item ->
                        ShoppingItemRow(
                            item = item,
                            showDay = state.filterDay == "Tutti",
                            onToggle = { vm.toggleItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingItem, showDay: Boolean = true, onToggle: () -> Unit) {
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
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.checked) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    color = if (item.checked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (showDay) {
                    Text(
                        item.dayOfWeek,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (item.checked) {
                Icon(
                    Icons.Default.CheckCircle, null,
                    tint = Green500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun mealTypeEmoji(mealType: String): String = when {
    mealType.contains("Colazione", ignoreCase = true) -> "🥣"
    mealType.contains("Spuntino", ignoreCase = true) -> "🍌"
    mealType.contains("Pranzo", ignoreCase = true) -> "🍽️"
    mealType.contains("Cena", ignoreCase = true) -> "🍴"
    mealType.contains("Post", ignoreCase = true) -> "💪"
    else -> "🥗"
}

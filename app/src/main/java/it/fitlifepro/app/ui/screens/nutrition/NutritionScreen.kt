
package it.fitlifepro.app.ui.screens.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.fitlifepro.app.data.model.MealPlan
import it.fitlifepro.app.data.repository.FitLifeRepository
import it.fitlifepro.app.ui.components.*
import it.fitlifepro.app.ui.theme.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class NutritionState(
    val meals: List<MealPlan> = emptyList(),
    val loggedIds: Set<Long> = emptySet(),
    val selectedDay: String = ""
)

@HiltViewModel
class NutritionViewModel @Inject constructor(private val repo: FitLifeRepository) : ViewModel() {
    private val _state = MutableStateFlow(NutritionState())
    val state: StateFlow<NutritionState> = _state.asStateFlow()
    val today = LocalDate.now().toString()
    val days = listOf("Lunedi","Martedi","Mercoledi","Giovedi","Venerdi","Sabato","Domenica")
    val todayName: String = LocalDate.now().dayOfWeek
        .getDisplayName(TextStyle.FULL, Locale.ITALIAN).replaceFirstChar { it.uppercase() }

    fun load(programId: Long, day: String) = viewModelScope.launch {
        combine(repo.getMealsByDay(programId, day), repo.getMealLog(today)) { meals, logs ->
            NutritionState(meals = meals, loggedIds = logs.map { it.mealPlanId }.toSet(), selectedDay = day)
        }.collect { _state.value = it }
    }
    fun toggleMeal(mealId: Long) = viewModelScope.launch {
        if (mealId in _state.value.loggedIds) repo.unlogMeal(today, mealId)
        else repo.logMeal(mealId, today)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(programId: Long, vm: NutritionViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var selectedDay by remember { mutableStateOf(vm.todayName) }

    LaunchedEffect(selectedDay) { vm.load(programId, selectedDay) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Nutrizione 🥗") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Green500,
                titleContentColor = Color.White))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                // Day tabs
                ScrollableTabRow(selectedTabIndex = vm.days.indexOf(selectedDay).coerceAtLeast(0),
                    containerColor = MaterialTheme.colorScheme.surface, edgePadding = 0.dp) {
                    vm.days.forEach { day ->
                        Tab(selected = selectedDay == day, onClick = { selectedDay = day }) {
                            Text(day.take(3), modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedDay == day) Green500 else MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            if (state.meals.isEmpty()) {
                item { EmptyState("Nessun pasto per $selectedDay", Icons.Default.NoMeals) }
            } else {
                items(state.meals) { meal ->
                    val done = meal.id in state.loggedIds
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(meal.mealType.replace("_"," "),
                                        style = MaterialTheme.typography.labelLarge, color = Green500)
                                    Spacer(Modifier.width(8.dp))
                                    Text(meal.timeHHMM, style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("${meal.food1} ${if (meal.qty1g > 0) "${meal.qty1g}g" else ""}",
                                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                if (meal.food2.isNotBlank())
                                    Text("+ ${meal.food2} ${if (meal.qty2g > 0) "${meal.qty2g}g" else ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline)
                                if (meal.kcalEstimated > 0)
                                    Text("${meal.kcalEstimated} kcal",
                                        style = MaterialTheme.typography.labelMedium, color = Amber500)
                            }
                            Checkbox(checked = done,
                                onCheckedChange = { vm.toggleMeal(meal.id) },
                                colors = CheckboxDefaults.colors(checkedColor = Green500))
                        }
                        if (meal.notes.isNotBlank()) {
                            Text(meal.notes, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

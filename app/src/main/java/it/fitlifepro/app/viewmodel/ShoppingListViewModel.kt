package it.fitlifepro.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.fitlifepro.app.data.repository.FitLifeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingItem(
    val name: String,
    val mealType: String,
    val dayOfWeek: String,
    var checked: Boolean = false
)

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
    val filterDay: String = "Tutti"
) {
    val days: List<String> get() = listOf("Tutti") +
        items.map { it.dayOfWeek }.distinct().sorted()
    val filtered: List<ShoppingItem> get() = if (filterDay == "Tutti") items
        else items.filter { it.dayOfWeek == filterDay }
    val groupedFiltered: Map<String, List<ShoppingItem>> get() =
        filtered.groupBy { it.mealType }
}

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repo: FitLifeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingListUiState())
    val state: StateFlow<ShoppingListUiState> = _state.asStateFlow()

    init { loadShoppingList() }

    private fun loadShoppingList() = viewModelScope.launch {
        repo.activeProgram.collect { program ->
            if (program == null) {
                _state.update { it.copy(isLoading = false, items = emptyList()) }
                return@collect
            }
            repo.getMealPlan(program.id).collect { plans ->
                val items = mutableListOf<ShoppingItem>()
                plans.forEach { plan ->
                    if (plan.food1.isNotBlank())
                        items += ShoppingItem(plan.food1.trim(), plan.mealType, plan.dayOfWeek)
                    if (plan.food2.isNotBlank())
                        items += ShoppingItem(plan.food2.trim(), plan.mealType, plan.dayOfWeek)
                }
                // Deduplica per nome+mealType, preserva ordine
                val seen = mutableSetOf<String>()
                val deduped = items.filter { seen.add("${it.name}_${it.mealType}") }
                _state.update { it.copy(isLoading = false, items = deduped) }
            }
        }
    }

    fun toggleItem(item: ShoppingItem) {
        _state.update { st ->
            st.copy(items = st.items.map {
                if (it.name == item.name && it.mealType == item.mealType &&
                    it.dayOfWeek == item.dayOfWeek) it.copy(checked = !it.checked)
                else it
            })
        }
    }

    fun setFilterDay(day: String) = _state.update { it.copy(filterDay = day) }

    fun uncheckAll() = _state.update { st ->
        st.copy(items = st.items.map { it.copy(checked = false) })
    }
}

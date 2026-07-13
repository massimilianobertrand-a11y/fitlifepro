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
    val totalGrams: Int,
    var checked: Boolean = false
)

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true
) {
    val checkedCount: Int get() = items.count { it.checked }
    val totalCount: Int get() = items.size
}

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repo: FitLifeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingListUiState())
    val state: StateFlow<ShoppingListUiState> = _state.asStateFlow()

    init { loadShoppingList() }

    private fun loadShoppingList() = viewModelScope.launch {
        repo.activeProgram.flatMapLatest { program ->
            if (program == null) {
                _state.update { it.copy(isLoading = false, items = emptyList()) }
                return@flatMapLatest kotlinx.coroutines.flow.emptyFlow()
            }
            repo.getMealPlan(program.id)
        }.collect { plans ->
            // Aggregate quantities per food name (case-insensitive)
            val quantityMap = mutableMapOf<String, Int>() // normalized name -> total grams
            plans.forEach { plan ->
                if (plan.food1.isNotBlank()) {
                    val key = plan.food1.trim().lowercase()
                    val display = plan.food1.trim()
                    // store display name keyed by lowercase
                    quantityMap[display] = (quantityMap[display] ?: 0) + plan.qty1g
                }
                if (plan.food2.isNotBlank()) {
                    val display = plan.food2.trim()
                    quantityMap[display] = (quantityMap[display] ?: 0) + plan.qty2g
                }
            }
            // Merge case-insensitive duplicates (prefer the capitalized form)
            val merged = mutableMapOf<String, Int>()
            quantityMap.forEach { (name, qty) ->
                val canonical = merged.keys.firstOrNull { it.equals(name, ignoreCase = true) } ?: name
                merged[canonical] = (merged[canonical] ?: 0) + qty
            }
            val items = merged
                .filter { it.key.isNotBlank() }
                .map { (name, qty) -> ShoppingItem(name, qty) }
                .sortedBy { it.name.lowercase() }
            _state.update { it.copy(isLoading = false, items = items) }
        }
    }

    fun toggleItem(item: ShoppingItem) {
        _state.update { st ->
            st.copy(items = st.items.map {
                if (it.name == item.name) it.copy(checked = !it.checked) else it
            })
        }
    }

    fun resetAll() {
        _state.update { st -> st.copy(items = st.items.map { it.copy(checked = false) }) }
    }
}

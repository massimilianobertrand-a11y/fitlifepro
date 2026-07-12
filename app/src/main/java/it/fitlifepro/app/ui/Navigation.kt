
package it.fitlifepro.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import it.fitlifepro.app.ui.screens.dashboard.DashboardScreen
import it.fitlifepro.app.ui.screens.history.HistoryScreen
import it.fitlifepro.app.ui.screens.hydration.HydrationScreen
import it.fitlifepro.app.ui.screens.`import`.ImportScreen
import it.fitlifepro.app.ui.screens.nutrition.NutritionScreen
import it.fitlifepro.app.ui.screens.supplements.SupplementsScreen
import it.fitlifepro.app.ui.screens.workout.WorkoutScreen
import androidx.compose.ui.graphics.Color
import it.fitlifepro.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val color: Color) {
    object Dashboard   : Screen("dashboard",   "Home",        Icons.Default.Home,          Indigo500)
    object Workout     : Screen("workout",     "Workout",     Icons.Default.FitnessCenter, Orange500)
    object Nutrition   : Screen("nutrition",   "Nutrizione",  Icons.Default.Restaurant,    Green500)
    object Supplements : Screen("supplements", "Integratori", Icons.Default.Medication,    Purple500)
    object Hydration   : Screen("hydration",   "Acqua",       Icons.Default.Water,         Blue500)
    object History     : Screen("history",     "Storico",     Icons.Default.BarChart,      Amber500)
    object Import      : Screen("import",      "Importa",     Icons.Default.UploadFile,    Indigo500)
}

val bottomNavItems = listOf(
    Screen.Dashboard, Screen.Workout, Screen.Nutrition,
    Screen.Supplements, Screen.Hydration, Screen.History
)

@Composable
fun FitLifeNavHost(
    navController: NavHostController,
    programId: Long
) {
    NavHost(navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route)   { DashboardScreen() }
        composable(Screen.Workout.route)     { WorkoutScreen(programId) }
        composable(Screen.Nutrition.route)   { NutritionScreen(programId) }
        composable(Screen.Supplements.route) { SupplementsScreen(programId) }
        composable(Screen.Hydration.route)   { HydrationScreen(programId) }
        composable(Screen.History.route)     { HistoryScreen() }
        composable(Screen.Import.route) {
            ImportScreen(onImportSuccess = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            })
        }
    }
}

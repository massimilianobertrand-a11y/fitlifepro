
package it.fitlifepro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import it.fitlifepro.app.ui.FitLifeNavHost
import it.fitlifepro.app.ui.Screen
import it.fitlifepro.app.ui.bottomNavItems
import it.fitlifepro.app.ui.theme.FitLifeTheme
import it.fitlifepro.app.viewmodel.DashboardViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitLifeTheme {
                FitLifeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitLifeApp() {
    val navController = rememberNavController()
    val dashVm: DashboardViewModel = viewModel()
    val dashState by dashVm.state.collectAsStateWithLifecycle()
    val programId = dashState.program?.id ?: -1L

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 4.dp) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = screen.color,
                            selectedTextColor = screen.color,
                            indicatorColor = screen.color.copy(alpha = 0.12f)
                        )
                    )
                }
                // Import button
                NavigationBarItem(
                    selected = currentRoute == Screen.Import.route,
                    onClick = { navController.navigate(Screen.Import.route) },
                    icon = { Icon(Icons.Default.UploadFile, "Importa") },
                    label = { Text("Importa") }
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            FitLifeNavHost(navController, programId)
        }
    }
}

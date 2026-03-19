package com.nfcemulator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.nfc.reader.ReadProgress
import com.nfcemulator.ui.editor.EditorScreen
import com.nfcemulator.ui.emulator.EmulatorScreen
import com.nfcemulator.ui.home.HomeScreen
import com.nfcemulator.ui.home.HomeViewModel
import com.nfcemulator.ui.home.TagUiModel
import com.nfcemulator.ui.reader.ReaderScreen
import androidx.compose.runtime.LaunchedEffect
import com.nfcemulator.ui.settings.SettingsScreen
import com.nfcemulator.ui.settings.SettingsViewModel
import com.nfcemulator.ui.theme.NfcColors
import com.nfcemulator.ui.theme.NfcDimensions
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Reader : Screen("reader", "Read", Icons.Default.Search)
    data object Emulator : Screen("emulator", "Emulate", Icons.Default.PlayArrow)
    data object Editor : Screen("editor", "Editor", Icons.Outlined.Edit)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun NfcNavigation(
    readProgress: ReadProgress,
    onImportClick: () -> Unit,
    onSaveTag: (TagDump) -> Unit,
    onResetReader: () -> Unit,
    onCrackKeys: (TagDump) -> Unit
) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Reader, Screen.Emulator, Screen.Editor, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = NfcColors.Background,
        bottomBar = {
            NavigationBar(
                containerColor = NfcColors.Surface,
                contentColor = NfcColors.TextPrimary,
                tonalElevation = NfcDimensions.CardElevation
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = if (currentRoute == screen.route) {
                            { Text(screen.label, style = MaterialTheme.typography.labelSmall) }
                        } else null,
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NfcColors.Primary,
                            selectedTextColor = NfcColors.Primary,
                            unselectedIconColor = NfcColors.TextSecondary,
                            indicatorColor = NfcColors.SurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = koinViewModel()
                val tags by viewModel.tags.collectAsState()
                HomeScreen(
                    tags = tags,
                    onTagClick = { },
                    onAddClick = { navController.navigate(Screen.Reader.route) },
                    onSearchQuery = { viewModel.search(it) }
                )
            }
            composable(Screen.Reader.route) {
                ReaderScreen(
                    readProgress = readProgress,
                    onImportClick = onImportClick,
                    onSaveTag = onSaveTag,
                    onReset = onResetReader,
                    onCrackKeys = onCrackKeys
                )
            }
            composable(Screen.Emulator.route) {
                EmulatorScreen(
                    selectedTag = null,
                    isEmulating = false,
                    emulationMode = "HCE Standard",
                    onStartEmulation = { },
                    onStopEmulation = { },
                    onSelectTag = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.Editor.route) {
                EditorScreen(dump = null)
            }
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = koinViewModel()
                val state by viewModel.uiState.collectAsState()
                LaunchedEffect(Unit) { viewModel.loadStats() }
                SettingsScreen(
                    hasRoot = state.hasRoot,
                    hasNxpChipset = state.hasNxpChipset,
                    emulationMode = state.emulationMode,
                    totalKeys = state.totalKeys,
                    totalTags = state.totalTags,
                    storageSize = state.storageSize,
                    onExportBackup = { },
                    onImportBackup = { }
                )
            }
        }
    }
}

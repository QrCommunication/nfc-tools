package com.nfcemulator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.nfc.reader.ReadProgress
import com.nfcemulator.ui.dashboard.DashboardScreen
import com.nfcemulator.ui.dashboard.DashboardStats
import com.nfcemulator.ui.editor.EditorScreen
import com.nfcemulator.ui.emulator.EmulatorScreen
import com.nfcemulator.ui.emulator.EmulatorViewModel
import com.nfcemulator.ui.home.HomeScreen
import com.nfcemulator.ui.home.HomeViewModel
import com.nfcemulator.ui.reader.ReaderScreen
import com.nfcemulator.ui.settings.SettingsScreen
import com.nfcemulator.ui.settings.SettingsViewModel
import com.nfcemulator.ui.splash.SplashScreen
import com.nfcemulator.ui.theme.LocalAppColors
import com.nfcemulator.ui.writer.WriteScreen
import com.nfcemulator.ui.writer.WriteViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun NfcNavigation(
    readProgress: ReadProgress,
    onImportClick: () -> Unit,
    onSaveTag: (TagDump) -> Unit,
    onResetReader: () -> Unit,
    onCrackKeys: (TagDump) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    currentLanguage: String,
    onSetLanguage: (String) -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val emulatorViewModel: EmulatorViewModel = koinViewModel()
    var selectedDumpForEditor by remember { mutableStateOf<TagDump?>(null) }

    LaunchedEffect(readProgress) {
        if (readProgress is ReadProgress.Complete) {
            selectedDumpForEditor = readProgress.dump
        }
    }

    data class DrawerItem(val route: String, val label: String, val icon: ImageVector)
    val drawerItems = listOf(
        DrawerItem("dashboard", "Dashboard", Icons.Default.Home),
        DrawerItem("tags", "My Tags", Icons.Default.List),
        DrawerItem("reader", "Read Tag", Icons.Default.Search),
        DrawerItem("writer", "Write to Card", Icons.Default.Create),
        DrawerItem("emulator", "Emulate", Icons.Default.PlayArrow),
        DrawerItem("editor", "Hex Editor", Icons.Outlined.Edit),
        DrawerItem("settings", "Settings", Icons.Default.Settings)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = LocalAppColors.current.Surface,
                drawerContentColor = LocalAppColors.current.TextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LocalAppColors.current.SurfaceVariant)
                        .padding(24.dp)
                ) {
                    Text("NFC Emulator", style = MaterialTheme.typography.headlineSmall, color = LocalAppColors.current.Primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Read. Clone. Emulate.", style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.Secondary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (currentRoute == item.route) LocalAppColors.current.Primary else LocalAppColors.current.TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    color = if (currentRoute == item.route) LocalAppColors.current.Primary else LocalAppColors.current.TextPrimary
                                )
                            },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = LocalAppColors.current.Primary.copy(alpha = 0.1f),
                                unselectedContainerColor = LocalAppColors.current.Surface
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = LocalAppColors.current.Background,
            topBar = {
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = LocalAppColors.current.Primary)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("dashboard") {
                    val settingsVm: SettingsViewModel = koinViewModel()
                    val settingsState by settingsVm.uiState.collectAsState()
                    LaunchedEffect(Unit) { settingsVm.loadStats() }
                    DashboardScreen(
                        stats = DashboardStats(
                            totalTags = settingsState.totalTags,
                            totalKeys = settingsState.totalKeys,
                            storageUsed = settingsState.storageSize,
                            hasRoot = settingsState.hasRoot,
                            emulationMode = settingsState.emulationMode
                        ),
                        onReadTag = { navController.navigate("reader") },
                        onImportFile = onImportClick,
                        onWriteCard = { navController.navigate("writer") },
                        onMyTags = { navController.navigate("tags") },
                        onSettings = { navController.navigate("settings") }
                    )
                }
                composable("tags") {
                    val viewModel: HomeViewModel = koinViewModel()
                    val tags by viewModel.tags.collectAsState()
                    HomeScreen(
                        tags = tags,
                        onTagClick = { tagId ->
                            val tag = tags.find { it.id == tagId }
                            if (tag != null) {
                                emulatorViewModel.selectTag(tag)
                                navController.navigate("emulator")
                            }
                        },
                        onEmulateTag = { tagId ->
                            val tag = tags.find { it.id == tagId }
                            if (tag != null) {
                                emulatorViewModel.selectTag(tag)
                                navController.navigate("emulator")
                            }
                        },
                        onEditTag = { navController.navigate("editor") },
                        onDeleteTag = { tagId -> viewModel.deleteTag(tagId) },
                        onRenameTag = { tagId, newName -> viewModel.renameTag(tagId, newName) },
                        onAddClick = { navController.navigate("reader") },
                        onSearchQuery = { viewModel.search(it) }
                    )
                }
                composable("reader") {
                    DisposableEffect(Unit) {
                        onDispose { onResetReader() }
                    }
                    ReaderScreen(
                        readProgress = readProgress,
                        onImportClick = onImportClick,
                        onSaveTag = onSaveTag,
                        onReset = onResetReader,
                        onCrackKeys = onCrackKeys
                    )
                }
                composable("writer") {
                    val viewModel: WriteViewModel = koinViewModel()
                    val state by viewModel.uiState.collectAsState()
                    DisposableEffect(Unit) {
                        onDispose { viewModel.cancelWrite() }
                    }
                    WriteScreen(
                        tags = state.tags,
                        selectedTag = state.selectedTag,
                        writeProgress = state.writeProgress,
                        onSelectTag = { viewModel.selectTag(it) },
                        onStartWrite = { viewModel.startWrite() },
                        onCancelWrite = { viewModel.cancelWrite() }
                    )
                }
                composable("emulator") {
                    val state by emulatorViewModel.uiState.collectAsState()
                    DisposableEffect(Unit) {
                        onDispose { emulatorViewModel.stopEmulation() }
                    }
                    EmulatorScreen(
                        selectedTag = state.selectedTag,
                        isEmulating = state.isEmulating,
                        emulationMode = state.emulationMode,
                        statusMessage = state.statusMessage,
                        writeProgress = state.writeProgress,
                        onStartEmulation = { emulatorViewModel.startEmulation() },
                        onStopEmulation = { emulatorViewModel.stopEmulation() },
                        onWriteToTag = { emulatorViewModel.startWriteMode() },
                        onSelectTag = { navController.navigate("tags") }
                    )
                }
                composable("editor") {
                    EditorScreen(dump = selectedDumpForEditor)
                }
                composable("settings") {
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
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = onToggleDarkMode,
                        currentLanguage = currentLanguage,
                        onSetLanguage = onSetLanguage,
                        onExportBackup = { },
                        onImportBackup = { }
                    )
                }
            }
        }
    }
}

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nfcemulator.R
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // Navigate to tags after successful save
    LaunchedEffect(readProgress) {
        if (readProgress is ReadProgress.Complete) {
            selectedDumpForEditor = readProgress.dump
        }
    }

    data class DrawerItem(val route: String, val labelRes: Int, val icon: ImageVector)
    val drawerItems = listOf(
        DrawerItem("dashboard", R.string.dashboard, Icons.Default.Home),
        DrawerItem("tags", R.string.my_tags, Icons.Default.List),
        DrawerItem("reader", R.string.read_tag, Icons.Default.Search),
        DrawerItem("writer", R.string.write_title, Icons.Default.Create),
        DrawerItem("emulator", R.string.emulate_title, Icons.Default.PlayArrow),
        DrawerItem("editor", R.string.hex_editor, Icons.Outlined.Edit),
        DrawerItem("settings", R.string.settings_title, Icons.Default.Settings)
    )

    val currentScreenLabel = drawerItems.find { it.route == currentRoute }
        ?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.app_name)

    val colors = LocalAppColors.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.Surface,
                drawerContentColor = colors.TextPrimary
            ) {
                // Header area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.SurfaceContainerHigh)
                        .padding(24.dp)
                ) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, color = colors.Primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodySmall, color = colors.Secondary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    drawerItems.forEach { item ->
                        val itemLabel = stringResource(item.labelRes)
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = itemLabel,
                                    tint = if (currentRoute == item.route) colors.Primary else colors.TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    itemLabel,
                                    color = if (currentRoute == item.route) colors.Primary else colors.TextPrimary
                                )
                            },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                // Always navigate fresh — no saveState/restoreState
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") { inclusive = item.route == "dashboard" }
                                    launchSingleTop = true
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = colors.Primary.copy(alpha = 0.12f),
                                unselectedContainerColor = colors.Surface
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = colors.Background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            currentScreenLabel,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colors.Primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colors.SurfaceContainer,
                        titleContentColor = colors.TextPrimary,
                        navigationIconContentColor = colors.Primary
                    )
                )
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
                        onReadTag = {
                            navController.navigate("reader") { launchSingleTop = true }
                        },
                        onImportFile = onImportClick,
                        onWriteCard = {
                            navController.navigate("writer") { launchSingleTop = true }
                        },
                        onMyTags = {
                            navController.navigate("tags") { launchSingleTop = true }
                        },
                        onSettings = {
                            navController.navigate("settings") { launchSingleTop = true }
                        }
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
                                navController.navigate("emulator") { launchSingleTop = true }
                            }
                        },
                        onEmulateTag = { tagId ->
                            val tag = tags.find { it.id == tagId }
                            if (tag != null) {
                                emulatorViewModel.selectTag(tag)
                                navController.navigate("emulator") { launchSingleTop = true }
                            }
                        },
                        onEditTag = {
                            navController.navigate("editor") { launchSingleTop = true }
                        },
                        onDeleteTag = { tagId -> viewModel.deleteTag(tagId) },
                        onRenameTag = { tagId, newName -> viewModel.renameTag(tagId, newName) },
                        onAddClick = {
                            navController.navigate("reader") { launchSingleTop = true }
                        },
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
                        onSaveTag = { dump ->
                            onSaveTag(dump)
                            navController.navigate("tags") { launchSingleTop = true }
                        },
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
                        onStartEmulation = { emulatorViewModel.startEmulation() },
                        onStopEmulation = { emulatorViewModel.stopEmulation() },
                        onSelectTag = {
                            navController.navigate("tags") { launchSingleTop = true }
                        }
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

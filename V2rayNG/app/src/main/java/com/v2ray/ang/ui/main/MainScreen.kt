package com.v2ray.ang.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.ui.compose.QRCodeDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    updateViewModel: UpdateViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val updateState by updateViewModel.state.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val isRunning = uiState.isRunning
    val displayText = mainViewModel.formatStatus(uiState.status)
    val selectedGuid = uiState.selectedGuid
    val doubleColumnDisplay = uiState.doubleColumnDisplay
    val confirmRemove = uiState.confirmRemove
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap

    val scope = rememberCoroutineScope()
    var selectedTabIndex by rememberSaveable { mutableStateOf(MainTab.Configs.ordinal) }
    val selectedTab = MainTab.entries[selectedTabIndex.coerceIn(0, MainTab.entries.lastIndex)]
    BackHandler(enabled = selectedTab != MainTab.Configs) {
        selectedTabIndex = MainTab.Configs.ordinal
    }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by rememberSaveable(stateSaver = ServerDeleteTarget.Saver) {
        mutableStateOf<ServerDeleteTarget?>(null)
    }

    var shareTarget by remember { mutableStateOf<Triple<String, ProfileItem, Boolean>?>(null) }
    val removeServer: (String, String) -> Unit = { guid, profileName ->
        if (confirmRemove) {
            showRemoveConfirm = ServerDeleteTarget(guid, profileName)
        } else {
            onAction(MainAction.RemoveServer(guid))
        }
    }

    UpdateDialog(
        state = updateState,
        onDownload = updateViewModel::startDownload,
        onInstall = updateViewModel::install,
        onDismiss = updateViewModel::dismiss
    )

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onAction(MainAction.RemoveServer(guid)) }
    )

    if (shareTarget != null) {
        val (guid, profile, more) = shareTarget!!
        ShareMethodDialog(
            guid = guid,
            profile = profile,
            more = more,
            onDismiss = { shareTarget = null },
            onAction = onAction,
            onRemove = removeServer,
        )
    }
    if (shareQRCodeBitmap != null) {
        QRCodeDialog(bitmap = shareQRCodeBitmap, onDismiss = { onAction(MainAction.DismissQRCodeDialog) })
    }

    val handleMoreMenu: (MainMoreMenuAction) -> Unit = { action ->
        when (action) {
                MainMoreMenuAction.RestartService -> onAction(MainAction.RestartService)
                MainMoreMenuAction.DeleteAll -> showDelAllConfirm = true
                MainMoreMenuAction.DeleteDuplicate -> showDelDuplicateConfirm = true
                MainMoreMenuAction.DeleteInvalid -> showDelInvalidConfirm = true
                MainMoreMenuAction.ExportAll -> onAction(MainAction.ExportAll)
                MainMoreMenuAction.LocateSelected -> onAction(MainAction.LocateSelectedServer)
                MainMoreMenuAction.SortByTestResults -> onAction(MainAction.SortByTestResults)
                MainMoreMenuAction.TestAll -> onAction(MainAction.TestAllServers)
                MainMoreMenuAction.TestAllRealPing -> onAction(MainAction.TestRealAllServers)
                MainMoreMenuAction.UpdateSubscriptions -> onAction(MainAction.UpdateSubscriptions)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            MainTopBar(
                tab = selectedTab,
                isLoading = isLoading,
                showSearch = showSearch,
                searchQuery = searchQuery,
                onSearchQueryChange = { query: String ->
                    searchQuery = query
                    onAction(MainAction.Search(query))
                },
                onSearchClose = {
                    searchQuery = ""
                    onAction(MainAction.Search(""))
                    showSearch = false
                },
                onSearchToggle = { show: Boolean -> showSearch = show },
                onAction = onAction,
                onNavigate = onNavigate,
                onOpenSettingsTab = { selectedTabIndex = MainTab.Settings.ordinal }
            )
        },
        bottomBar = {
            MainBottomBar(
                displayText = displayText,
                isRunning = isRunning,
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab != MainTab.Configs && showSearch) {
                        searchQuery = ""
                        onAction(MainAction.Search(""))
                        showSearch = false
                    }
                    selectedTabIndex = tab.ordinal
                },
                onAction = onAction
            )
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Home -> HomeTab(
                isRunning = isRunning,
                displayText = displayText,
                selectedGuid = selectedGuid,
                onNavigate = onNavigate,
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.Settings -> SettingsTab(
                onNavigate = onNavigate,
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.Configs -> if (groups.isNotEmpty()) {
                GroupedServerList(
                    groups = groups,
                    mainViewModel = mainViewModel,
                    selectedGuid = selectedGuid,
                    locateTarget = uiState.locateTarget,
                    onSelectGroup = { groupId -> onAction(MainAction.SelectGroup(groupId)) },
                    onPingGroup = { groupId ->
                        onAction(MainAction.SelectGroup(groupId))
                        onAction(MainAction.TestRealAllServers)
                    },
                    onGroupMenuAction = { groupId, action ->
                        onAction(MainAction.SelectGroup(groupId))
                        handleMoreMenu(action)
                    },
                    onSelectServer = { guid -> onAction(MainAction.SelectServer(guid)) },
                    onEditServer = { guid, profile -> onAction(MainAction.EditServer(guid, profile)) },
                    onShareServer = { guid, profile ->
                        shareTarget = Triple(guid, profile, false)
                    },
                    onMoreServer = { guid, profile ->
                        shareTarget = Triple(guid, profile, true)
                    },
                    onRemoveServer = removeServer,
                    onLocateHandled = { onAction(MainAction.LocateHandled) },
                    contentPadding = PaddingValues(bottom = 12.dp),
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
            }
        }
    }
}

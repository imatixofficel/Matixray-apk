package com.v2ray.ang.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppTopBar
import com.v2ray.ang.ui.compose.verticalScrollbar

@Composable
fun MainTopBar(
    tab: MainTab,
    isLoading: Boolean,
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
    onOpenSettingsTab: () -> Unit
) {
    when (tab) {
        MainTab.Home -> TabTitle(title = stringResource(R.string.app_name))
        MainTab.Settings -> TabTitle(title = stringResource(R.string.tab_settings))
        MainTab.Configs -> if (showSearch) {
            AppTopBar(
                title = stringResource(R.string.title_server),
                onBackClick = onSearchClose,
                isLoading = isLoading,
                isSearchActive = true,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchClose = onSearchClose,
                searchPlaceholder = stringResource(R.string.menu_item_search)
            )
        } else {
            ConfigsHeader(
                isLoading = isLoading,
                onSearchToggle = onSearchToggle,
                onAction = onAction,
                onNavigate = onNavigate,
                onOpenSettingsTab = onOpenSettingsTab
            )
        }
    }
}

@Composable
private fun TabTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
    )
}

@Composable
private fun ConfigsHeader(
    isLoading: Boolean,
    onSearchToggle: (Boolean) -> Unit,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
    onOpenSettingsTab: () -> Unit
) {
    var showImportMenu by remember { mutableStateOf(false) }
    val importMenuScrollState = rememberScrollState()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val maxMenuHeight = LocalConfiguration.current.screenHeightDp.dp - statusBarHeight - navBarHeight - 20.dp
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onAction(MainAction.ImportQRcode) }) {
                Icon(
                    painterResource(R.drawable.ic_scan_24dp),
                    contentDescription = stringResource(R.string.menu_item_import_config_qrcode),
                    tint = accent
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onSearchToggle(true) }) {
                Icon(
                    painterResource(R.drawable.ic_search_24dp),
                    contentDescription = stringResource(R.string.acc_search),
                    tint = accent
                )
            }
            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = { showImportMenu = true }) {
                    Icon(
                        painterResource(R.drawable.ic_add_24dp),
                        contentDescription = stringResource(R.string.acc_add),
                        tint = accent
                    )
                }
                DropdownMenu(
                    expanded = showImportMenu,
                    onDismissRequest = { showImportMenu = false },
                    scrollState = importMenuScrollState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .heightIn(max = maxMenuHeight)
                        .verticalScrollbar(importMenuScrollState)
                ) {
                    ImportMenuContent(
                        onAction = { action ->
                            showImportMenu = false
                            onAction(action)
                        }
                    )
                }
            }
            IconButton(onClick = { onNavigate(MainDestination.Subscriptions) }) {
                Icon(
                    painterResource(R.drawable.ic_configs_24dp),
                    contentDescription = stringResource(R.string.title_sub_setting),
                    tint = accent
                )
            }
        }
        Text(
            text = stringResource(R.string.tab_configs),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderAction(
                iconRes = R.drawable.ic_sort_24dp,
                text = stringResource(R.string.sort_by_ping),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onAction(MainAction.SortByTestResults) }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderAction(
                    iconRes = R.drawable.ic_refresh_24dp,
                    text = stringResource(R.string.action_update_all),
                    color = accent,
                    onClick = { onAction(MainAction.UpdateSubscriptions) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                HeaderAction(
                    iconRes = null,
                    text = stringResource(R.string.action_ping_all),
                    color = accent,
                    onClick = { onAction(MainAction.TestRealAllServers) }
                )
                IconButton(onClick = onOpenSettingsTab) {
                    Icon(
                        painterResource(R.drawable.ic_settings_24dp),
                        contentDescription = stringResource(R.string.tab_settings),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun HeaderAction(
    iconRes: Int?,
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            maxLines = 1
        )
    }
}

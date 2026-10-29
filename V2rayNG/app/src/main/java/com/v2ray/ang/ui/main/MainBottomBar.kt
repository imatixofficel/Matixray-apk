package com.v2ray.ang.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import kotlinx.coroutines.launch

private val ConnectedGreen = Color(0xFF22C55E)

@Composable
fun MainBottomBar(
    displayText: String,
    isRunning: Boolean,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAction: (MainAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(start = 20.dp, end = 120.dp, top = 6.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable { onAction(MainAction.TestCurrentServer) }
                            .semantics { contentDescription = displayText }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTab.entries.forEach { tab ->
                        BottomBarItem(
                            tab = tab,
                            selected = tab == selectedTab,
                            onClick = { onTabSelected(tab) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        ConnectButton(
            isRunning = isRunning,
            onAction = onAction,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
        )
    }
}

/**
 * Round connect button that sits half over the bar. Sky blue when off, green when connected,
 * with a spin and a bounce when it is pressed and a soft pulse while the connection is up.
 */
@Composable
private fun ConnectButton(
    isRunning: Boolean,
    onAction: (MainAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val spin = remember { Animatable(0f) }
    val press = remember { Animatable(1f) }
    val accent by animateColorAsState(
        targetValue = if (isRunning) ConnectedGreen else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 450),
        label = "connectAccent"
    )
    val surfaceColor = MaterialTheme.colorScheme.surface
    val description = stringResource(if (isRunning) R.string.acc_stop else R.string.acc_start)

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            spin.snapTo(0f)
        }
    }

    Box(modifier = modifier.size(80.dp), contentAlignment = Alignment.Center) {
        if (isRunning) {
            PulseRing(color = accent, modifier = Modifier.size(72.dp))
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = press.value
                    scaleY = press.value
                }
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(surfaceColor)
                .border(width = 3.dp, color = accent, shape = CircleShape)
                .clickable {
                    scope.launch {
                        press.animateTo(0.86f, spring(stiffness = Spring.StiffnessHigh))
                        press.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    if (!isRunning) {
                        scope.launch {
                            spin.animateTo(720f, tween(durationMillis = 1200))
                        }
                    }
                    onAction(MainAction.ToggleService)
                }
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_power_24dp),
                contentDescription = null,
                tint = accent,
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer { rotationZ = spin.value }
            )
        }
    }
}

@Composable
private fun PulseRing(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "connectPulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1800, easing = LinearEasing)),
        label = "connectPulseProgress"
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = 1f + 0.4f * progress
                scaleY = 1f + 0.4f * progress
                alpha = 0.35f * (1f - progress)
            }
            .background(color, CircleShape)
    )
}

@Composable
private fun BottomBarItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(tab.labelRes),
            color = color,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

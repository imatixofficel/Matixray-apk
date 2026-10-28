package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val SkyBlue = Color(0xFF1BA8E8)
private val SkyBlueDark = Color(0xFF4CC3F5)

private val LightColor = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8E3F7),
    onPrimaryContainer = Color(0xFF00334A),
    secondary = SkyBlue,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6EFFB),
    onSecondaryContainer = Color(0xFF00334A),
    tertiary = Color(0xFF2E9E5B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC8F0D6),
    onTertiaryContainer = Color(0xFF00210E),
    error = Color(0xFFD93025),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFEFEFF5),
    onBackground = Color(0xFF111116),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111116),
    surfaceVariant = Color(0xFFE4E6EE),
    onSurfaceVariant = Color(0xFF5B5F6B),
    outline = Color(0xFF8A8E9A),
    outlineVariant = Color(0xFFDADCE4),
    inverseSurface = Color(0xFF2A2B31),
    inverseOnSurface = Color(0xFFF2F2F7),
    inversePrimary = SkyBlueDark,
    scrim = Color(0xFF000000),
    surfaceTint = SkyBlue,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F7FB),
    surfaceContainer = Color(0xFFF1F1F6),
    surfaceContainerHigh = Color(0xFFEBEBF1),
    surfaceContainerHighest = Color(0xFFE4E4EB),
)

private val DarkColor = darkColorScheme(
    primary = SkyBlueDark,
    onPrimary = Color(0xFF00324A),
    primaryContainer = Color(0xFF0B4A66),
    onPrimaryContainer = Color(0xFFCDEEFF),
    secondary = SkyBlueDark,
    onSecondary = Color(0xFF00324A),
    secondaryContainer = Color(0xFF123C52),
    onSecondaryContainer = Color(0xFFCDEEFF),
    tertiary = Color(0xFF83D6B5),
    onTertiary = Color(0xFF00382E),
    tertiaryContainer = Color(0xFF005143),
    onTertiaryContainer = Color(0xFFA0F2D0),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111116),
    onBackground = Color(0xFFE6E6EC),
    surface = Color(0xFF1C1C22),
    onSurface = Color(0xFFE6E6EC),
    surfaceVariant = Color(0xFF2C2D35),
    onSurfaceVariant = Color(0xFFC3C5CF),
    outline = Color(0xFF8E909B),
    outlineVariant = Color(0xFF3A3B44),
    inverseSurface = Color(0xFFE6E6EC),
    inverseOnSurface = Color(0xFF1C1C22),
    inversePrimary = SkyBlue,
    scrim = Color(0xFF000000),
    surfaceTint = SkyBlueDark,
    surfaceContainerLowest = Color(0xFF0C0C10),
    surfaceContainerLow = Color(0xFF17171C),
    surfaceContainer = Color(0xFF1C1C22),
    surfaceContainerHigh = Color(0xFF26262D),
    surfaceContainerHighest = Color(0xFF30303A),
)

// Semantic Colors
val colorPing = Color(0xFF2E9E5B)
val colorPingRed = Color(0xFFE53935)
val colorConfigType = SkyBlue
val colorFabActive = SkyBlue
val colorFabInactiveLight = Color(0xFF9C9CA6)
val colorFabInactiveDark = Color(0xFF6A6A74)
val dividerColorLight = Color(0xFFE3E4EA)
val dividerColorDark = Color(0xFF34353D)

// Toast Colors 85%
val toastNormalBgLight = Color(0xD9353A3E)
val toastNormalBgDark = Color(0xD94A4F54)
val toastSuccessBg = Color(0xD9388E3C)
val toastErrorBg = Color(0xD9D50000)
val toastInfoBg = Color(0xD90E86C4)
val toastIconCircleBg = Color(0x33FFFFFF)
val toastTextColor = Color.White

/** App font: Vazirmatn, used for every text style. */
val AppFontFamily = FontFamily(
    Font(R.font.vazirmatn_black, FontWeight.Normal),
    Font(R.font.vazirmatn_black, FontWeight.Medium),
    Font(R.font.vazirmatn_black, FontWeight.SemiBold),
    Font(R.font.vazirmatn_black, FontWeight.Bold)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private fun appTypography(): Typography {
    val base = Typography()
    val f = AppFontFamily
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = f),
        displayMedium = base.displayMedium.copy(fontFamily = f),
        displaySmall = base.displaySmall.copy(fontFamily = f),
        headlineLarge = base.headlineLarge.copy(fontFamily = f),
        headlineMedium = base.headlineMedium.copy(fontFamily = f),
        headlineSmall = base.headlineSmall.copy(fontFamily = f),
        titleLarge = base.titleLarge.copy(fontFamily = f),
        titleMedium = base.titleMedium.copy(fontFamily = f),
        titleSmall = base.titleSmall.copy(fontFamily = f),
        bodyLarge = base.bodyLarge.copy(fontFamily = f),
        bodyMedium = base.bodyMedium.copy(fontFamily = f),
        bodySmall = base.bodySmall.copy(fontFamily = f),
        labelLarge = base.labelLarge.copy(fontFamily = f),
        labelMedium = base.labelMedium.copy(fontFamily = f),
        labelSmall = base.labelSmall.copy(fontFamily = f)
    )
}

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = remember { appTypography() },
            shapes = AppShapes
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}

package me.neko.nzhelper.ui.theme

import android.app.Activity
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.core.view.WindowCompat
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.neko.nzhelper.core.datastore.ThemeSettings
import me.neko.nzhelper.core.util.BackgroundImageManager
import java.io.File
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColors
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColors
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

val LocalDarkMode = compositionLocalOf { false }
private val LocalThemePreferences = compositionLocalOf<ThemeState?> { null }
object LocalThemeState {
    val current: ThemeState
        @Composable get() = checkNotNull(LocalThemePreferences.current)

    @Composable
    fun currentOrNull(): ThemeState? = LocalThemePreferences.current
}

@Composable
private fun LowPowerColorCanvas(
    primary: Color,
    secondary: Color,
    dark: Boolean,
    blur: Float
) {
    val context = LocalContext.current
    val powerManager = remember { context.getSystemService(PowerManager::class.java) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var target by remember { mutableStateOf(0f) }
    LaunchedEffect(powerManager, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                delay(30000)
                if (powerManager?.isPowerSaveMode != true) target = 1f - target
            }
        }
    }
    val phase by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(2000),
        label = "canvasPhase"
    )
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur((blur.coerceAtLeast(18f)).dp)
    ) {
        val width = size.width
        val height = size.height
        val alpha = if (dark) 0.11f else 0.075f
        drawCircle(
            color = primary.copy(alpha = alpha),
            radius = width * 0.62f,
            center = androidx.compose.ui.geometry.Offset(
                width * (0.08f + phase * 0.18f),
                height * (0.14f + phase * 0.1f)
            )
        )
        drawCircle(
            color = secondary.copy(alpha = alpha * 0.82f),
            radius = width * 0.52f,
            center = androidx.compose.ui.geometry.Offset(
                width * (0.92f - phase * 0.2f),
                height * (0.86f - phase * 0.12f)
            )
        )
    }
}

class ThemeState(
    initialMode: ThemeSettings.ThemeMode,
    initialAmoledDark: Boolean,
    initialDynamicColor: Boolean,
    initialThemeColorIndex: Int,
    initialBackgroundImagePath: String?,
    initialBackgroundOpacity: Float,
    initialBackgroundBlur: Float,
    initialCardOpacity: Float,
    initialDialogOpacity: Float,
    initialFloatingBottomBar: Boolean,
    initialBlurEffect: Boolean
) {
    var themeMode by mutableStateOf(initialMode)
    var amoledDark by mutableStateOf(initialAmoledDark)
    var dynamicColor by mutableStateOf(initialDynamicColor)
    var themeColorIndex by mutableStateOf(initialThemeColorIndex)
    var backgroundImagePath by mutableStateOf(initialBackgroundImagePath)
    var backgroundOpacity by mutableStateOf(initialBackgroundOpacity)
    var backgroundBlur by mutableStateOf(initialBackgroundBlur)
    var cardOpacity by mutableStateOf(initialCardOpacity)
    var dialogOpacity by mutableStateOf(initialDialogOpacity)
    var floatingBottomBar by mutableStateOf(initialFloatingBottomBar)
    var blurEffect by mutableStateOf(initialBlurEffect)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NzHelperTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val themeState = remember {
        ThemeState(
            initialMode = ThemeSettings.getThemeMode(context),
            initialAmoledDark = ThemeSettings.isAmoledDark(context),
            initialDynamicColor = ThemeSettings.isDynamicColor(context),
            initialThemeColorIndex = ThemeSettings.getThemeColorIndex(context),
            initialBackgroundImagePath = ThemeSettings.getBackgroundImagePath(context),
            initialBackgroundOpacity = ThemeSettings.getBackgroundOpacity(context),
            initialBackgroundBlur = ThemeSettings.getBackgroundBlur(context),
            initialCardOpacity = ThemeSettings.getCardOpacity(context),
            initialDialogOpacity = ThemeSettings.getDialogOpacity(context),
            initialFloatingBottomBar = ThemeSettings.isFloatingBottomBar(context),
            initialBlurEffect = ThemeSettings.isBlurEffectEnabled(context)
        )
    }

    // 背景图片文件被外部删除时自动清理设置
    LaunchedEffect(themeState.backgroundImagePath) {
        val path = themeState.backgroundImagePath
        if (path != null && !File(path).exists()) {
            themeState.backgroundImagePath = null
            ThemeSettings.setBackgroundImagePath(context, null)
        }
    }

    val hazeState = rememberHazeState()

    val darkTheme = when (themeState.themeMode) {
        ThemeSettings.ThemeMode.LIGHT -> false
        ThemeSettings.ThemeMode.DARK -> true
        ThemeSettings.ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        themeState.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> {
            val option = ThemeColorOptions.getOrElse(themeState.themeColorIndex) {
                ThemeColorOptions.first()
            }
            if (darkTheme) option.darkScheme else option.lightScheme
        }
    }

    val hasBackgroundImage = themeState.backgroundImagePath != null

    val resolvedColorScheme = remember(
        darkTheme,
        themeState.amoledDark,
        hasBackgroundImage,
        themeState.cardOpacity,
        themeState.dialogOpacity,
        colorScheme
    ) {
        val base = if (darkTheme && themeState.amoledDark) {
            colorScheme.copy(
                background = AmoledBlack,
                surface = AmoledBlack,
                surfaceContainer = AmoledBlack,
                surfaceContainerLow = AmoledBlack,
                surfaceContainerLowest = AmoledBlack,
                surfaceBright = Color(0xFF0A0A0A),
                surfaceContainerHigh = Color(0xFF0E0E0E),
                surfaceContainerHighest = Color(0xFF141414),
                surfaceDim = AmoledBlack
            )
        } else {
            colorScheme
        }
        val whiteFirst = if (!darkTheme) {
            base.copy(
                background = Color.White,
                surface = Color.White,
                surfaceContainer = Color.Transparent
            )
        } else {
            base.copy(surfaceContainer = Color.Transparent)
        }
        if (hasBackgroundImage) {
            whiteFirst.copy(
                surfaceContainer = Color.Transparent,
                surfaceBright = whiteFirst.surfaceBright.copy(alpha = themeState.cardOpacity),
                surfaceContainerHigh = whiteFirst.surfaceContainerHigh.copy(alpha = themeState.dialogOpacity),
                surfaceContainerHighest = whiteFirst.surfaceContainerHighest.copy(alpha = themeState.dialogOpacity)
            )
        } else {
            whiteFirst
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkMode provides darkTheme,
        LocalThemePreferences provides themeState,
        LocalHazeState provides hazeState,
        LocalOverscrollFactory provides null
    ) {
        MaterialExpressiveTheme(
            colorScheme = resolvedColorScheme,
            shapes = Shapes,
            motionScheme = MotionScheme.expressive(),
            typography = Typography,
        ) {
            val bgPath = themeState.backgroundImagePath
            val bgBitmap by produceState<ImageBitmap?>(initialValue = null, bgPath) {
                value = if (bgPath == null) {
                    null
                } else {
                    withContext(Dispatchers.IO) {
                        BackgroundImageManager.loadImageBitmap(bgPath)?.asImageBitmap()
                    }
                }
            }
            val backgroundOverlay = resolvedColorScheme.background.copy(
                alpha = (1f - themeState.backgroundOpacity).coerceIn(0f, 1f)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(resolvedColorScheme.background)
            ) {
                Box(Modifier.fillMaxSize().then(
                    if (themeState.blurEffect) Modifier.hazeSource(hazeState, zIndex = -1f) else Modifier
                )) {
                    LowPowerColorCanvas(
                        primary = resolvedColorScheme.primary,
                        secondary = resolvedColorScheme.tertiary,
                        dark = darkTheme,
                        blur = themeState.backgroundBlur
                    )
                val bitmap = bgBitmap
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.1f)
                            .blur(themeState.backgroundBlur.dp),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundOverlay)
                    )
                }
                }
                val glassColor = glassOverlayColor()
                val miuixColors = remember(resolvedColorScheme, glassColor, darkTheme) {
                    val scheme = resolvedColorScheme
                    val colors = if (darkTheme) miuixDarkColors() else miuixLightColors()
                    colors.copy(
                        primary = scheme.primary,
                        onPrimary = scheme.onPrimary,
                        primaryVariant = scheme.primary,
                        background = scheme.background,
                        onBackground = scheme.onBackground,
                        surface = scheme.surface,
                        onSurface = scheme.onSurface,
                        onSurfaceVariantSummary = scheme.onSurfaceVariant,
                        surfaceContainer = glassColor,
                        onSurfaceContainer = scheme.onSurface,
                        error = scheme.error,
                        onError = scheme.onError
                    )
                }
                MiuixTheme(colors = miuixColors, content = content)
            }
        }
    }
}

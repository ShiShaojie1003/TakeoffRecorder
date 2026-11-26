package me.neko.nzhelper.ui.component.setting

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import me.neko.nzhelper.ui.theme.LiquidGlass
import me.neko.nzhelper.ui.theme.LocalDarkMode
import me.neko.nzhelper.ui.theme.liquidGlassBorderColor
import me.neko.nzhelper.ui.theme.liquidGlassContainerColor
import me.neko.nzhelper.ui.theme.liquidGlassHighlightColor

val SettingsCornerRadius = 24.dp
val SettingsConnectionRadius = 6.dp
private val GroupGap = 2.dp

data class SettingsItemCorners(val topRadius: Dp, val bottomRadius: Dp)

val LocalSettingsItemCorners = compositionLocalOf {
    SettingsItemCorners(SettingsCornerRadius, SettingsCornerRadius)
}

@DslMarker
annotation class SettingsCardDsl

@SettingsCardDsl
class SettingsCardScope {
    internal val items = mutableListOf<@Composable () -> Unit>()

    fun item(content: @Composable () -> Unit) {
        items.add(content)
    }
}

@Composable
fun TrailingArrowIcon() {
    val tint = if (LocalDarkMode.current) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
    }
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: SettingsCardScope.() -> Unit
) {
    val scope = SettingsCardScope().apply(content)
    if (scope.items.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        scope.items.forEachIndexed { index, itemContent ->
            val isFirst = index == 0
            val isLast = index == scope.items.lastIndex
            val corners = SettingsItemCorners(
                topRadius = if (isFirst) SettingsCornerRadius else SettingsConnectionRadius,
                bottomRadius = if (isLast) SettingsCornerRadius else SettingsConnectionRadius
            )
            CompositionLocalProvider(LocalSettingsItemCorners provides corners) {
                itemContent()
            }
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GroupGap)
                )
            }
        }
    }
}

@Composable
fun SettingsItemSurface(
    modifier: Modifier = Modifier,
    containerColor: Color = liquidGlassContainerColor(),
    content: @Composable ColumnScope.() -> Unit
) {
    val corners = LocalSettingsItemCorners.current
    val shape = RoundedCornerShape(
        topStart = corners.topRadius,
        topEnd = corners.topRadius,
        bottomEnd = corners.bottomRadius,
        bottomStart = corners.bottomRadius
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        liquidGlassHighlightColor(),
                        Color.Transparent,
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = 96f
                )
            )
            .border(1.dp, liquidGlassBorderColor(), shape),
        content = content
    )
}

@Composable
fun SettingsItemHost(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val corners = LocalSettingsItemCorners.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val topRadius by animateDpAsState(
        targetValue = if (pressed) SettingsCornerRadius else corners.topRadius,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "settingsItemHostTopRadius"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (pressed) SettingsCornerRadius else corners.bottomRadius,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "settingsItemHostBottomRadius"
    )
    val shape = RoundedCornerShape(
        topStart = topRadius,
        topEnd = topRadius,
        bottomEnd = bottomRadius,
        bottomStart = bottomRadius
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(liquidGlassContainerColor())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        liquidGlassHighlightColor(),
                        Color.Transparent,
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = 96f
                )
            )
            .border(1.dp, liquidGlassBorderColor(), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onClick()
                        }
                    )
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    selected: Boolean = false,
    badgeText: String? = null,
    iconTint: Color? = null,
    iconSize: Dp = 24.dp,
    leadingModifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val container = if (selected) colors.primary.copy(alpha = 0.12f) else liquidGlassContainerColor()
    val foreground = if (selected) colors.primary else titleColor
    val corners = LocalSettingsItemCorners.current
    LiquidGlass(
        modifier.fillMaxWidth(),
        RoundedCornerShape(
            topStart = corners.topRadius, topEnd = corners.topRadius,
            bottomStart = corners.bottomRadius, bottomEnd = corners.bottomRadius
        ),
        container
    ) {
        BasicComponent(
            title = title,
            summary = subtitle,
            titleColor = BasicComponentColors(foreground, foreground.copy(alpha = 0.38f)),
            summaryColor = BasicComponentColors(subtitleColor, subtitleColor.copy(alpha = 0.38f)),
            enabled = enabled,
            onClick = onClick,
            startAction = {
                Box(
                    Modifier.size(32.dp).then(leadingModifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon, contentDescription = null,
                        tint = iconTint ?: if (selected) colors.primary else colors.onSurfaceVariant,
                        modifier = Modifier.size(iconSize)
                    )
                }
            },
            endActions = {
                if (badgeText != null) Badge { Text(badgeText) }
                if (trailingContent != null) trailingContent() else TrailingArrowIcon()
            }
        )
    }
}

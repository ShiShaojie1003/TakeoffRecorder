package me.neko.nzhelper.ui.component.miuix

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import me.neko.nzhelper.ui.theme.LiquidGlass
import me.neko.nzhelper.ui.theme.glassBackdrop
import me.neko.nzhelper.ui.theme.glassOverlayColor
import me.neko.nzhelper.ui.theme.liquidGlassContainerColor
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun GlassTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null
) {
    TopAppBar(
        title = title,
        modifier = Modifier.glassBackdrop(liquidGlassContainerColor()),
        color = Color.Transparent,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun GlassMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    OverlayListPopup(
        show = expanded,
        popupModifier = modifier,
        alignment = PopupPositionProvider.Align.End,
        enableWindowDim = false,
        onDismissRequest = onDismissRequest
    ) {
        ListPopupColumn(content)
    }
}

@Composable
fun GlassMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    BasicComponent(
        startAction = leadingIcon,
        onClick = onClick,
        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        content = { text() }
    )
}

@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null
) {
    WindowDialog(
        show = true,
        modifier = modifier,
        backgroundColor = glassOverlayColor(),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon?.invoke()
            title?.invoke()
            text?.invoke()
            confirmButton()
            dismissButton?.invoke()
        }
    }
}

@Composable
fun GlassSheet(
    onDismissRequest: () -> Unit,
    dragHandle: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    WindowBottomSheet(
        show = true,
        backgroundColor = glassOverlayColor(),
        onDismissRequest = onDismissRequest,
        dragHandleColor = if (dragHandle == null) MaterialTheme.colorScheme.outline else Color.Transparent,
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    colors: androidx.compose.material3.CardColors? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = liquidGlassContainerColor()
    val color = colors?.containerColor?.let { it.copy(alpha = minOf(it.alpha, glass.alpha)) } ?: glass
    LiquidGlass(modifier, shape, color) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                (colors?.contentColor ?: MaterialTheme.colorScheme.onSurface)
        ) {
            Column(
                modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
                content = content
            )
        }
    }
}

@Composable
fun GlassCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Checkbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = onCheckedChange?.let { { it(!checked) } },
        modifier = modifier,
        enabled = enabled
    )
}

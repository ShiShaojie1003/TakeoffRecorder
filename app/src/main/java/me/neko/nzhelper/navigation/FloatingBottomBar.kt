package me.neko.nzhelper.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.neko.nzhelper.ui.theme.LocalThemeState
import me.neko.nzhelper.ui.theme.glassBackdrop
import me.neko.nzhelper.ui.theme.liquidGlassBorderColor
import me.neko.nzhelper.ui.theme.liquidGlassContainerColor
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDefaults
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun floatingBarContentBottomInset(): Dp =
    if (LocalThemeState.current.floatingBottomBar) floatingBarBottomOverlayHeight() else 0.dp

@Composable
fun floatingBarBottomOverlayHeight(): Dp = with(LocalDensity.current) {
    WindowInsets.navigationBars.getBottom(this).toDp() + 84.dp
}

@Composable
fun RecordNavigationBar(
    pagerState: PagerState,
    onItemClick: (Int) -> Unit,
    floating: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = liquidGlassContainerColor()
    val shape = RoundedCornerShape(28.dp)
    val frame = if (floating) {
        Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            .clip(shape)
            .border(1.dp, liquidGlassBorderColor(), shape)
    } else {
        Modifier
    }
    val palette = MiuixTheme.colorScheme
    val colors = NavigationBarDefaults.navigationBarItemColors(
        selectedContentColor = palette.primary,
        unselectedContentColor = palette.onSurfaceVariantSummary
    )

    NavigationBar(
        modifier = modifier.then(frame).glassBackdrop(tint, radius = 24.dp),
        color = Color.Transparent,
        showDivider = false,
        defaultWindowInsetsPadding = !floating
    ) {
        BottomNavItem.items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = pagerState.currentPage == index,
                onClick = { onItemClick(index) },
                icon = item.icon,
                label = item.title,
                colors = colors
            )
        }
    }
}

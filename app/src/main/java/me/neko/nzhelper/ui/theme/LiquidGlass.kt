package me.neko.nzhelper.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * iOS 27「液态玻璃」风格的页卡容器。
 *
 * 参考 Apple Liquid Glass 设计规范，通过三个要素还原玻璃质感：
 * 1. 半透明容器 — 让背景内容若隐若现，形成景深；
 * 2. 顶部高光 — 一道自上而下的白色渐变，模拟玻璃受光边缘的折射；
 * 3. 极淡描边 — 一圈 1dp 低透明度描边，勾勒玻璃轮廓。
 *
 * 颜色随主题自动适配：日间为半透明白（叠加在纯白/浅灰背景上依然干净），
 * 夜间为半透明深色（叠加在 AMOLED 纯黑背景上保持深邃）。
 *
 * @param shape 圆角形状，缺省 16.dp（与设置卡片一致）。
 * @param containerColor 玻璃底色，缺省自动按主题派生。
 */
@Composable
fun liquidGlassContainerColor(): Color =
    MaterialTheme.colorScheme.surfaceBright.copy(
        alpha = if (LocalDarkMode.current) 0.66f else 0.72f
    )

@Composable
fun liquidGlassBorderColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(
        alpha = if (LocalDarkMode.current) 0.12f else 0.08f
    )

@Composable
fun liquidGlassHighlightColor(): Color =
    Color.White.copy(alpha = if (LocalDarkMode.current) 0.10f else 0.55f)

@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = liquidGlassContainerColor(),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, liquidGlassBorderColor(), shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
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
        )
        content()
    }
}

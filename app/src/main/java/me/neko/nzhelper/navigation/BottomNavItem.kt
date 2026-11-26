package me.neko.nzhelper.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(val title: String, val icon: ImageVector) {
    Home("计时", Icons.Outlined.Timer),
    Statistics("统计", Icons.Outlined.BarChart),
    History("历史", Icons.Outlined.History),
    Mine("我的", Icons.Outlined.Person);

    companion object {
        val items = entries
    }
}

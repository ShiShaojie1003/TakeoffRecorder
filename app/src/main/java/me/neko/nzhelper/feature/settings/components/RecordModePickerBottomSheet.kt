package me.neko.nzhelper.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Female
import androidx.compose.material.icons.outlined.Male
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.neko.nzhelper.core.model.SessionMode
import me.neko.nzhelper.ui.component.setting.SettingsCard
import me.neko.nzhelper.ui.component.setting.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordModePickerBottomSheet(
    currentMode: SessionMode,
    onConfirm: (SessionMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(currentMode) }

    @Suppress("DEPRECATION")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "默认记录模式",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                SessionMode.entries.forEach { mode ->
                    val isSelected = selected == mode
                    item {
                        SettingsItem(
                            icon = when (mode) {
                                SessionMode.SOLO_MALE -> Icons.Outlined.Male
                                SessionMode.SOLO_FEMALE -> Icons.Outlined.Female
                                SessionMode.PAIR -> Icons.Outlined.FavoriteBorder
                            },
                            title = mode.label,
                            subtitle = when (mode) {
                                SessionMode.SOLO_MALE -> "记录个人（男性）活动，高潮按次数记录。"
                                SessionMode.SOLO_FEMALE -> "记录个人（女性）活动，支持多次高潮计数与女性向建议。"
                                SessionMode.PAIR -> "记录双人性生活，可填写对方性别、昵称、高潮次数与避孕措施等。"
                            },
                            selected = isSelected,
                            onClick = { selected = mode },
                            trailingContent = {
                                RadioButton(selected = isSelected, onClick = null)
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("取消")
                }
                Button(
                    onClick = { onConfirm(selected) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("确定")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

package me.neko.nzhelper.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import me.neko.nzhelper.core.model.Session
import me.neko.nzhelper.core.util.formatTime
import me.neko.nzhelper.ui.component.setting.TrailingArrowIcon

/**
 * 最近记录卡片：展示最近七天的记录；若七天内无记录则回退为最近三条。
 * 点击条目回调 [onRecordClick] 打开记录详情。
 */
@Composable
fun RecentRecordsCard(
    records: List<Session>,
    onRecordClick: (Session) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceBright
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "最近记录",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            if (records.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "暂无记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    records.forEach { session ->
                        RecentRecordRow(
                            session = session,
                            onClick = { onRecordClick(session) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRecordRow(
    session: Session,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val dateText = remember(session.timestamp) {
        val daysAgo = ChronoUnit.DAYS.between(session.timestamp.toLocalDate(), LocalDate.now())
        when (daysAgo) {
            0L -> "今天"
            1L -> "昨天"
            else -> session.timestamp.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA))
        }
    }
    val timeText = remember(session.timestamp) {
        session.timestamp.format(DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$dateText $timeText",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatTime(session.duration),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = colorScheme.primary
        )
        TrailingArrowIcon()
    }
}

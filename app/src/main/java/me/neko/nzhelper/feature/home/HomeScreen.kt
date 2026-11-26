package me.neko.nzhelper.feature.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.LocalDate
import java.time.LocalDateTime
import me.neko.nzhelper.core.database.SessionRepository
import me.neko.nzhelper.core.database.StatisticsRepository
import me.neko.nzhelper.core.datastore.AgeGroupSettings
import me.neko.nzhelper.core.datastore.TimerSettings
import me.neko.nzhelper.core.model.Session
import me.neko.nzhelper.core.service.TimerService
import me.neko.nzhelper.feature.addrecord.AddRecordFlow
import me.neko.nzhelper.feature.home.components.ConfirmResetDialog
import me.neko.nzhelper.feature.home.components.ConfirmStopDialog
import me.neko.nzhelper.feature.home.components.RecentRecordsCard
import me.neko.nzhelper.feature.home.components.TimerCard
import me.neko.nzhelper.feature.history.components.SessionDetailDialog
import me.neko.nzhelper.navigation.floatingBarContentBottomInset
import me.neko.nzhelper.ui.component.dialog.CustomAppAlertDialog

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
    isActive: Boolean = false,
    stopRequestId: Int = 0,
    onStopRequestConsumed: (Int) -> Unit = {},
    onOpenAddRecord: (AddRecordFlow, Int) -> Unit = { _, _ -> },
    onOpenHistory: () -> Unit = {},
    onOpenEditRecord: (Session) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // 绑定 Service
    val serviceIntent = remember { Intent(context, TimerService::class.java) }
    var timerService by remember { mutableStateOf<TimerService?>(null) }
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                timerService = (binder as TimerService.LocalBinder).getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                timerService = null
            }
        }
    }

    // 启动并绑定服务
    LaunchedEffect(Unit) {
        val autoStart = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getBoolean("auto_start_timer", false)

        if (autoStart) {
            ContextCompat.startForegroundService(
                context,
                serviceIntent.apply { action = TimerService.ACTION_START }
            )
        }
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }
    DisposableEffect(Unit) {
        onDispose { context.unbindService(connection) }
    }

    val elapsedSeconds by timerService?.elapsedSec?.collectAsState(initial = 0)
        ?: remember { mutableIntStateOf(0) }
    val isServiceRunning by timerService?.isRunning?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    var floatingTimerEnabled by remember { mutableStateOf(TimerSettings.isFloatingEnabled(context)) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var pendingFloatingGrant by remember { mutableStateOf(false) }
    val toggleFloatingTimer: (Boolean) -> Unit = { enabled ->
        if (enabled && !Settings.canDrawOverlays(context)) {
            showOverlayPermissionDialog = true
        } else {
            floatingTimerEnabled = enabled
            TimerSettings.setFloatingEnabled(context, enabled)
            timerService?.applyFloatingPreference()
        }
    }
    val sessions = remember { mutableStateListOf<Session>() }
    var isLoading by remember { mutableStateOf(true) }
    var resumeKey by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeKey++
                floatingTimerEnabled = TimerSettings.isFloatingEnabled(context)
                if (pendingFloatingGrant && Settings.canDrawOverlays(context)) {
                    pendingFloatingGrant = false
                    floatingTimerEnabled = true
                    TimerSettings.setFloatingEnabled(context, true)
                    timerService?.applyFloatingPreference()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(stopRequestId, timerService) {
        if (stopRequestId > 0 && timerService != null) {
            // A notification tap is a one-shot event. Clear it before showing
            // the dialog so Pager disposal/recreation cannot replay it.
            onStopRequestConsumed(stopRequestId)
            showConfirmDialog = true
        }
    }

    LaunchedEffect(Unit) {
        val loaded = SessionRepository.loadSessions(context)
            .sortedByDescending { it.timestamp }
        sessions.clear()
        sessions.addAll(loaded)
        isLoading = false
    }
    LaunchedEffect(isActive) {
        if (isActive) {
            val loaded = SessionRepository.loadSessions(context)
                .sortedByDescending { it.timestamp }
            sessions.clear()
            sessions.addAll(loaded)
            isLoading = false
        }
    }

    val latestInfo by remember(sessions) {
        derivedStateOf { StatisticsRepository.calculateLatestInfo(sessions) }
    }

    // 最近记录：优先展示最近七天；七天内无记录则回退为最近三条
    val recentRecords by remember(sessions) {
        derivedStateOf {
            val weekAgo = LocalDateTime.now().minusDays(7)
            val inWeek = sessions.filter { !it.timestamp.isBefore(weekAgo) }
            if (inWeek.isNotEmpty()) inWeek else sessions.take(3)
        }
    }

    var detailSession by remember { mutableStateOf<Session?>(null) }

    val birthdayGreeting = remember {
        try {
            if (!AgeGroupSettings.isBirthDateSet(context)) return@remember null
            val birth = AgeGroupSettings.getBirthDate(context)
            val today = LocalDate.now()
            if (birth.month == today.month && birth.dayOfMonth == today.dayOfMonth)
                "今天对自己好一点，放松心情享受生活吧～"
            else null
        } catch (_: Exception) {
            null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(text = "记录") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        )
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 16.dp + floatingBarContentBottomInset()
                )
            ) {
                item {
                    TimerCard(
                        elapsedSeconds = elapsedSeconds,
                        isRunning = isServiceRunning,
                        latestInfo = latestInfo,
                        isLoading = isLoading,
                        floatingEnabled = floatingTimerEnabled,
                        onToggleFloating = { toggleFloatingTimer(!floatingTimerEnabled) },
                        onToggleRun = {
                            if (isServiceRunning) {
                                context.startService(serviceIntent.apply {
                                    action = TimerService.ACTION_PAUSE
                                })
                            } else {
                                ContextCompat.startForegroundService(
                                    context,
                                    serviceIntent.apply { action = TimerService.ACTION_START }
                                )
                            }
                        },
                        onStop = {
                            if (elapsedSeconds > 0) showConfirmDialog = true
                            else Toast.makeText(context, "计时尚未开始", Toast.LENGTH_SHORT).show()
                        },
                        onReset = {
                            if (elapsedSeconds > 0) showResetConfirmDialog = true
                            else Toast.makeText(context, "计时尚未开始", Toast.LENGTH_SHORT).show()
                        },
                        onOpenHistory = onOpenHistory
                    )
                }
                item {
                    FilledTonalButton(
                        onClick = { onOpenAddRecord(AddRecordFlow.MANUAL, 0) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "手动添加记录",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (birthdayGreeting != null) {
                    item {
                        BirthdayCard(message = birthdayGreeting)
                    }
                }
                item {
                    RecentRecordsCard(
                        records = recentRecords,
                        onRecordClick = { detailSession = it }
                    )
                }
            }
        }
    }

    detailSession?.let { session ->
        SessionDetailDialog(
            session = session,
            onDismiss = { detailSession = null },
            onEditClick = {
                val target = session
                detailSession = null
                onOpenEditRecord(target)
            }
        )
    }

    if (showOverlayPermissionDialog) {
        CustomAppAlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            iconVector = Icons.Outlined.PictureInPictureAlt,
            title = "需要悬浮窗权限",
            message = "计时悬浮窗需要「显示在其他应用上层」权限，请在系统设置中开启后再打开此开关。",
            confirmText = "去授权",
            confirmIcon = Icons.Outlined.Settings,
            dismissText = "取消",
            onConfirm = {
                showOverlayPermissionDialog = false
                pendingFloatingGrant = true
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    if (showConfirmDialog) {
        ConfirmStopDialog(
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                context.startService(serviceIntent.apply {
                    action = TimerService.ACTION_PAUSE
                })
                onOpenAddRecord(AddRecordFlow.TIMER, elapsedSeconds)
            }
        )
    }

    if (showResetConfirmDialog) {
        ConfirmResetDialog(
            onDismiss = { showResetConfirmDialog = false },
            onConfirm = {
                showResetConfirmDialog = false
                context.startService(serviceIntent.apply {
                    action = TimerService.ACTION_RESET
                })
            }
        )
    }
}

@Composable
private fun BirthdayCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Celebration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "生日快乐 🎂",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

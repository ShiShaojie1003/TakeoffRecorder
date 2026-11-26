package me.neko.nzhelper.feature.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.ViewInAr
import me.neko.nzhelper.ui.component.miuix.GlassDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import me.neko.nzhelper.ui.component.miuix.GlassCard
import androidx.compose.material3.CardDefaults
import me.neko.nzhelper.ui.component.miuix.GlassMenu
import me.neko.nzhelper.ui.component.miuix.GlassMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import me.neko.nzhelper.ui.component.miuix.GlassTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import top.yukonga.miuix.kmp.basic.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.neko.nzhelper.core.ai.AiProvider
import me.neko.nzhelper.core.ai.AiSettings
import me.neko.nzhelper.feature.ai.components.AiProviderEditDialog
import me.neko.nzhelper.feature.ai.components.ModelPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiProviderListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior =
        top.yukonga.miuix.kmp.basic.MiuixScrollBehavior()

    var providers by remember { mutableStateOf<List<AiProvider>>(emptyList()) }
    var editingProvider by remember { mutableStateOf<AiProvider?>(null) }
    var deleteTarget by remember { mutableStateOf<AiProvider?>(null) }
    var modelPickerProvider by remember { mutableStateOf<AiProvider?>(null) }

    LaunchedEffect(Unit) {
        providers = AiSettings.getProviders(context)
    }

    fun refresh() {
        scope.launch { providers = AiSettings.getProviders(context) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            GlassTopAppBar(
                title = "供应商",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "返回")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingProvider = AiProvider.create() }
            ) {
                Icon(Icons.Outlined.Add, "添加供应商")
            }
        }
    ) { innerPadding ->
        if (providers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "还没有配置 AI 供应商",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "点击右下角 + 添加",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        onClick = { editingProvider = provider },
                        onDelete = { deleteTarget = provider },
                        onPickModel = { modelPickerProvider = provider },
                        onActivate = {
                            scope.launch {
                                AiSettings.setActive(context, provider.id)
                                refresh()
                            }
                        }
                    )
                }
            }
        }
    }

    if (editingProvider != null) {
        AiProviderEditDialog(
            provider = editingProvider!!,
            onDismiss = { editingProvider = null },
            onSaved = {
                refresh()
                editingProvider = null
            }
        )
    }

    if (deleteTarget != null) {
        GlassDialog(
            onDismissRequest = { deleteTarget = null },
            icon = {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "删除供应商",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "确定要删除「${deleteTarget!!.name.ifBlank { "未命名" }}」吗？此操作不可恢复。",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            AiSettings.deleteProvider(context, deleteTarget!!.id)
                            deleteTarget = null
                            refresh()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("删除") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deleteTarget = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) { Text("取消") }
            }
        )
    }

    if (modelPickerProvider != null) {
        ModelPickerDialog(
            provider = modelPickerProvider!!,
            onDismiss = { modelPickerProvider = null },
            onConfirmed = { model, cached, manual ->
                scope.launch {
                    AiSettings.saveProvider(
                        context,
                        modelPickerProvider!!.copy(
                            model = model,
                            cachedModels = cached,
                            manualModels = manual
                        )
                    )
                    modelPickerProvider = null
                    refresh()
                }
            },
            onChanged = { cached, manual ->
                scope.launch {
                    AiSettings.saveProvider(
                        context,
                        modelPickerProvider!!.copy(
                            cachedModels = cached,
                            manualModels = manual
                        )
                    )
                    refresh()
                }
            }
        )
    }
}

@Composable
private fun ProviderCard(
    provider: AiProvider,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPickModel: () -> Unit,
    onActivate: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    GlassCard(
        onClick = onActivate,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (provider.isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (provider.isActive) Icons.Outlined.CheckCircle
                else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (provider.isActive) "当前使用" else "点击启用",
                tint = if (provider.isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name.ifBlank { "未命名" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (provider.cachedModels.isNotEmpty())
                        "${provider.model} · ${provider.cachedModels.size} 个模型"
                    else "暂无模型",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        "更多",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                GlassMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    GlassMenuItem(
                        text = { Text("选择模型") },
                        onClick = { menuExpanded = false; onPickModel() },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.ViewInAr,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    GlassMenuItem(
                        text = { Text("编辑") },
                        onClick = { menuExpanded = false; onClick() },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Edit,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    GlassMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDelete() },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

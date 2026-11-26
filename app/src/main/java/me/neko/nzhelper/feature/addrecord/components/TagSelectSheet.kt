package me.neko.nzhelper.feature.addrecord.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.neko.nzhelper.core.datastore.TagSettings
import me.neko.nzhelper.core.model.SessionMode
import me.neko.nzhelper.core.model.TagDef
import me.neko.nzhelper.ui.component.dialog.ConfirmDialog
import me.neko.nzhelper.ui.component.tag.TagChip
import me.neko.nzhelper.ui.theme.TagColors
import me.neko.nzhelper.ui.theme.TagIcons
import me.neko.nzhelper.ui.theme.glassOverlayColor
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagSelectCard(
    title: String,
    groupId: String,
    mode: SessionMode,
    selectedIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    singleSelect: Boolean = false,
    autoMatchLabel: String? = null,
    onAutoMatch: ((Set<String>) -> Set<String>)? = null
) {
    val context = LocalContext.current
    fun loadTags() = TagSettings.getTags(context).filter { it.groupId == groupId && it.appliesTo(mode) }

    var tags by remember(groupId, mode) { mutableStateOf(loadTags()) }
    var sheet by remember(groupId, mode) { mutableStateOf<TagSheetEntry?>(null) }
    val selected = tags.filter { it.id in selectedIds }

    Column {
        BasicComponent(
            title = title,
            summary = if (selected.isEmpty()) "未选择" else null,
            insideMargin = PaddingValues(0.dp),
            onClick = {
                tags = loadTags()
                sheet = TagSheetEntry.Picker
            },
            endActions = {
                IconButton(onClick = {
                    tags = loadTags()
                    sheet = TagSheetEntry.NewTag
                }) {
                    Icon(Icons.Outlined.Add, contentDescription = "新建${title}标签")
                }
            }
        )
        if (selected.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                selected.forEach { TagChip(name = it.name, color = it.color, icon = it.icon) }
            }
        }
    }

    sheet?.let { entry ->
        GroupTagSheet(
            title = title,
            entry = entry,
            tags = tags,
            selectedIds = selectedIds,
            singleSelect = singleSelect,
            autoMatchLabel = autoMatchLabel,
            onAutoMatch = onAutoMatch,
            onSaveTag = { original, name ->
                val existing = TagSettings.findTagByName(context, name)
                if (existing != null && existing.id != original?.id) {
                    null
                } else if (original == null) {
                    TagSettings.ensureRecordGroup(context, groupId, title)
                    val group = TagSettings.getGroup(context, groupId)
                    TagSettings.addTag(
                        context, name, groupId, group?.icon ?: "hash", group?.color ?: "slate",
                        modeKeys = group?.modeKeys.orEmpty()
                    )?.also { tags = loadTags() }
                } else {
                    TagSettings.updateTag(context, original.id, name = name)
                    tags = loadTags()
                    tags.firstOrNull { it.id == original.id }
                }
            },
            onDeleteTag = { tag ->
                TagSettings.deleteTag(context, tag.id)
                tags = loadTags()
                if (tag.id in selectedIds) onSelectionChange(selectedIds - tag.id)
            },
            onConfirm = {
                onSelectionChange(it)
                sheet = null
            },
            onDismiss = { sheet = null }
        )
    }
}

private enum class TagSheetEntry { Picker, NewTag }

@Composable
private fun GroupTagSheet(
    title: String,
    entry: TagSheetEntry,
    tags: List<TagDef>,
    selectedIds: Set<String>,
    singleSelect: Boolean,
    autoMatchLabel: String?,
    onAutoMatch: ((Set<String>) -> Set<String>)?,
    onSaveTag: (TagDef?, String) -> TagDef?,
    onDeleteTag: (TagDef) -> Unit,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selection by remember { mutableStateOf(selectedIds) }
    var editing by remember { mutableStateOf(entry == TagSheetEntry.NewTag) }
    var editorTag by remember { mutableStateOf<TagDef?>(null) }
    var pendingDelete by remember { mutableStateOf<TagDef?>(null) }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val initiallySelected = remember { selectedIds }

    fun closeEditor() {
        if (entry == TagSheetEntry.NewTag) onDismiss() else editing = false
    }

    fun edit(tag: TagDef?) {
        editorTag = tag
        name = tag?.name.orEmpty()
        error = false
        editing = true
    }

    fun saveTag() {
        if (name.isBlank()) return
        val saved = onSaveTag(editorTag, name.trim())
        when {
            saved == null -> error = true
            editorTag == null -> onConfirm(if (singleSelect) setOf(saved.id) else selection + saved.id)
            else -> closeEditor()
        }
    }

    WindowBottomSheet(
        show = true,
        title = if (!editing) title else if (editorTag == null) "新建${title}标签" else "编辑${title}标签",
        backgroundColor = glassOverlayColor(),
        onDismissRequest = { if (editing) closeEditor() else onDismiss() }
    ) {
        Column(Modifier.fillMaxWidth().imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editing) {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = false
                    },
                    label = "标签名称",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { saveTag() }),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Text("这个标签名称已存在", color = MaterialTheme.colorScheme.error)
                }
                editorTag?.let { tag ->
                    BasicComponent(
                        title = "删除标签",
                        onClick = { pendingDelete = tag }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = ::closeEditor, modifier = Modifier.weight(1f)) {
                        Text(if (entry == TagSheetEntry.NewTag) "取消" else "返回")
                    }
                    Button(
                        onClick = ::saveTag,
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) { Text(if (editorTag == null) "添加并选中" else "保存") }
                }
            } else {
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    item {
                        BasicComponent(
                            title = "新建${title}标签",
                            startAction = { Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = { edit(null) }
                        )
                    }
                    items(tags.sortedBy { it.id !in initiallySelected }, key = { it.id }) { tag ->
                        val checked = tag.id in selection
                        BasicComponent(
                            title = tag.name,
                            startAction = {
                                Icon(TagIcons.iconFor(tag.icon), null, tint = TagColors.contentColor(tag.color))
                            },
                            onClick = {
                                selection = when {
                                    checked -> selection - tag.id
                                    singleSelect -> setOf(tag.id)
                                    else -> selection + tag.id
                                }
                            },
                            endActions = {
                                if (checked) Icon(Icons.Outlined.Check, "已选中", tint = MaterialTheme.colorScheme.primary)
                                IconButton(onClick = { edit(tag) }) {
                                    Icon(Icons.Outlined.Edit, "编辑${tag.name}")
                                }
                            }
                        )
                    }
                }
                if (autoMatchLabel != null && onAutoMatch != null) {
                    BasicComponent(title = autoMatchLabel, onClick = { selection = onAutoMatch(selection) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("取消") }
                    Button(
                        onClick = { onConfirm(selection.intersect(tags.map { it.id }.toSet())) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) { Text("确定") }
                }
            }
        }
    }
    pendingDelete?.let { tag ->
        ConfirmDialog(
            title = "删除标签",
            message = "删除「${tag.name}」后，历史记录仍保留原标签。",
            confirmText = "删除",
            onDismiss = { pendingDelete = null },
            onConfirm = {
                onDeleteTag(tag)
                selection -= tag.id
                pendingDelete = null
                editing = false
            }
        )
    }
}

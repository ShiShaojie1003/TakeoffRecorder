package me.neko.nzhelper.feature.addrecord.components

import androidx.compose.runtime.Composable
import me.neko.nzhelper.core.datastore.TagSettings
import me.neko.nzhelper.core.model.SessionFormState
import me.neko.nzhelper.core.model.SessionMode
import me.neko.nzhelper.ui.component.form.ContraceptionSection
import me.neko.nzhelper.ui.component.form.PartnerGenderSection
import me.neko.nzhelper.ui.component.form.SettingsSection
import me.neko.nzhelper.ui.component.setting.SettingsCard

@Composable
internal fun PartnerPage(
    formState: SessionFormState,
    onFormStateChange: (SessionFormState) -> Unit
) {

    SettingsCard {
        item {
            SettingsSection {
                PartnerGenderSection(formState, onFormStateChange)
            }
        }
        item {
            SettingsSection {
                TagSelectCard(
                    title = "体位",
                    groupId = TagSettings.GROUP_POSITION,
                    mode = SessionMode.fromKey(formState.mode),
                    selectedIds = formState.positions,
                    onSelectionChange = {
                        onFormStateChange(formState.copy(positions = it))
                    }
                )
            }
        }
        item {
            SettingsSection {
                TagSelectCard(
                    title = "情趣玩具",
                    groupId = TagSettings.LEGACY_GROUP_TOOL,
                    mode = SessionMode.fromKey(formState.mode),
                    selectedIds = formState.toys,
                    onSelectionChange = {
                        onFormStateChange(formState.copy(toys = it))
                    }
                )
            }
        }
        item {
            SettingsSection {
                ContraceptionSection(formState, onFormStateChange)
            }
        }
        item {
            SettingsSection {
                TagSelectCard(
                    title = "射精方式",
                    groupId = TagSettings.GROUP_EJACULATE,
                    mode = SessionMode.fromKey(formState.mode),
                    selectedIds = setOfNotNull(formState.ejaculation.takeIf { it.isNotBlank() }),
                    singleSelect = true,
                    onSelectionChange = {
                        onFormStateChange(
                            formState.copy(ejaculation = it.firstOrNull() ?: "")
                        )
                    }
                )
            }
        }
    }
}

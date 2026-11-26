package me.neko.nzhelper.feature.addrecord.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.neko.nzhelper.core.datastore.TagSettings
import me.neko.nzhelper.core.model.SessionFormState
import me.neko.nzhelper.core.model.SessionMode
import me.neko.nzhelper.ui.component.form.ClimaxCountSection
import me.neko.nzhelper.ui.component.form.RatingSection
import me.neko.nzhelper.ui.component.form.SettingsSection
import me.neko.nzhelper.ui.component.setting.SettingsCard

@Composable
internal fun RatingPage(
    formState: SessionFormState,
    onFormStateChange: (SessionFormState) -> Unit
) {

    SettingsCard {
        item {
            SettingsSection {
                RatingSection(
                    rating = formState.rating,
                    onRatingChange = {
                        onFormStateChange(formState.copy(rating = it))
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    SettingsCard {
        item {
            SettingsSection {
                ClimaxCountSection(formState, onFormStateChange)
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    SettingsCard {
        item {
            SettingsSection {
                TagSelectCard(
                    title = "情绪",
                    groupId = TagSettings.LEGACY_GROUP_STATE,
                    mode = SessionMode.fromKey(formState.mode),
                    selectedIds = formState.moods,
                    onSelectionChange = {
                        onFormStateChange(formState.copy(moods = it))
                    }
                )
            }
        }
    }
}

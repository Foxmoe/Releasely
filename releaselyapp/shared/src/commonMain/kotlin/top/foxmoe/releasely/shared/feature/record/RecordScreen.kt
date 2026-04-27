package top.foxmoe.releasely.shared.feature.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyButton
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyCard
import top.foxmoe.releasely.shared.domain.model.RecordType

@Composable
fun RecordScreen(
    selectedType: RecordType,
    protectionEnabled: Boolean,
    pleasureLevel: Float,
    onTypeSelected: (RecordType) -> Unit,
    onProtectionChanged: (Boolean) -> Unit,
    onPleasureChanged: (Float) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReleaselyCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "记录", style = MaterialTheme.typography.bodyLarge)

            Text(text = "行为类型", style = MaterialTheme.typography.bodyMedium)
            RecordTypeSegmentedButtons(
                selected = selectedType,
                onSelect = onTypeSelected
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "保护措施", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = protectionEnabled,
                    onCheckedChange = onProtectionChanged
                )
            }

            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "愉悦度 ${pleasureLevel.toInt()}/10",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = pleasureLevel,
                    onValueChange = onPleasureChanged,
                    valueRange = 0f..10f,
                    steps = 9
                )
            }

            ReleaselyButton(
                text = "保存记录",
                onClick = onSaveClick
            )
        }
    }
}

@Composable
private fun RecordTypeSegmentedButtons(
    selected: RecordType,
    onSelect: (RecordType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RecordType.entries.forEach { type ->
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { onSelect(type) }
            ) {
                val shortName = when (type) {
                    RecordType.WORKOUT -> "运动"
                    RecordType.RELAX -> "放松"
                    RecordType.SOCIAL -> "社交"
                    RecordType.SLEEP -> "睡眠"
                    RecordType.OTHER -> "其他"
                }
                val marker = if (selected == type) "•" else ""
                Text(text = "$shortName$marker")
            }
        }
    }
}

package top.foxmoe.releasely.shared.feature.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyButton
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyCard
import top.foxmoe.releasely.shared.domain.model.BiologicalSex
import top.foxmoe.releasely.shared.domain.model.RecordType

@Composable
fun RecordScreen(
    biologicalSex: BiologicalSex,
    selectedSexType: RecordType,
    protectionEnabled: Boolean,
    pleasureLevel: Float,
    periodFlowLevel: Float,
    onSexTypeSelected: (RecordType) -> Unit,
    onProtectionChanged: (Boolean) -> Unit,
    onPleasureChanged: (Float) -> Unit,
    onPeriodFlowChanged: (Float) -> Unit,
    onSaveSexClick: () -> Unit,
    onSavePeriodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(text = "性生活记录", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "记录每一次体验，建立属于你的长期趋势。",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(text = "类型", style = MaterialTheme.typography.bodyMedium)
                SexTypeSegmentedButtons(
                    selected = selectedSexType,
                    onSelect = onSexTypeSelected
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "有保护措施", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = protectionEnabled,
                        onCheckedChange = onProtectionChanged,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "体验评分 ${pleasureLevel.toInt()}/10",
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
                    text = "保存性生活",
                    onClick = onSaveSexClick
                )
            }
        }

        if (biologicalSex == BiologicalSex.FEMALE) {
            ReleaselyCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "月经记录", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "当天流量强度 ${periodFlowLevel.toInt()}/5",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = periodFlowLevel,
                        onValueChange = onPeriodFlowChanged,
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    ReleaselyButton(
                        text = "保存月经记录",
                        onClick = onSavePeriodClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SexTypeSegmentedButtons(
    selected: RecordType,
    onSelect: (RecordType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(RecordType.SEX_SOLO, RecordType.SEX_PARTNER).forEach { type ->
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { onSelect(type) }
            ) {
                val shortName = when (type) {
                    RecordType.SEX_SOLO -> "个人"
                    RecordType.SEX_PARTNER -> "伴侣"
                    RecordType.MENSTRUATION -> ""
                }
                val marker = if (selected == type) "•" else ""
                Text(text = "$shortName$marker")
            }
        }
    }
}

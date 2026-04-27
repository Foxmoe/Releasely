package top.foxmoe.releasely.shared.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.foxmoe.releasely.shared.designsystem.components.ReleaselyCard
import top.foxmoe.releasely.shared.domain.model.BiologicalSex

@Composable
fun SettingsScreen(
    biologicalSex: BiologicalSex,
    isAppLockEnabled: Boolean,
    isNotificationEnabled: Boolean,
    isDarkMode: Boolean,
    appVersion: String,
    onToggleAppLock: (Boolean) -> Unit,
    onDestroyData: () -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSexChanged: (BiologicalSex) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("档案类型", style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSexChanged(BiologicalSex.MALE) }
                    ) {
                        Text(if (biologicalSex == BiologicalSex.MALE) "男性 •" else "男性")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSexChanged(BiologicalSex.FEMALE) }
                    ) {
                        Text(if (biologicalSex == BiologicalSex.FEMALE) "女性 •" else "女性")
                    }
                }
            }
        }

        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("系统开关", style = MaterialTheme.typography.bodyLarge)
                SettingSwitchRow("通知管理", isNotificationEnabled, onValueChange = onToggleNotification)
                SettingSwitchRow("深色模式", isDarkMode, onValueChange = onToggleDarkMode)
                SettingSwitchRow("应用锁", isAppLockEnabled, onValueChange = onToggleAppLock)
            }
        }

        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("数据与备份", style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onBackup
                    ) { Text("备份") }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onRestore
                    ) { Text("恢复") }
                }

                Button(
                    onClick = onDestroyData,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text("销毁全部本地数据")
                }
            }
        }

        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("关于", style = MaterialTheme.typography.bodyLarge)
                Text("版本号：$appVersion", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = onPrivacyPolicyClick
                ) {
                    Text("隐私协议")
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onValueChange)
    }
}



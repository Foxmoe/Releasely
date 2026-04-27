package top.foxmoe.releasely.shared.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun SettingsScreen(
    isCamouflageMode: Boolean,
    isAppLockEnabled: Boolean,
    isNotificationEnabled: Boolean,
    isDarkMode: Boolean,
    appVersion: String,
    onToggleCamouflage: () -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onDestroyData: () -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReleaselyCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("隐私安全", style = MaterialTheme.typography.bodyLarge)
                SettingSwitchRow("伪装模式", isCamouflageMode, onValueChange = { onToggleCamouflage() })
                SettingSwitchRow("应用锁", isAppLockEnabled, onValueChange = onToggleAppLock)

                Button(
                    onClick = onDestroyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
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
                Text("通用设置", style = MaterialTheme.typography.bodyLarge)
                SettingSwitchRow("通知管理", isNotificationEnabled, onValueChange = onToggleNotification)
                SettingSwitchRow("深色模式", isDarkMode, onValueChange = onToggleDarkMode)
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onValueChange)
    }
}



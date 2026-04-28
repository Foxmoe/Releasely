package top.foxmoe.releasely.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("privacy", Context.MODE_PRIVATE) }

    var biometricUnlock by remember { mutableStateOf(prefs.getBoolean("biometric_unlock", false)) }
    var screenshotProtection by remember { mutableStateOf(prefs.getBoolean("screenshot_protection", true)) }
    var cloudSync by remember { mutableStateOf(prefs.getBoolean("cloud_sync", true)) }

    fun save() {
        prefs.edit()
            .putBoolean("biometric_unlock", biometricUnlock)
            .putBoolean("screenshot_protection", screenshotProtection)
            .putBoolean("cloud_sync", cloudSync)
            .apply()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(text = "隐私设置", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("隐私保护", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        PrivacySwitchItem(
            title = "生物识别解锁",
            subtitle = "使用指纹/面容解锁应用",
            checked = biometricUnlock,
            onCheckedChange = {
                biometricUnlock = it
                save()
            }
        )

        PrivacySwitchItem(
            title = "截图保护",
            subtitle = "防止应用内截图和录屏",
            checked = screenshotProtection,
            onCheckedChange = {
                screenshotProtection = it
                save()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("数据同步", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        PrivacySwitchItem(
            title = "云端同步",
            subtitle = "将数据加密后同步到云端",
            checked = cloudSync,
            onCheckedChange = {
                cloudSync = it
                save()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // 清除所有本地数据
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
        ) {
            Text("清除所有本地数据")
        }
    }
}

@Composable
fun PrivacySwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

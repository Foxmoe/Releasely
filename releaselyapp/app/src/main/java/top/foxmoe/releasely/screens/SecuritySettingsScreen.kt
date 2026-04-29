package top.foxmoe.releasely.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest

@Composable
fun SecuritySettingsScreen(
    onBack: () -> Unit,
    onDecoyNavigate: () -> Unit
) {
    val context = LocalContext.current
    var appLockEnabled by remember { mutableStateOf(false) }
    var decoyEnabled by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("security", Context.MODE_PRIVATE)
        appLockEnabled = prefs.getBoolean("app_lock_enabled", false)
        decoyEnabled = prefs.getBoolean("decoy_enabled", false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "安全设置",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SecurityCard(
                    title = "应用锁",
                    subtitle = "启用 PIN 码保护",
                    icon = Icons.Filled.Lock,
                    color = Color(0xFFFF9800),
                    isEnabled = appLockEnabled,
                    onToggle = { enabled ->
                        if (enabled) {
                            showPinSetup = true
                        } else {
                            val prefs = context.getSharedPreferences("security", Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("app_lock_enabled", false).apply()
                            appLockEnabled = false
                        }
                    }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecurityCard(
                        title = "界面伪装",
                        subtitle = "伪装成计算器应用",
                        icon = Icons.Filled.Edit,
                        color = Color(0xFF9C27B0),
                        isEnabled = decoyEnabled,
                        onToggle = { enabled ->
                            val prefs = context.getSharedPreferences("security", Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("decoy_enabled", enabled).apply()
                            decoyEnabled = enabled
                        }
                    )
                    if (decoyEnabled) {
                        Button(
                            onClick = onDecoyNavigate,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("预览伪装界面")
                        }
                    }
                }
            }

            item {
                SecurityCard(
                    title = "一键销毁",
                    subtitle = "清除所有本地数据",
                    icon = Icons.Filled.Delete,
                    color = Color(0xFFF44336),
                    isEnabled = false,
                    onToggle = {}
                )
            }

            item {
                SecurityCard(
                    title = "导出数据",
                    subtitle = "导出加密的备份文件",
                    icon = Icons.Filled.Send,
                    color = Color(0xFF2196F3),
                    isEnabled = false,
                    onToggle = {}
                )
            }
        }
    }

    if (showPinSetup) {
        PinSetupDialog(
            onPinSet = { pin ->
                val prefs = context.getSharedPreferences("security", Context.MODE_PRIVATE)
                val hashedPin = hashPin(pin)
                prefs.edit()
                    .putString("pin_hash", hashedPin)
                    .putBoolean("app_lock_enabled", true)
                    .apply()
                appLockEnabled = true
                showPinSetup = false
            },
            onDismiss = { showPinSetup = false }
        )
    }
}

@Composable
fun SecurityCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun PinSetupDialog(
    onPinSet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 1) "设置 PIN" else "确认 PIN") },
        text = {
            OutlinedTextField(
                value = if (step == 1) pin else confirmPin,
                onValueChange = {
                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                        if (step == 1) pin = it else confirmPin = it
                    }
                },
                label = { Text(if (step == 1) "输入 4-6 位 PIN" else "再次输入 PIN") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                if (step == 1 && pin.length >= 4) {
                    step = 2
                } else if (step == 2) {
                    if (pin == confirmPin) {
                        onPinSet(pin)
                    }
                }
            }) {
                Text("下一步")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun hashPin(pin: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
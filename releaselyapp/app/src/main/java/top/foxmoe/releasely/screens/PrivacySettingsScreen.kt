package top.foxmoe.releasely.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject
import top.foxmoe.releasely.ReleaselyApp

data class PrivacyRequestRecord(
    val id: Long,
    val requestType: String,
    val status: String,
    val requestedAt: String,
    val details: String?
)

@Composable
fun PrivacySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("privacy", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("auth", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var biometricUnlock by remember { mutableStateOf(prefs.getBoolean("biometric_unlock", false)) }
    var screenshotProtection by remember { mutableStateOf(prefs.getBoolean("screenshot_protection", true)) }
    var cloudSync by remember { mutableStateOf(prefs.getBoolean("cloud_sync", true)) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    // Data subject rights
    var showExportDialog by remember { mutableStateOf(false) }
    var showRequestHistory by remember { mutableStateOf(false) }
    var requestHistory by remember { mutableStateOf<List<PrivacyRequestRecord>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(false) }
    var requestError by remember { mutableStateOf<String?>(null) }

    fun save() {
        prefs.edit()
            .putBoolean("biometric_unlock", biometricUnlock)
            .putBoolean("screenshot_protection", screenshotProtection)
            .putBoolean("cloud_sync", cloudSync)
            .apply()
    }

    fun loadRequestHistory() {
        val token = authPrefs.getString("token", null) ?: return
        isLoadingHistory = true
        requestError = null
        scope.launch {
            val app = context.applicationContext as ReleaselyApp
            try {
                val response = app.apiService.get("/privacy/request/status", token)
                response.getOrNull()?.let { jsonString ->
                    val obj = JSONObject(jsonString)
                    val data = obj.optJSONObject("data")
                    val requestsArray = data?.optJSONArray("requests")
                    val records = mutableListOf<PrivacyRequestRecord>()
                    requestsArray?.let { array ->
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            records.add(
                                PrivacyRequestRecord(
                                    id = item.optLong("id", 0),
                                    requestType = item.optString("requestType", ""),
                                    status = item.optString("status", ""),
                                    requestedAt = item.optString("requestedAt", ""),
                                    details = item.optString("details", null)
                                )
                            )
                        }
                    }
                    requestHistory = records
                }
            } catch (e: Exception) {
                requestError = "加载失败: ${e.message}"
            } finally {
                isLoadingHistory = false
            }
        }
    }

    fun submitDataRequest(requestType: String, details: String?) {
        val token = authPrefs.getString("token", null)
        if (token == null) {
            requestError = "未登录或登录已过期"
            return
        }
        scope.launch {
            val app = context.applicationContext as ReleaselyApp
            try {
                val json = JSONObject().apply {
                    put("requestType", requestType)
                    details?.let { put("details", it) }
                }
                val response = app.apiService.post("/privacy/request", json.toString(), token)
                response.getOrNull()?.let { jsonString ->
                    val obj = JSONObject(jsonString)
                    if (obj.optInt("code", 500) == 200) {
                        requestError = null
                        loadRequestHistory()
                    } else {
                        requestError = obj.optString("message", "提交失败")
                    }
                }
            } catch (e: Exception) {
                requestError = "提交失败: ${e.message}"
            }
        }
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
                (context as? top.foxmoe.releasely.MainActivity)?.updateScreenshotProtection()
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

        Text("数据主体权利 (GDPR/个人信息保护法)", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        // Data export button
        Button(
            onClick = { showExportDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Icon(
                imageVector = Icons.Filled.CloudDownload,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("申请数据导出")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // View request history button
        OutlinedButton(
            onClick = {
                showRequestHistory = true
                loadRequestHistory()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("查看数据请求记录", color = Color(0xFF1976D2))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("危险区域", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showClearConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
        ) {
            Text("清除所有本地数据")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showDeleteAccountConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("注销账户")
        }

        if (isDeleting) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFB71C1C))
            }
        }

        deleteError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = Color(0xFFF44336),
                fontSize = 12.sp
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("确认清除") },
            text = { Text("此操作将删除所有本地记录、设置和用户资料，且无法恢复。确定继续吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()
                        context.getSharedPreferences("privacy", Context.MODE_PRIVATE).edit().clear().apply()
                        context.getSharedPreferences("notification", Context.MODE_PRIVATE).edit().clear().apply()
                        context.deleteDatabase("releasely.db")
                        (context as? android.app.Activity)?.finishAffinity()
                    }
                ) {
                    Text("清除", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteAccountConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFB71C1C),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("注销账户", color = Color(0xFFB71C1C)) },
            text = {
                Text(
                    text = "此操作不可撤销。所有云端和本地数据将被永久删除。\n\n您的账户、周期记录、activity记录、健康报告、用药记录、伴侣关系等信息都将被删除，且无法恢复。\n\n确定要继续吗？",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        deleteError = null
                        val token = authPrefs.getString("token", null)
                        if (token == null) {
                            deleteError = "未登录或登录已过期"
                            isDeleting = false
                            return@TextButton
                        }
                        scope.launch {
                            val app = context.applicationContext as ReleaselyApp
                            val result = app.authService.deleteAccount(token)
                            isDeleting = false
                            if (result.success) {
                                // Clear all local data
                                authPrefs.edit().clear().apply()
                                prefs.edit().clear().apply()
                                context.getSharedPreferences("notification", Context.MODE_PRIVATE).edit().clear().apply()
                                context.deleteSharedPreferences("api_settings")
                                context.deleteDatabase("releasely.db")
                                showDeleteAccountConfirm = false
                                // Finish all activities to return to login screen
                                (context as? android.app.Activity)?.finishAffinity()
                            } else {
                                deleteError = result.error ?: "注销失败"
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    Text("确认注销", color = Color(0xFFB71C1C))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountConfirm = false },
                    enabled = !isDeleting
                ) {
                    Text("取消")
                }
            }
        )
    }

    if (showExportDialog) {
        var details by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("申请数据导出") },
            text = {
                Column {
                    Text(
                        text = "根据 GDPR 和个人信息保护法，您有权获取您的个人数据副本。我们将在 30 天内处理您的请求，并通过安全方式提供数据下载链接。",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("补充说明（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        submitDataRequest("EXPORT", details.ifBlank { null })
                        showExportDialog = false
                    }
                ) {
                    Text("提交申请", color = Color(0xFF1976D2))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showRequestHistory) {
        AlertDialog(
            onDismissRequest = { showRequestHistory = false },
            title = { Text("数据请求记录") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    requestError?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (isLoadingHistory) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (requestHistory.isEmpty()) {
                        Text(
                            text = "暂无数据请求记录",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(requestHistory) { record ->
                                RequestHistoryItem(record = record)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRequestHistory = false }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun RequestHistoryItem(record: PrivacyRequestRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (record.requestType) {
                        "EXPORT" -> "数据导出"
                        "DELETE" -> "账户注销"
                        "RECTIFY" -> "数据更正"
                        else -> record.requestType
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                StatusChip(status = record.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.requestedAt,
                fontSize = 12.sp,
                color = Color.Gray
            )
            record.details?.let { details ->
                if (details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = details,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "PENDING" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "APPROVED" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "REJECTED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "COMPLETED" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "CANCELLED" -> Color(0xFFECEFF1) to Color(0xFF546E7A)
        else -> Color(0xFFECEFF1) to Color(0xFF546E7A)
    }
    val label = when (status) {
        "PENDING" -> "待处理"
        "APPROVED" -> "已批准"
        "REJECTED" -> "已拒绝"
        "COMPLETED" -> "已完成"
        "CANCELLED" -> "已取消"
        else -> status
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
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
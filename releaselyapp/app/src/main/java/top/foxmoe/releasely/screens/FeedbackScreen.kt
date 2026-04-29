package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val scope = rememberCoroutineScope()

    var feedbackType by remember { mutableStateOf("建议") }
    var content by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val types = listOf("建议", "Bug 反馈", "功能需求", "其他")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "匿名反馈",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (submitted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("反馈已提交", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("感谢你的建议，我们会认真考虑", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                    Text("返回")
                }
            }
            return
        }

        Text("反馈类型", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { type ->
                FilterChip(
                    selected = feedbackType == type,
                    onClick = { feedbackType = type },
                    label = { Text(type, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it; error = null },
            label = { Text("反馈内容") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            shape = RoundedCornerShape(12.dp),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text("联系方式（可选）") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (content.isBlank()) {
                    error = "请输入反馈内容"
                    return@Button
                }
                loading = true
                error = null
                scope.launch {
                    val json = JSONObject().apply {
                        put("type", feedbackType)
                        put("content", content)
                        put("contact", contact.takeIf { it.isNotBlank() })
                    }
                    val result = app.apiService.post("/feedback", json.toString())
                    loading = false
                    result.getOrNull()?.let { response ->
                        val obj = JSONObject(response)
                        if (obj.optInt("code", 500) == 200) {
                            submitted = true
                        } else {
                            error = obj.optString("message", "提交失败")
                        }
                    } ?: run {
                        error = "网络错误，请稍后重试"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text("提交反馈", fontSize = 16.sp)
            }
        }
    }
}

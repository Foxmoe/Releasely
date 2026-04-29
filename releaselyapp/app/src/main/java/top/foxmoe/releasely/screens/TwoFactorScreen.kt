package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.foxmoe.releasely.ReleaselyApp

@Composable
fun TwoFactorScreen(
    preAuthToken: String,
    username: String,
    onBack: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "双重验证",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "请输入身份验证器应用中的 6 位验证码",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                    code = it
                    error = null
                }
            },
            label = { Text("验证码") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(12.dp)
        )

        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (code.length != 6) {
                    error = "请输入 6 位验证码"
                    return@Button
                }
                loading = true
                error = null
                scope.launch {
                    val authService = top.foxmoe.releasely.services.AuthService(app.apiService)
                    val result = authService.verify2FA(preAuthToken, code)
                    loading = false
                    if (result.success) {
                        result.token?.let { token ->
                            app.apiService.setAuthToken(token)
                            app.syncService.setAuthToken(token)
                            app.securitySettingsService.setAuthToken(token)
                        }
                        context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putString("token", result.token)
                            .putString("username", username)
                            .apply()
                        onVerifySuccess()
                    } else {
                        error = result.error ?: "验证失败"
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
                Text("验证", fontSize = 16.sp)
            }
        }
    }
}

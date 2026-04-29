package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "隐私政策",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            item {
                PrivacySection(
                    title = "1. 数据收集",
                    content = """
                        Releasely（起飞了么）是一款注重隐私的性健康管理应用。我们采取"数据最小化"原则：

                        • 服务端仅存储用户名、邮箱等账户基本信息
                        • 敏感的性行为记录、健康数据采用端到端加密后存储
                        • 我们不会向第三方出售或共享您的任何个人数据
                        • 匿名反馈功能不会收集任何可识别身份的信息
                    """.trimIndent()
                )
                PrivacySection(
                    title = "2. 本地数据存储",
                    content = """
                        您的所有敏感数据默认仅存储在本地设备中：

                        • 使用 SQLDelight + SQLite 进行本地数据持久化
                        • 敏感字段（如行为备注）可选择 AES-256-GCM 加密
                        • 应用锁和生物识别功能保护本地数据访问
                        • 截图保护功能防止数据被意外泄露
                    """.trimIndent()
                )
                PrivacySection(
                    title = "3. 云端同步",
                    content = """
                        当您启用云端同步功能时：

                        • 数据在传输前进行加密处理
                        • 使用 HTTPS/TLS 1.3 保护传输安全
                        • 服务端存储的数据同样经过加密处理
                        • 您可随时关闭同步功能，数据将保留在本地
                    """.trimIndent()
                )
                PrivacySection(
                    title = "4. 数据权利",
                    content = """
                        根据 GDPR 和个人信息保护法，您享有以下权利：

                        • 访问权：随时查看您的个人数据
                        • 更正权：修改不准确的个人信息
                        • 删除权：通过"清除所有本地数据"功能彻底删除数据
                        • 导出权：使用数据导出功能获取您的数据副本
                        • 撤回同意权：随时关闭同步或注销账户
                    """.trimIndent()
                )
                PrivacySection(
                    title = "5. 安全措施",
                    content = """
                        我们采取多重措施保护您的数据安全：

                        • 应用启动锁（PIN / 指纹 / 面容识别）
                        • 界面伪装模式（伪装成计算器应用）
                        • 截图/录屏保护（FLAG_SECURE）
                        • 双重身份验证（2FA TOTP）
                        • 定期安全审计和依赖更新
                    """.trimIndent()
                )
                PrivacySection(
                    title = "6. 联系我们",
                    content = """
                        如果您对隐私政策有任何疑问，可以通过以下方式联系我们：

                        • GitHub Issues: https://github.com/Foxmoe/Releasely/issues
                        • 应用内匿名反馈功能

                        本隐私政策最后更新日期：2026年4月30日
                    """.trimIndent()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFF666666)
        )
    }
}

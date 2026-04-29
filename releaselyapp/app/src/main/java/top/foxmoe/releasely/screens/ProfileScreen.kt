package top.foxmoe.releasely.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.foxmoe.releasely.ReleaselyApp

/**
 * 个人中心页：展示用户资料、菜单列表和子页面导航
 */
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE) }
    var isLoggedIn by remember { mutableStateOf(authPrefs.getString("token", null) != null) }

    var showPartnerScreen by remember { mutableStateOf(false) }
    var showSecurityScreen by remember { mutableStateOf(false) }
    var showPersonalProfile by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showPrivacySettings by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showFeedbackScreen by remember { mutableStateOf(false) }
    var showLoginScreen by remember { mutableStateOf(false) }
    var showRegisterScreen by remember { mutableStateOf(false) }
    var showTwoFactorScreen by remember { mutableStateOf(false) }
    var twoFactorToken by remember { mutableStateOf("") }
    var twoFactorUsername by remember { mutableStateOf("") }

    // 子页面导航栈处理
    when {
        showPartnerScreen -> {
            val partners = remember { mutableStateOf<List<PartnerDisplayItem>>(emptyList()) }
            LaunchedEffect(Unit) {
                val app = context.applicationContext as ReleaselyApp
                partners.value = app.partnerService.getAllPartners().map {
                    PartnerDisplayItem(it.id, it.name, it.inviteCode, it.status)
                }
            }
            PartnerScreen(
                partners = partners.value,
                onAddPartner = { name ->
                    GlobalScope.launch(Dispatchers.IO) {
                        val app = context.applicationContext as ReleaselyApp
                        app.partnerService.insertPartner(name)
                    }
                },
                onInvitePartner = { /* TODO: 处理伴侣邀请码 */ },
                onBack = { showPartnerScreen = false }
            )
            return
        }

        showSecurityScreen -> {
            SecuritySettingsScreen(
                onBack = { showSecurityScreen = false },
                onDecoyNavigate = { /* TODO: 伪装模式导航 */ }
            )
            return
        }

        showPersonalProfile -> {
            PersonalProfileScreen(onBack = { showPersonalProfile = false })
            return
        }

        showNotificationSettings -> {
            NotificationSettingsScreen(onBack = { showNotificationSettings = false })
            return
        }

        showPrivacySettings -> {
            PrivacySettingsScreen(onBack = { showPrivacySettings = false })
            return
        }

        showAbout -> {
            AboutScreen(
                onBack = { showAbout = false },
                onPrivacyPolicy = {
                    showAbout = false
                    showPrivacyPolicy = true
                }
            )
            return
        }

        showPrivacyPolicy -> {
            PrivacyPolicyScreen(onBack = { showPrivacyPolicy = false })
            return
        }

        showFeedbackScreen -> {
            FeedbackScreen(onBack = { showFeedbackScreen = false })
            return
        }

        showLoginScreen -> {
            LoginScreen(
                onBack = { showLoginScreen = false },
                onLoginSuccess = {
                    isLoggedIn = true
                    showLoginScreen = false
                },
                onNeed2FA = { token, user ->
                    twoFactorToken = token
                    twoFactorUsername = user
                    showLoginScreen = false
                    showTwoFactorScreen = true
                },
                onRegisterClick = {
                    showLoginScreen = false
                    showRegisterScreen = true
                }
            )
            return
        }

        showRegisterScreen -> {
            RegisterScreen(
                onBack = {
                    showRegisterScreen = false
                    showLoginScreen = true
                },
                onRegisterSuccess = {
                    showRegisterScreen = false
                    showLoginScreen = true
                }
            )
            return
        }

        showTwoFactorScreen -> {
            TwoFactorScreen(
                preAuthToken = twoFactorToken,
                username = twoFactorUsername,
                onBack = {
                    showTwoFactorScreen = false
                    showLoginScreen = true
                },
                onVerifySuccess = {
                    isLoggedIn = true
                    showTwoFactorScreen = false
                }
            )
            return
        }
    }

    val menuItems = remember {
        listOf(
            MenuItem("个人资料", Icons.Filled.Person, Color(0xFF2196F3)),
            MenuItem("伴侣管理", Icons.Filled.Favorite, Color(0xFF9C27B0)),
            MenuItem("安全设置", Icons.Filled.Lock, Color(0xFFFF9800)),
            MenuItem("通知设置", Icons.Filled.Notifications, Color(0xFFE91E63)),
            MenuItem("隐私设置", Icons.Filled.Edit, Color(0xFF607D8B)),
            MenuItem("匿名反馈", Icons.Filled.MailOutline, Color(0xFF00BCD4)),
            MenuItem("关于", Icons.Filled.Info, Color(0xFF9E9E9E)),
            MenuItem("退出登录", Icons.Filled.ExitToApp, Color(0xFFF44336))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 用户资料头部
        item {
            ProfileHeader(isLoggedIn = isLoggedIn, onLoginClick = { showLoginScreen = true })
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 菜单列表
        items(menuItems.size) { index ->
            val menuItem = menuItems[index]
            MenuListItem(
                menuItem = menuItem,
                onClick = {
                    when (menuItem.title) {
                        "个人资料" -> showPersonalProfile = true
                        "伴侣管理" -> showPartnerScreen = true
                        "安全设置" -> showSecurityScreen = true
                        "通知设置" -> showNotificationSettings = true
                        "隐私设置" -> showPrivacySettings = true
                        "关于" -> showAbout = true
                        "匿名反馈" -> showFeedbackScreen = true
                        "退出登录" -> {
                            if (isLoggedIn) {
                                authPrefs.edit().remove("token").remove("username").remove("userId").apply()
                                val app = context.applicationContext as ReleaselyApp
                                app.apiService.setAuthToken(null)
                                app.syncService.setAuthToken(null)
                                app.securitySettingsService.setAuthToken(null)
                                isLoggedIn = false
                            } else {
                                showLoginScreen = true
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * 用户资料头部，展示头像和真实用户信息
 */
@Composable
fun ProfileHeader(isLoggedIn: Boolean, onLoginClick: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    var userName by remember { mutableStateOf("用户") }
    var recordDays by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val profile = app.getActiveProfile()
        userName = profile?.name ?: "用户"
        recordDays = app.activityService.getActivityCount().toInt()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFBBDEFB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).ifEmpty { "用" },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isLoggedIn) userName else "未登录",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "已记录 $recordDays 天",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        if (!isLoggedIn) {
            androidx.compose.material3.Button(
                onClick = onLoginClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("登录", fontSize = 14.sp)
            }
        }
    }
}

/**
 * 菜单项数据类
 */
data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * 伴侣展示用数据类（前端聚合数据）
 */
data class PartnerDisplayItem(
    val id: String,
    val name: String,
    val inviteCode: String?,
    val status: String
)

/**
 * 菜单列表项组件，带图标、标题和右箭头
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuListItem(
    menuItem: MenuItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        onClick = onClick
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
                    imageVector = menuItem.icon,
                    contentDescription = null,
                    tint = menuItem.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = menuItem.title,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

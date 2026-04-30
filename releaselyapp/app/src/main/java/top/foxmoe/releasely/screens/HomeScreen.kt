package top.foxmoe.releasely.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.foxmoe.releasely.ReleaselyApp

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val isFemale = remember { app.isFemale() }

    var activityCount by remember { mutableIntStateOf(0) }
    var cycleDay by remember { mutableIntStateOf(0) }
    var cyclePhase by remember { mutableStateOf("") }
    var protectionRate by remember { mutableStateOf("0%") }
    var userName by remember { mutableStateOf("用户") }
    var dataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        activityCount = app.activityService.getActivityCount().toInt()
        val profile = app.getActiveProfile()
        userName = profile?.name ?: "用户"

        if (isFemale) {
            val latestCycle = app.cycleService.getLatestCycle()
            if (latestCycle != null) {
                val now = System.currentTimeMillis() / 1000
                val daysSinceStart = ((now - latestCycle.startDate) / (24 * 60 * 60)).toInt()
                cycleDay = daysSinceStart
                cyclePhase = when {
                    daysSinceStart < 5 -> "月经期"
                    daysSinceStart < 14 -> "卵泡期"
                    daysSinceStart < 21 -> "排卵期"
                    else -> "黄体期"
                }
            }
        }

        val now = System.currentTimeMillis() / 1000
        val startOfMonth = now - (now % (30 * 24 * 60 * 60))
        val monthActivities = app.activityService.getActivitiesByDateRange(startOfMonth, now)
        if (monthActivities.isNotEmpty()) {
            val protectedCount = monthActivities.count { it.protection }
            val rate = (protectedCount * 100 / monthActivities.size)
            protectionRate = "$rate%"
        }
        dataLoaded = true
    }

    val greeting = remember {
        val hour = java.time.LocalTime.now().hour
        when {
            hour < 6 -> "夜深了"
            hour < 12 -> "早上好"
            hour < 14 -> "中午好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    val greetingEmoji = remember {
        val hour = java.time.LocalTime.now().hour
        when {
            hour < 6 -> "🌙"
            hour < 12 -> "☀️"
            hour < 14 -> "🌤"
            hour < 18 -> "🌅"
            else -> "🌆"
        }
    }

    // 入场动画
    val scaleAnim = remember { Animatable(0.95f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp)
    ) {
        // 问候语区域 - 带渐变背景
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scaleAnim.value),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF9C27B0)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "$greeting $greetingEmoji",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "已记录 $activityCount 天",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // 今日概览标题
        item {
            Text(
                text = "今日概览",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 统计卡片第一行
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isFemale) {
                    AnimatedStatCard(
                        title = "周期天数",
                        value = if (cycleDay > 0) "Day $cycleDay" else "-",
                        subtitle = cyclePhase.ifEmpty { "未记录" },
                        icon = Icons.Filled.Favorite,
                        color = Color(0xFFE91E63),
                        bgColor = Color(0xFFFCE4EC),
                        modifier = Modifier.weight(1f),
                        delay = 100
                    )
                } else {
                    AnimatedStatCard(
                        title = "健康指数",
                        value = if (activityCount > 0) "良好" else "-",
                        subtitle = "基于记录",
                        icon = Icons.Filled.ThumbUp,
                        color = Color(0xFF2196F3),
                        bgColor = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f),
                        delay = 100
                    )
                }
                AnimatedStatCard(
                    title = "亲密次数",
                    value = activityCount.toString(),
                    subtitle = "累计",
                    icon = Icons.Filled.FavoriteBorder,
                    color = Color(0xFF9C27B0),
                    bgColor = Color(0xFFF3E5F5),
                    modifier = Modifier.weight(1f),
                    delay = 200
                )
            }
        }

        // 统计卡片第二行
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedStatCard(
                    title = "保护率",
                    value = protectionRate,
                    subtitle = "本月",
                    icon = Icons.Filled.Lock,
                    color = Color(0xFF4CAF50),
                    bgColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f),
                    delay = 300
                )
                AnimatedStatCard(
                    title = "心情指数",
                    value = "😊",
                    subtitle = "今日",
                    icon = Icons.Filled.Star,
                    color = Color(0xFFFF9800),
                    bgColor = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f),
                    delay = 400
                )
            }
        }

        // 快捷操作
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "快捷操作",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.Add,
                    label = "新记录",
                    color = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Filled.Info,
                    label = "报告",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Filled.Search,
                    label = "知识",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AnimatedStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    delay: Int = 0
) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )
    }

    Card(
        modifier = modifier
            .scale(scale.value)
            .graphicsLayer(alpha = alpha.value),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

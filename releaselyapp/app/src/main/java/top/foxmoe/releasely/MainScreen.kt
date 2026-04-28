package top.foxmoe.releasely

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import top.foxmoe.releasely.screens.DataScreen
import top.foxmoe.releasely.screens.HomeScreen
import top.foxmoe.releasely.screens.ProfileScreen
import top.foxmoe.releasely.screens.ToolsScreen

/**
 * 主屏幕：底部导航容器，管理 4 个一级页面的切换
 *
 * 页面结构：
 * - 首页 (HomeScreen)：今日概览、统计卡片
 * - 数据 (DataScreen)：行为/周期/健康记录
 * - 工具 (ToolsScreen)：各类健康计算工具入口
 * - 我的 (ProfileScreen)：个人资料与设置
 */
@Composable
fun MainScreen() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("首页", "数据", "工具", "我的")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Filled.DateRange,
        Icons.Filled.Build,
        Icons.Filled.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> DataScreen()
                2 -> ToolsScreen()
                3 -> ProfileScreen()
            }
        }
    }
}

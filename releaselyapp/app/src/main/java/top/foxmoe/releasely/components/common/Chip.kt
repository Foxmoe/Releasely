package top.foxmoe.releasely.components.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 小型标签组件，用于展示状态/分类信息（如"有保护"、"未服用"等）
 */
@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE0E0E0)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

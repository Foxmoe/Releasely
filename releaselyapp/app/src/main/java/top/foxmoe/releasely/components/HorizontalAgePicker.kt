package top.foxmoe.releasely.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalAgePicker(
    initialAge: Int,
    onAgeSelected: (Int) -> Unit,
    themeColor: Color
) {

    val startAge = 17
    val endAge = 150
    val count = endAge - startAge + 1
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialAge - startAge)
    
    // 增加宽度以容纳3位数年龄
    val itemWidth = 80.dp
    val density = LocalDensity.current
    val itemWidthPx = with(density) { itemWidth.toPx() }
    
    // 计算中间位置的 padding
    var rowWidthPx by remember { mutableFloatStateOf(0f) }
    val contentPadding = with(density) { (rowWidthPx / 2 - itemWidthPx / 2).toDp() }

    // 监听滚动停止，吸附居中并更新选中值
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val centerIndex = listState.firstVisibleItemIndex
        val currentAge = startAge + centerIndex
        if (currentAge != initialAge) {
             onAgeSelected(currentAge)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().onSizeChanged { rowWidthPx = it.width.toFloat() }) {
        if (rowWidthPx > 0) {
            LazyRow(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(horizontal = maxOf(0.dp, contentPadding)),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(count) { index ->
                    val age = startAge + index
                    val isSelected = age == initialAge
                    
                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = age.toString(),
                            style = TextStyle(
                                fontSize = if (isSelected) 36.sp else 24.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.Gray.copy(alpha = 0.5f)
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                        
                        if (isSelected) {
                            // 下划线指示器
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .width(20.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(themeColor)
                            )
                        }
                    }
                }
            }
        }
    }
}


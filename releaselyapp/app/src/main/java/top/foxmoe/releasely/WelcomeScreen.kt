package top.foxmoe.releasely

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.foxmoe.releasely.components.GenderDropdown
import top.foxmoe.releasely.components.HorizontalAgePicker
import top.foxmoe.releasely.components.PartnerSelectionCard

// 定义性别枚举与对应的颜色
enum class Gender(val label: String, val color: Color) {
    Mr("先生", Color(0xFFBBDEFB)), // 淡蓝色
    Ms("女士", Color(0xFFFFCDD2)), // 淡红色
    Unknown("未知", Color(0xFFC8E6C9)) // 淡绿色
}

enum class SetupStep {
    Profile,
    Age,
    Partner
}

@Composable
fun WelcomeScreen() {
    var step by remember { mutableStateOf(SetupStep.Profile) }
    
    // Step 1: Profile
    var nickname by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.Mr) }
    
    // Step 2: Age
    var age by remember { mutableIntStateOf(25) }
    
    // Step 3: Partner
    var hasPartner by remember { mutableStateOf<Boolean?>(null) } // null: 未选择, true: 有, false: 无

    var isError by remember { mutableStateOf(false) }

    // 背景颜色动画
    val themeColor by animateColorAsState(
        targetValue = selectedGender.color,
        animationSpec = tween(durationMillis = 500),
        label = "ThemeColorAnimation"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // 上方半屏 (调整高度比例，使其向上移动)
        Box(
            modifier = Modifier
                .weight(0.35f) // 也就是占 35%
                .fillMaxWidth()
                .background(themeColor)
        )

        // 下方半屏
        Column(
            modifier = Modifier
                .weight(0.65f) // 也就是占 65%
                .fillMaxWidth(),
            // Remove padding here to allow full-width animation
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        slideInHorizontally { width -> direction * width } togetherWith
                        slideOutHorizontally { width -> -direction * width }
                    },
                    label = "StepAnimation"
                ) { targetStep ->
                    // Apply padding inside the individual steps
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 32.dp)) {
                        when (targetStep) {
                            SetupStep.Profile -> {
                                ProfileStepContent(
                                    nickname = nickname,
                                    onNicknameChange = {
                                        nickname = it
                                        if (isError) isError = false
                                    },
                                    selectedGender = selectedGender,
                                    onGenderChange = { selectedGender = it },
                                    isError = isError,
                                    themeColor = themeColor
                                )
                            }
                            SetupStep.Age -> {
                                AgeStepContent(
                                    age = age,
                                    onAgeSelected = { age = it },
                                    themeColor = themeColor
                                )
                            }
                            SetupStep.Partner -> {
                                PartnerStepContent(
                                    hasPartner = hasPartner,
                                    onPartnerSelect = {
                                        hasPartner = it
                                        if (isError) isError = false
                                    },
                                    isError = isError,
                                    themeColor = themeColor
                                )
                            }
                        }
                    }
                }
            }
            
            // 底部按钮区域 (Static, outside animation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                when (step) {
                    SetupStep.Profile -> {
                        Button(
                            onClick = {
                                if (nickname.isBlank()) {
                                    isError = true
                                } else {
                                    isError = false
                                    step = SetupStep.Age
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text("继续")
                        }
                    }
                    SetupStep.Age -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { step = SetupStep.Profile }) {
                                Text("返回", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { step = SetupStep.Partner },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("下一步")
                            }
                        }
                    }
                    SetupStep.Partner -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { step = SetupStep.Age }) {
                                Text("返回", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (hasPartner == null) {
                                        isError = true
                                    } else {
                                        // TODO: 处理完成逻辑
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("完成")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStepContent(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    selectedGender: Gender,
    onGenderChange: (Gender) -> Unit,
    isError: Boolean,
    themeColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 文字部分 - Windows 10 Metro 风格 (左对齐，简洁)
        Text(
            text = "嗨！",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "看起来是首次使用Releasely",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "怎么称呼您？",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 输入行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 昵称输入框
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColor) // 输入框背景跟随主题色
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 性别下拉栏
            GenderDropdown(
                selectedGender = selectedGender,
                onGenderSelected = onGenderChange
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 提示文字
        Text(
            text = if (isError) "昵称不能为空哦！" else "您可以随时更改昵称或切换档案。",
            style = TextStyle(
                fontSize = 12.sp,
                color = if (isError) Color.Red else Color.Gray
            )
        )
    }
}

@Composable
fun AgeStepContent(
    age: Int,
    onAgeSelected: (Int) -> Unit,
    themeColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "基本情况",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "您今年多大年龄？",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "我们将根据年龄为您制定计划",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(64.dp))

        // 横向年龄选择器
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalAgePicker(
                initialAge = age,
                onAgeSelected = onAgeSelected,
                themeColor = themeColor
            )
        }
    }
}

@Composable
fun PartnerStepContent(
    hasPartner: Boolean?,
    onPartnerSelect: (Boolean) -> Unit,
    isError: Boolean,
    themeColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "情感状态",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "最后确认一下",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        // 两个大卡片选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PartnerSelectionCard(
                text = "单身",
                isSelected = hasPartner == false,
                onClick = { onPartnerSelect(false) },
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
            
            PartnerSelectionCard(
                text = "有伴侣",
                isSelected = hasPartner == true,
                onClick = { onPartnerSelect(true) },
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isError) "请选择一项以完成设置" else "不同的状态会有不同的健康建议",
            style = TextStyle(
                fontSize = 12.sp,
                color = if (isError) Color.Red else Color.Gray
            )
        )
    }
}

package top.foxmoe.releasely.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ActivityForm(
    onSubmit: (date: Long, type: String, protection: Boolean, pleasure: Int?, mood: String?, notes: String?) -> Unit,
    onCancel: () -> Unit,
    initialDate: Long = System.currentTimeMillis() / 1000
) {
    var selectedDate by remember { mutableLongStateOf(initialDate) }
    var selectedType by remember { mutableStateOf("亲密行为") }
    var protection by remember { mutableStateOf(false) }
    var pleasure by remember { mutableIntStateOf(3) }
    var selectedMood by remember { mutableStateOf("愉悦") }
    var notes by remember { mutableStateOf("") }

    val types = listOf(
        "亲密行为", "自慰", "口交", "肛交", "边缘行为",
        "月经开始", "月经结束", "排卵日", "身体不适", "其他"
    )
    val moods = listOf("愉悦", "放松", "一般", "疲惫", "不适")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "添加记录",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Text("类型", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type, fontSize = 12.sp) }
                )
            }
        }

        Text("保护措施", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = protection,
                onClick = { protection = true },
                label = { Text("有保护") }
            )
            FilterChip(
                selected = !protection,
                onClick = { protection = false },
                label = { Text("无保护") }
            )
        }

        Text("愉悦度: $pleasure", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = pleasure.toFloat(),
            onValueChange = { pleasure = it.toInt() },
            valueRange = 1f..5f,
            steps = 3
        )

        Text("心情", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            moods.forEach { mood ->
                FilterChip(
                    selected = selectedMood == mood,
                    onClick = { selectedMood = mood },
                    label = { Text(mood) }
                )
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("备注") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onSubmit(
                        selectedDate,
                        selectedType,
                        protection,
                        pleasure,
                        selectedMood,
                        notes.ifBlank { null }
                    )
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("保存")
            }
        }
    }
}

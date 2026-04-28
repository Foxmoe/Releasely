package top.foxmoe.releasely.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleForm(
    onSubmit: (startDate: Long, duration: Int?) -> Unit,
    onCancel: () -> Unit,
    initialStartDate: Long = System.currentTimeMillis() / 1000
) {
    var startDate by remember { mutableLongStateOf(initialStartDate) }
    var duration by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "添加月经记录",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it.filter { c -> c.isDigit() } },
            label = { Text("持续天数") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
                    val durationInt = duration.toIntOrNull()
                    onSubmit(startDate, durationInt)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("保存")
            }
        }
    }
}
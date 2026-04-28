package top.foxmoe.releasely.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationForm(
    onSubmit: (name: String, dosage: String, reminderTime: Long) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var reminderHour by remember { mutableStateOf(22) }
    var reminderMinute by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "添加药物",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("药物名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("剂量") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("提醒时间", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = reminderHour.toString(),
                onValueChange = { reminderHour = it.toIntOrNull() ?: 22 },
                modifier = Modifier.width(60.dp),
                singleLine = true
            )
            Text("时")
            OutlinedTextField(
                value = reminderMinute.toString().padStart(2, '0'),
                onValueChange = { reminderMinute = it.toIntOrNull() ?: 0 },
                modifier = Modifier.width(60.dp),
                singleLine = true
            )
            Text("分")
        }

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
                    val now = java.time.LocalDateTime.now()
                    var reminderDateTime = now.withHour(reminderHour).withMinute(reminderMinute)
                    if (reminderDateTime.isBefore(now)) {
                        reminderDateTime = reminderDateTime.plusDays(1)
                    }
                    val reminderTime = reminderDateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
                    onSubmit(name, dosage, reminderTime)
                },
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank() && dosage.isNotBlank()
            ) {
                Text("保存")
            }
        }
    }
}
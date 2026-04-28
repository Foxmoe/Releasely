package top.foxmoe.releasely.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AppLockScreen(
    onUnlock: (pin: String) -> Boolean,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "输入 PIN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                    pin = it
                    error = false
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.width(200.dp),
            singleLine = true,
            isError = error
        )

        if (error) {
            Text(
                text = "PIN 错误",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!onUnlock(pin)) {
                    error = true
                    pin = ""
                }
            },
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = pin.length >= 4
        ) {
            Text("解锁")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onCancel) {
            Text("取消")
        }
    }
}

@Composable
fun DecoyScreen() {
    var display by remember { mutableStateOf("0") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = display,
            fontSize = 64.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("AC", "+/-", "%", "÷").forEach { label ->
                CalcButton(label, Color(0xFFA5A5A5)) {
                    display = when (label) {
                        "AC" -> "0"
                        "+/-" -> (display.toDoubleOrNull()?.times(-1) ?: 0.0).toString()
                        "%" -> (display.toDoubleOrNull()?.div(100) ?: 0.0).toString()
                        else -> display
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("7", "8", "9", "×").forEach { label ->
                CalcButton(label, Color(0xFF505050)) {
                    display = label
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("4", "5", "6", "-").forEach { label ->
                CalcButton(label, Color(0xFF505050)) {
                    display = label
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("1", "2", "3", "+").forEach { label ->
                CalcButton(label, Color(0xFF505050)) {
                    display = label
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton("0", Color(0xFF505050), modifier = Modifier.weight(2f)) {
                display = "0"
            }
            CalcButton(".", Color(0xFF505050)) {
                if (!display.contains(".")) display += "."
            }
            CalcButton("=", Color(0xFFFF9F0A)) {
                display = "="
            }
        }
    }
}

@Composable
fun CalcButton(
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(32.dp)
    ) {
        Text(label, fontSize = 28.sp, color = Color.White)
    }
}
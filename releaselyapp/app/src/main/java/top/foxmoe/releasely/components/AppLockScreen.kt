package top.foxmoe.releasely.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
    onCancel: () -> Unit,
    useBiometric: Boolean = false,
    onBiometricUnlock: () -> Unit = {}
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

        if (useBiometric) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBiometricUnlock,
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "生物识别",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("指纹/面容解锁")
            }
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
    var waitingForSecondOperand by remember { mutableStateOf(false) }

    fun calculate(): Double {
        val second = display.toDoubleOrNull() ?: 0.0
        val first = firstOperand ?: 0.0
        return when (operator) {
            "+" -> first + second
            "-" -> first - second
            "×" -> first * second
            "÷" -> if (second != 0.0) first / second else 0.0
            else -> second
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = display,
            fontSize = 64.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        val buttons = listOf(
            listOf("AC", "+/-", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val color = when (label) {
                        "AC", "+/-", "%" -> Color(0xFFA5A5A5)
                        "=", "÷", "×", "-", "+" -> Color(0xFFFF9F0A)
                        else -> Color(0xFF505050)
                    }
                    val modifier = if (label == "0") Modifier.weight(2f) else Modifier.weight(1f)
                    CalcButton(label, color, modifier = modifier) {
                        when (label) {
                            "AC" -> {
                                display = "0"
                                firstOperand = null
                                operator = null
                                waitingForSecondOperand = false
                            }
                            "+/-" -> {
                                display = (display.toDoubleOrNull()?.times(-1) ?: 0.0).toString()
                            }
                            "%" -> {
                                display = (display.toDoubleOrNull()?.div(100) ?: 0.0).toString()
                            }
                            "+", "-", "×", "÷" -> {
                                firstOperand = display.toDoubleOrNull()
                                operator = label
                                waitingForSecondOperand = true
                            }
                            "=" -> {
                                val result = calculate()
                                display = if (result == result.toLong().toDouble()) {
                                    result.toLong().toString()
                                } else {
                                    result.toString()
                                }
                                firstOperand = null
                                operator = null
                                waitingForSecondOperand = false
                            }
                            "." -> {
                                if (!display.contains(".")) display += "."
                            }
                            else -> {
                                if (waitingForSecondOperand) {
                                    display = label
                                    waitingForSecondOperand = false
                                } else {
                                    display = if (display == "0") label else display + label
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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

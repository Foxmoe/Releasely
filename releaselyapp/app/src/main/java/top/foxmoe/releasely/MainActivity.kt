package top.foxmoe.releasely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.foxmoe.releasely.shared.Greeting

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val greeting = Greeting()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(greeting)
                }
            }
        }
    }
}

@Composable
fun AppContent(greeting: Greeting) {
    var showMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "KMP 环境测试",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { showMessage = !showMessage },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(if (showMessage) "隐藏消息" else "显示来自 Shared 的消息")
        }

        if (showMessage) {
            Text(
                text = greeting.greet(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 30.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
package top.foxmoe.releasely.shared.feature.panic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class PanicMode {
    NONE,
    CALCULATOR,
    WEATHER
}

@Composable
fun PanicOverlay(
    mode: PanicMode,
    modifier: Modifier = Modifier
) {
    if (mode == PanicMode.NONE) return

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (mode) {
            PanicMode.CALCULATOR -> FakeCalculator()
            PanicMode.WEATHER -> FakeWeather()
            PanicMode.NONE -> Unit
        }
    }
}

@Composable
private fun FakeCalculator() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Calculator", style = MaterialTheme.typography.displayLarge)
        Text("0", style = MaterialTheme.typography.displayLarge)
        repeat(4) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("7  8  9  /", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun FakeWeather() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("City Weather", style = MaterialTheme.typography.displayLarge)
        Text("Sunny 24°C", style = MaterialTheme.typography.bodyLarge)
        Text("Humidity 41%", style = MaterialTheme.typography.bodyMedium)
        Text("Wind NE 3m/s", style = MaterialTheme.typography.bodyMedium)
        Text("Tomorrow: Cloudy 22°C", style = MaterialTheme.typography.bodyMedium)
    }
}

package top.foxmoe.releasely.shared.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaselyTopAppBar(
    title: String,
    onPanicClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        actions = {
            TextButton(onClick = onPanicClick) {
                Text(text = "伪装", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

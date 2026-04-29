package top.foxmoe.releasely.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.foxmoe.releasely.ReleaselyApp
import top.foxmoe.releasely.services.WishRecord

@Composable
fun WishlistScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val scope = rememberCoroutineScope()

    var wishes by remember { mutableStateOf<List<WishRecord>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        wishes = app.wishlistService.getAllWishes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "愿望清单",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 添加按钮
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ 添加愿望")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(wishes.size) { index ->
                val wish = wishes[index]
                WishItemCard(
                    wish = wish,
                    onToggle = {
                        scope.launch {
                            app.wishlistService.toggleWishStatus(wish.id, !wish.isCompleted)
                            wishes = app.wishlistService.getAllWishes()
                        }
                    },
                    onDelete = {
                        scope.launch {
                            app.wishlistService.deleteWish(wish.id)
                            wishes = app.wishlistService.getAllWishes()
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddWishDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc ->
                scope.launch {
                    app.wishlistService.insertWish(title, desc)
                    wishes = app.wishlistService.getAllWishes()
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WishItemCard(
    wish: WishRecord,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (wish.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wish.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (wish.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (wish.isCompleted) Color.Gray else Color.Black
                )
                wish.description?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textDecoration = if (wish.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
            Row {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = if (wish.isCompleted) "标记未完成" else "标记完成",
                        tint = if (wish.isCompleted) Color(0xFF4CAF50) else Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddWishDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加愿望") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description.takeIf { it.isNotBlank() })
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

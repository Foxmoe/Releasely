package top.foxmoe.releasely.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.foxmoe.releasely.Gender
import top.foxmoe.releasely.ReleaselyApp
import top.foxmoe.releasely.components.GenderDropdown

@Composable
fun PersonalProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ReleaselyApp
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.Mr) }
    var age by remember { mutableStateOf("") }
    var hasPartner by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val profile = app.getActiveProfile()
        profile?.let {
            name = it.name
            gender = Gender.entries.find { g -> g.name == it.gender } ?: Gender.Mr
            age = it.age.toString()
            hasPartner = it.has_partner == 1L
        }
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
            Text(text = "个人资料", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("性别", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        GenderDropdown(
            selectedGender = gender,
            onGenderSelected = { gender = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it.filter { c -> c.isDigit() } },
            label = { Text("年龄") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("有伴侣", fontSize = 16.sp)
            Switch(checked = hasPartner, onCheckedChange = { hasPartner = it })
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val profile = app.getActiveProfile()
                    profile?.let {
                        app.profileQueries.updateProfile(
                            name = name,
                            gender = gender.name,
                            age = age.toLongOrNull() ?: it.age,
                            has_partner = if (hasPartner) 1L else 0L,
                            id = it.id
                        )
                    }
                    withContext(Dispatchers.Main) {
                        saved = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("保存")
        }

        if (saved) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("保存成功", color = Color(0xFF4CAF50), fontSize = 14.sp)
        }
    }
}

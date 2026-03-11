package top.foxmoe.releasely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as ReleaselyApp
        val queries = app.profileQueries

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isChecking by remember { mutableStateOf(true) }
                    var showWelcome by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        try {
                            withContext(Dispatchers.IO) {
                                val count = queries.countProfiles().executeAsOne()
                                showWelcome = count == 0L
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Fallback in case of DB error
                            showWelcome = true
                        } finally {
                            isChecking = false
                        }
                    }

                    if (!isChecking) {
                        if (showWelcome) {
                            WelcomeScreen(onFinished = { name, gender, age, hasPartner ->
                                scope.launch(Dispatchers.IO) {
                                    val id = UUID.randomUUID().toString()
                                    try {
                                        queries.transaction {
                                            queries.deactivateAllProfiles()
                                            queries.insertNewProfile(
                                                id = id,
                                                name = name,
                                                gender = gender.name,
                                                age = age.toLong(),
                                                has_partner = if (hasPartner) 1L else 0L,
                                                is_active = 1L
                                            )
                                        }
                                        withContext(Dispatchers.Main) {
                                            showWelcome = false
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                        } else {
                            MainScreen()
                        }
                    }
                }
            }
        }
    }
}
package top.foxmoe.releasely

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateScreenshotProtection()

        val app = application as ReleaselyApp
        val queries = app.profileQueries

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var screenState by remember { mutableStateOf<ScreenState>(ScreenState.Loading) }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        screenState = checkSecurityAndShowScreen(app)
                    }

                    when (val state = screenState) {
                        is ScreenState.Loading -> {
                            // Loading
                        }
                        is ScreenState.Decoy -> {
                            DecoyScreenComponent()
                        }
                        is ScreenState.AppLock -> {
                            val biometricPrefs = getSharedPreferences("privacy", MODE_PRIVATE)
                            val biometricEnabled = biometricPrefs.getBoolean("biometric_unlock", false)

                            AppLockScreenComponent(
                                onUnlock = { pin ->
                                    val result = runCatching {
                                        kotlinx.coroutines.runBlocking {
                                            app.securitySettingsService.verifyPin(null, pin)
                                        }
                                    }.getOrDefault(false)
                                    if (result) {
                                        screenState = ScreenState.Main(checkProfile(app))
                                        true
                                    } else {
                                        false
                                    }
                                },
                                onCancel = { finish() },
                                useBiometric = biometricEnabled,
                                onBiometricUnlock = {
                                    screenState = ScreenState.Main(checkProfile(app))
                                }
                            )

                            // 自动触发生物识别
                            LaunchedEffect(biometricEnabled) {
                                if (biometricEnabled) {
                                    showBiometricPrompt(
                                        onSuccess = {
                                            screenState = ScreenState.Main(checkProfile(app))
                                        },
                                        onError = { /* 失败时让用户手动输入 PIN */ }
                                    )
                                }
                            }
                        }
                        is ScreenState.Welcome -> {
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
                                            screenState = ScreenState.Main(true)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                        }
                        is ScreenState.Main -> {
                            if (state.hasProfile) {
                                MainScreen()
                            } else {
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
                                                screenState = ScreenState.Main(true)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkSecurityAndShowScreen(app: ReleaselyApp): ScreenState {
        val settings = app.securitySettingsService.getSettings(null)

        if (settings.decoyEnabled) {
            return ScreenState.Decoy
        }

        if (settings.appLockEnabled) {
            return ScreenState.AppLock
        }

        return ScreenState.Main(checkProfile(app))
    }

    private fun checkProfile(app: ReleaselyApp): Boolean {
        return try {
            val count = app.profileQueries.countProfiles().executeAsOne()
            count > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit, onError: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物识别解锁")
            .setSubtitle("使用指纹或面容解锁 Releasely")
            .setNegativeButtonText("使用 PIN")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun updateScreenshotProtection() {
        val prefs = getSharedPreferences("privacy", MODE_PRIVATE)
        val enabled = prefs.getBoolean("screenshot_protection", true)
        if (enabled) {
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

sealed class ScreenState {
    data object Loading : ScreenState()
    data object Decoy : ScreenState()
    data object AppLock : ScreenState()
    data object Welcome : ScreenState()
    data class Main(val hasProfile: Boolean) : ScreenState()
}

@Composable
fun DecoyScreenComponent() {
    top.foxmoe.releasely.components.DecoyScreen()
}

@Composable
fun AppLockScreenComponent(
    onUnlock: (String) -> Boolean,
    onCancel: () -> Unit,
    useBiometric: Boolean = false,
    onBiometricUnlock: () -> Unit = {}
) {
    top.foxmoe.releasely.components.AppLockScreen(onUnlock, onCancel, useBiometric, onBiometricUnlock)
}

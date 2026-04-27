package top.foxmoe.releasely

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import top.foxmoe.releasely.shared.data.local.DatabaseDriverFactory
import top.foxmoe.releasely.shared.di.sharedModule

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                modules(
                    sharedModule(
                        baseUrl = "http://10.0.2.2:8080",
                        driverFactory = DatabaseDriverFactory(this@MainActivity),
                        camouflageController = AppIconManager(this@MainActivity)
                    )
                )
            }
        }

        setContent {
            ReleaselyRootApp()
        }
    }
}
package top.foxmoe.releasely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import top.foxmoe.releasely.shared.data.local.DatabaseDriverFactory
import top.foxmoe.releasely.shared.di.sharedModule

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
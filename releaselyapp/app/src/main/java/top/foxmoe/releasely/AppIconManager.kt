package top.foxmoe.releasely

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import top.foxmoe.releasely.shared.domain.service.CamouflageController

class AppIconManager(
    private val context: Context
) : CamouflageController {

    private val packageManager: PackageManager = context.packageManager
    private val normalAlias = ComponentName(context, "${context.packageName}.LauncherNormal")
    private val calcAlias = ComponentName(context, "${context.packageName}.LauncherCalc")

    override fun setCamouflageEnabled(enabled: Boolean) {
        val normalState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        val calcState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        packageManager.setComponentEnabledSetting(
            normalAlias,
            normalState,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            calcAlias,
            calcState,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun isCamouflageEnabled(): Boolean {
        val state = packageManager.getComponentEnabledSetting(calcAlias)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
}


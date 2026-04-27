package top.foxmoe.releasely.shared.di

import android.content.Context
import org.koin.core.KoinApplication
import top.foxmoe.releasely.shared.data.local.DatabaseDriverFactory

fun initKoinAndroid(
    context: Context,
    baseUrl: String,
    appDeclaration: KoinApplication.() -> Unit = {}
) = initKoin(
    baseUrl = baseUrl,
    driverFactory = DatabaseDriverFactory(context),
    appDeclaration = appDeclaration
)

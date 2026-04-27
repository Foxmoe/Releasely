package top.foxmoe.releasely.shared.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.KoinApplication
import org.koin.dsl.module
import top.foxmoe.releasely.shared.data.local.DatabaseDriverFactory
import top.foxmoe.releasely.shared.data.local.LocalDatabase
import top.foxmoe.releasely.shared.data.remote.ApiService
import top.foxmoe.releasely.shared.data.remote.KtorApiService
import top.foxmoe.releasely.shared.data.remote.createHttpClient
import top.foxmoe.releasely.shared.data.repository.AuthRepositoryImpl
import top.foxmoe.releasely.shared.data.repository.HealthRecordRepositoryImpl
import top.foxmoe.releasely.shared.domain.repository.AuthRepository
import top.foxmoe.releasely.shared.domain.repository.HealthRecordRepository
import top.foxmoe.releasely.shared.domain.service.CamouflageController
import top.foxmoe.releasely.shared.domain.service.NoOpCamouflageController
import top.foxmoe.releasely.shared.domain.usecase.LoginUseCase
import top.foxmoe.releasely.shared.domain.usecase.SaveRecordUseCase
import top.foxmoe.releasely.shared.presentation.viewmodel.MainViewModel

fun sharedModule(
    baseUrl: String,
    driverFactory: DatabaseDriverFactory,
    camouflageController: CamouflageController = NoOpCamouflageController()
): Module = module {
    single { createHttpClient() }
    single<ApiService> { KtorApiService(get(), baseUrl) }

    single { driverFactory }
    single { LocalDatabase(get()) }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<HealthRecordRepository> { HealthRecordRepositoryImpl(get()) }

    single { LoginUseCase(get()) }
    single { SaveRecordUseCase(get()) }
    single { camouflageController }

    factory { MainViewModel(get(), get(), get(), get()) }
}

fun initKoin(
    baseUrl: String,
    driverFactory: DatabaseDriverFactory,
    camouflageController: CamouflageController = NoOpCamouflageController(),
    appDeclaration: KoinApplication.() -> Unit = {}
) = startKoin {
    appDeclaration()
    modules(sharedModule(baseUrl, driverFactory, camouflageController))
}

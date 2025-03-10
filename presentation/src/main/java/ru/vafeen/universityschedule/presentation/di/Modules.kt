package ru.vafeen.universityschedule.presentation.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.vafeen.universityschedule.domain.MainActivityIntentProvider
import ru.vafeen.universityschedule.presentation.MainActivityIntentProviderImpl
import ru.vafeen.universityschedule.presentation.components.edit_link_dialog.EditLinkDialogViewModel
import ru.vafeen.universityschedule.presentation.main.MainActivityViewModel
import ru.vafeen.universityschedule.presentation.features.main_screen.MainScreenViewModel
import ru.vafeen.universityschedule.presentation.features.settings_screen.SettingsScreenViewModel


internal val koinViewModelDIModule = module {
    viewModelOf(::MainActivityViewModel)
    viewModelOf(::MainScreenViewModel)
    viewModelOf(::SettingsScreenViewModel)
    viewModelOf(::EditLinkDialogViewModel)
}
internal val mainActivityIntentProviderDIModule = module {
    singleOf(::MainActivityIntentProviderImpl) {
        bind<MainActivityIntentProvider>()
    }
}
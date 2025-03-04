package ru.vafeen.universityschedule.presentation.main

import ru.vafeen.universityschedule.domain.models.Release
import ru.vafeen.universityschedule.domain.models.Settings
import ru.vafeen.universityschedule.presentation.navigation.Screen

internal data class MainActivityState(
    /**
     * Получает версию приложения.
     */
    val versionCode: Long,
    val release: Release? = null,
    val isUpdateInProcess: Boolean = false,
    val percentage: Float = 0f,
    val settings: Settings,
    /**
     * Экран, который должен отображаться при запуске приложения.
     */
    val startScreen: Screen = Screen.Main,
    val currentScreen:Screen = startScreen,

)
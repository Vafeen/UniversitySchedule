package ru.vafeen.universityschedule.presentation.navigation


import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.vafeen.universityschedule.domain.utils.getMainColorForThisThemeOrDefault
import ru.vafeen.universityschedule.presentation.components.bottom_bar.BottomBar
import ru.vafeen.universityschedule.presentation.components.bottom_sheet.NewVersionInfoBottomSheet
import ru.vafeen.universityschedule.presentation.features.main_screen.MainScreen
import ru.vafeen.universityschedule.presentation.features.settings_screen.SettingsScreen
import ru.vafeen.universityschedule.presentation.components.ui_utils.UpdateAvailable
import ru.vafeen.universityschedule.presentation.components.ui_utils.UpdateProgress
import ru.vafeen.universityschedule.presentation.main.MainActivityEvent
import ru.vafeen.universityschedule.presentation.main.MainActivityViewModel
import ru.vafeen.universityschedule.presentation.theme.Theme

@Composable
internal fun NavigationRoot(
    viewModel: MainActivityViewModel,
) {
    val state by viewModel.state.collectAsState()
    // Определение текущей темы (светлая или темная)
    val dark = isSystemInDarkTheme()


    // Проверка обновлений и отображение нижнего листа с информацией о версии
    LaunchedEffect(null) {
        viewModel.sendEvent(MainActivityEvent.CheckUpdates)
    }

    // Показ информации о новой версии, если она еще не была показана
    if (state.settings.lastDemonstratedVersion < state.versionCode && state.settings.releaseBody != "") {
        NewVersionInfoBottomSheet(viewModel = viewModel) {
            viewModel.sendEvent(MainActivityEvent.SaveSettingsEvent {
                it.copy(lastDemonstratedVersion = state.versionCode)
            })
        }
    }

    // Scaffold для отображения интерфейса с нижней панелью
    Scaffold(containerColor = Theme.colors.singleTheme,
        bottomBar = {
            BottomBar(
                selectedScreen = state.currentScreen,
                bottomBarNavigator = viewModel,
                containerColor = state.settings.getMainColorForThisThemeOrDefault(
                    dark,
                    Theme.colors.mainColor
                ),
            )
        }) { innerPadding ->
        // Основное содержимое экрана
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .background(Theme.colors.singleTheme)
        ) {
            // Навигация между экранами с помощью NavHost
            NavHost(
                modifier = Modifier.weight(1f),
                navController = viewModel.navController as NavHostController,
                startDestination = state.startScreen,
                enterTransition = { fadeIn(animationSpec = tween()) },
                exitTransition = { fadeOut(animationSpec = tween()) },
                popEnterTransition = { fadeIn(animationSpec = tween()) },
                popExitTransition = { fadeOut(animationSpec = tween()) },
            ) {
                composable<Screen.Main> {
                    MainScreen(viewModel)
                }
                composable<Screen.Settings> {
                    SettingsScreen(viewModel)
                }
            }
            state.release?.let {
                UpdateAvailable(release = it) {
                    viewModel.sendEvent(MainActivityEvent.UpdateApp)
                }
            }
            // Показывать индикатор загрузки, если обновление в процессе
            if (state.isUpdateInProcess) UpdateProgress(percentage = state.percentage)
        }
    }
}
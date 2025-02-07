package ru.vafeen.universityschedule.presentation.navigation


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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.vafeen.universityschedule.domain.models.Release
import ru.vafeen.universityschedule.domain.utils.getMainColorForThisTheme
import ru.vafeen.universityschedule.presentation.components.bottom_bar.BottomBar
import ru.vafeen.universityschedule.presentation.components.bottom_sheet.NewVersionInfoBottomSheet
import ru.vafeen.universityschedule.presentation.components.screens.MainScreen
import ru.vafeen.universityschedule.presentation.components.screens.SettingsScreen
import ru.vafeen.universityschedule.presentation.components.ui_utils.UpdateAvailable
import ru.vafeen.universityschedule.presentation.components.ui_utils.UpdateProgress
import ru.vafeen.universityschedule.presentation.components.viewModels.MainActivityViewModel
import ru.vafeen.universityschedule.presentation.theme.Theme

@Composable
internal fun NavigationRoot(
    viewModel: MainActivityViewModel,
) {
    // Подписка на состояние процесса обновления и процента скачанных данных
    val isUpdateInProcess by viewModel.isUpdateInProcessFlow.collectAsState(false)
    val downloadedPercentage by viewModel.percentageFlow.collectAsState(0f)

    // Определение текущей темы (светлая или темная)
    val dark = isSystemInDarkTheme()
    val defaultColor = Theme.colors.mainColor

    // Получение настроек пользователя
    val settings by viewModel.settings.collectAsState()

    // Определение основного цвета в зависимости от текущей темы
    val mainColor by remember {
        derivedStateOf {
            settings.getMainColorForThisTheme(isDark = dark) ?: defaultColor
        }
    }

    // Стейт для контроля показа информации о новой версии

    var releaseForUpdates: Release? by remember { mutableStateOf(null) }
    // Проверка обновлений и отображение нижнего листа с информацией о версии
    LaunchedEffect(null) {
        releaseForUpdates = viewModel.checkUpdates()
    }

    // Показ информации о новой версии, если она еще не была показана
    if (settings.lastDemonstratedVersion < viewModel.versionCode && settings.releaseBody != "") {
        NewVersionInfoBottomSheet(viewModel = viewModel) {
            viewModel.saveSettingsToSharedPreferences { settings ->
                settings.copy(lastDemonstratedVersion = viewModel.versionCode)
            }
        }
    }

    // Лаунч эффекта для вызова миграции API
    LaunchedEffect(null) {
        viewModel.callSchedulerAPIMigration()
    }

    // Scaffold для отображения интерфейса с нижней панелью
    Scaffold(containerColor = Theme.colors.singleTheme,
        bottomBar = {
            val selectedScreen by viewModel.currentScreen.collectAsState()
            BottomBar(
                selectedScreen = selectedScreen,
                bottomBarNavigator = viewModel,
                containerColor = mainColor,
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
                startDestination = viewModel.startScreen,
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
            releaseForUpdates?.let {
                UpdateAvailable(release = it) {
                    viewModel.update()
                    releaseForUpdates = null
                }
            }
            // Показывать индикатор загрузки, если обновление в процессе
            if (isUpdateInProcess) UpdateProgress(percentage = downloadedPercentage)
        }
    }
}
package ru.vafeen.universityschedule.presentation.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.vafeen.universityschedule.domain.network.result.DownloadStatus
import ru.vafeen.universityschedule.domain.network.service.Refresher
import ru.vafeen.universityschedule.domain.network.service.SettingsManager
import ru.vafeen.universityschedule.domain.scheduler.SchedulerAPIMigrationManager
import ru.vafeen.universityschedule.domain.usecase.network.GetLatestReleaseUseCase
import ru.vafeen.universityschedule.domain.utils.getVersionCode
import ru.vafeen.universityschedule.domain.utils.getVersionName
import ru.vafeen.universityschedule.domain.utils.launchIO
import ru.vafeen.universityschedule.presentation.components.viewModels.BaseStateViewModel
import ru.vafeen.universityschedule.presentation.navigation.BottomBarNavigator
import ru.vafeen.universityschedule.presentation.navigation.Screen
import ru.vafeen.universityschedule.presentation.utils.Link
import ru.vafeen.universityschedule.presentation.utils.RefresherInfo
import ru.vafeen.universityschedule.presentation.utils.copyTextToClipBoard
import kotlin.system.exitProcess

/**
 * ViewModel для управления состоянием главной активности приложения.
 * Обрабатывает обновления, миграцию данных, навигацию и общие ошибки.
 *
 * @param getLatestReleaseUseCase UseCase для получения последней версии приложения.
 * @param refresher Сервис для скачивания и обновлений.
 * @param context Контекст приложения для работы с ресурсами и управления ошибками.
 * @param schedulerAPIMigrationManager Менеджер миграции API, который отвечает за перенос с AlarmManager на WorkManager.
 * @param settingsManager Менеджер для работы с настройками приложения.
 */
internal class MainActivityViewModel(
    private val getLatestReleaseUseCase: GetLatestReleaseUseCase,
    context: Context,
    private val schedulerAPIMigrationManager: SchedulerAPIMigrationManager,
    private val settingsManager: SettingsManager,
    private val refresher: Refresher,
) : BaseStateViewModel<MainActivityState, MainActivityEvent>(), BottomBarNavigator {
    override val _state: MutableStateFlow<MainActivityState> =
        MutableStateFlow(
            MainActivityState(
                versionCode = context.getVersionCode(),
                settings = settingsManager.settingsFlow.value
            )
        )
    override val state: StateFlow<MainActivityState> = _state

    override fun sendEvent(event: MainActivityEvent) {
        when (event) {
            MainActivityEvent.CheckUpdates -> {
                viewModelScope.launchIO {
                    checkUpdates()
                }
            }

            MainActivityEvent.UpdateApp -> {
                update()
            }

            MainActivityEvent.ClearReleaseData -> {
                updateState { it.copy(release = null) }
            }

            is MainActivityEvent.SaveSettingsEvent -> {
                settingsManager.save(event.saving)
            }
        }
    }


    private val versionName = context.getVersionName()

    private suspend fun checkUpdates() {
        val localRelease = getLatestReleaseUseCase.invoke()
        updateState { s ->
            s.copy(release = if (localRelease != null && versionName != null &&
                localRelease.tagName.substringAfter("v") != versionName
            ) {
                settingsManager.save {
                    it.copy(releaseBody = localRelease.body)
                }
                localRelease
            } else null)
        }

    }

    private fun update() {
        state.value.release?.let {
            viewModelScope.launch(Dispatchers.IO) {
                refresher.refresh(viewModelScope, it.apkUrl, RefresherInfo.APK_FILE_NAME)
            }
        }
    }

    init {
        viewModelScope.launchIO {
            refresher.progressFlow.map {
                it !is DownloadStatus.Error && it !is DownloadStatus.Success
            }.collect {
                updateState { s -> s.copy(isUpdateInProcess = it) }
            }
        }
        viewModelScope.launchIO {
            refresher.progressFlow.map {
                when (it) {
                    is DownloadStatus.InProgress -> it.percentage
                    DownloadStatus.Success -> 100f
                    else -> 0f
                }
            }.collect {
                updateState { s -> s.copy(percentage = it) }
            }
        }
        viewModelScope.launchIO {
            settingsManager.settingsFlow.collect { s ->
                updateState {
                    it.copy(settings = s)
                }
            }
        }
    }


    /**
     * Регистрирует обработчик необработанных исключений для приложения.
     * В случае ошибки копирует информацию об ошибке в буфер обмена и завершает процесс.
     *
     * @param context Контекст приложения, используемый для работы с буфером обмена.
     */
    private fun registerGeneralExceptionCallback(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            context.copyTextToClipBoard(
                label = "Error",
                text = "Contact us about this problem: ${Link.EMAIL}\n\n Exception in ${thread.name} thread\n${throwable.stackTraceToString()}"
            )
            Log.e(
                "GeneralException",
                "Exception in thread ${thread.name}",
                throwable
            )
            exitProcess(0)
        }
    }

    init {
        // Регистрируем обработчик исключений при инициализации ViewModel.
        registerGeneralExceptionCallback(context = context)
    }

    /**
     * Контроллер навигации, управляющий переходами между экранами.
     */
    override var navController: NavHostController? = null


    /**
     * Эмитирует текущий экран на основе стека навигации.
     * Отслеживает изменения в backStack навигации и обновляет состояние текущего экрана.
     */
    private fun emitCurrentScreen() {
        viewModelScope.launch(Dispatchers.Main) {
            navController?.currentBackStackEntryFlow?.collect { backStackEntry ->
                val destination = backStackEntry.destination
                when {
                    destination.hasRoute(Screen.Main::class) -> updateState {
                        it.copy(currentScreen = Screen.Main)
                    }

                    destination.hasRoute(Screen.Settings::class) -> updateState {
                        it.copy(currentScreen = Screen.Settings)
                    }
                }
            }
        }
    }

    /**
     * Обрабатывает действие "Назад". Переходит на предыдущий экран в навигации.
     */
    override fun back() {
        navController?.popBackStack()
        emitCurrentScreen()
    }

    /**
     * Переходит на указанный экран.
     *
     * @param screen Целевой экран для навигации.
     */
    override fun navigateTo(screen: Screen) {
        if (screen != Screen.Main)
            navController?.navigate(screen)
        else navController?.popBackStack()
        emitCurrentScreen()
    }


}

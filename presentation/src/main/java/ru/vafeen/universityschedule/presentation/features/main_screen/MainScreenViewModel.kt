package ru.vafeen.universityschedule.presentation.features.main_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.vafeen.universityschedule.domain.models.Lesson
import ru.vafeen.universityschedule.domain.models.Reminder
import ru.vafeen.universityschedule.domain.models.Settings
import ru.vafeen.universityschedule.domain.network.service.SettingsManager
import ru.vafeen.universityschedule.domain.usecase.CatMeowUseCase
import ru.vafeen.universityschedule.domain.usecase.db.DeleteRemindersUseCase
import ru.vafeen.universityschedule.domain.usecase.db.GetAsFlowLessonsUseCase
import ru.vafeen.universityschedule.domain.usecase.db.GetAsFlowRemindersUseCase
import ru.vafeen.universityschedule.domain.usecase.db.GetReminderByIdOfReminderUseCase
import ru.vafeen.universityschedule.domain.usecase.db.InsertLessonsUseCase
import ru.vafeen.universityschedule.domain.usecase.db.InsertRemindersUseCase
import ru.vafeen.universityschedule.domain.usecase.db.UpdateLessonsUseCase
import ru.vafeen.universityschedule.domain.usecase.scheduler.CancelJobUseCase
import ru.vafeen.universityschedule.domain.usecase.scheduler.ScheduleRepeatingJobUseCase
import ru.vafeen.universityschedule.domain.utils.launchIO
import ru.vafeen.universityschedule.presentation.components.viewModels.BaseStateViewModel
import java.time.LocalDate

/**
 * ViewModel для главного экрана приложения, который управляет расписанием пар,
 * напоминаниями и настройками. Обрабатывает добавление, удаление, обновление пар и напоминаний,
 * а также управление миграцией данных и задачами планировщика.
 *
 * @param getAsFlowLessonsUseCase UseCase для получения пар как потока данных.
 * @param getAsFlowRemindersUseCase UseCase для получения напоминаний как потока данных.
 * @param insertLessonsUseCase UseCase для добавления новых пар.
 * @param insertRemindersUseCase UseCase для добавления новых напоминаний.
 * @param deleteAllReminderUseCase UseCase для удаления всех напоминаний.
 * @param getReminderByIdOfReminderUseCase UseCase для получения напоминания по ID.
 * @param scheduleRepeatingJobUseCase UseCase для планирования повторяющихся задач.
 * @param catMeowUseCase UseCase для вызова "мяу" .
 * @param cancelJobUseCase UseCase для отмены задач.
 * @param updateLessonsUseCase UseCase для обновления информации о парах.
 * @param settingsManager Менеджер для работы с настройками приложения.
 */
internal class MainScreenViewModel(
    getAsFlowLessonsUseCase: GetAsFlowLessonsUseCase,
    getAsFlowRemindersUseCase: GetAsFlowRemindersUseCase,
    private val insertLessonsUseCase: InsertLessonsUseCase,
    private val insertRemindersUseCase: InsertRemindersUseCase,
    private val deleteAllReminderUseCase: DeleteRemindersUseCase,
    private val getReminderByIdOfReminderUseCase: GetReminderByIdOfReminderUseCase,
    private val scheduleRepeatingJobUseCase: ScheduleRepeatingJobUseCase,
    private val catMeowUseCase: CatMeowUseCase,
    private val cancelJobUseCase: CancelJobUseCase,
    private val updateLessonsUseCase: UpdateLessonsUseCase,
    private val settingsManager: SettingsManager
) : BaseStateViewModel<MainScreenState, MainScreenEvent>() {
    override val _state = MutableStateFlow(MainScreenState())
    override val state: StateFlow<MainScreenState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) {
            settingsManager.settingsFlow.collect { s ->
                updateState {
                    it.copy(settings = s)
                }
            }
        }
        viewModelScope.launchIO {
            getAsFlowLessonsUseCase.invoke().collect {
                updateState { s ->
                    s.copy(lessons = it)
                }
            }
        }
        viewModelScope.launchIO {
            getAsFlowRemindersUseCase.invoke().collect {
                updateState { s ->
                    s.copy(reminders = it)
                }
            }
        }
    }

    override fun sendEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.NowIsLessonEvent -> {
                updateState {
                    it.copy(nowIsLesson = event.nowIsLesson)
                }
            }
            /**
             * Вызов "мяу" на котиках.
             */
            MainScreenEvent.MeowEvent -> {
                catMeowUseCase.invoke()
            }
            /**
             * Функция для сохранения настроек в SharedPreferences.
             */
            is MainScreenEvent.SaveSettingsEvent -> {
                settingsManager.save(event.saving)
            }

            is MainScreenEvent.IsFrequencyChangingEvent -> {
                updateState {
                    it.copy(isFrequencyChanging = event.isFrequencyChanging)
                }
            }

            is MainScreenEvent.AddReminderAbout15MinutesBeforeLessonAndUpdateLocalDB -> {
                viewModelScope.launchIO {
                    addReminderAbout15MinutesBeforeLessonAndUpdateLocalDB(
                        event.lesson,
                        event.newReminder
                    )
                }
            }

            is MainScreenEvent.AddReminderAboutCheckingOnLessonAndUpdateLocalDB -> {
                viewModelScope.launchIO {
                    addReminderAboutCheckingOnLessonAndUpdateLocalDB(
                        event.lesson,
                        event.newReminder
                    )
                }
            }

            is MainScreenEvent.RemoveReminderAbout15MinutesBeforeLessonAndUpdateLocalDB -> {
                viewModelScope.launchIO {
                    removeReminderAbout15MinutesBeforeLessonAndUpdateLocalDB(event.lesson)
                }
            }

            is MainScreenEvent.RemoveReminderAboutCheckingOnLessonAndUpdateLocalDB -> {
                viewModelScope.launchIO {
                    removeReminderAboutCheckingOnLessonAndUpdateLocalDB(event.lesson)
                }
            }

            is MainScreenEvent.UpdateLessonEvent -> {
                viewModelScope.launchIO {
                    updateLessonsUseCase.invoke(event.lesson)
                }
            }
        }
    }


    /**
     * Функция для добавления напоминания за 15 минут до начала пары
     * и обновления локальной базы данных.
     * @param lesson Пара, для которой создается напоминание.
     * @param newReminder Новое напоминание.
     */
    private suspend fun addReminderAbout15MinutesBeforeLessonAndUpdateLocalDB(
        lesson: Lesson, newReminder: Reminder
    ) {
        val newLesson = lesson.copy(idOfReminderBeforeLesson = newReminder.idOfReminder)
        insertLessonsUseCase.invoke(newLesson)
        insertRemindersUseCase.invoke(newReminder)
        scheduleRepeatingJobUseCase.invoke(reminder = newReminder)
    }

    /**
     * Функция для удаления напоминания о проверке за 15 минут до начала пары
     * и обновления локальной базы данных.
     * @param lesson Пара, для которой удаляется напоминание.
     */
    private suspend fun removeReminderAbout15MinutesBeforeLessonAndUpdateLocalDB(
        lesson: Lesson
    ) {
        val newLesson = lesson.copy(idOfReminderBeforeLesson = null)
        insertLessonsUseCase.invoke(newLesson)
        val reminder = getReminderByIdOfReminderUseCase.invoke(
            idOfReminder = lesson.idOfReminderBeforeLesson ?: -1
        )
        reminder?.let {
            cancelJobUseCase.invoke(reminder = it)
            deleteAllReminderUseCase.invoke(it)
        }
    }


    /**
     * Функция для добавления напоминания об отметке на паре
     * и обновления локальной базы данных.
     * @param lesson Пара, для которой создается напоминание.
     * @param newReminder Новое напоминание.
     */
    private suspend fun addReminderAboutCheckingOnLessonAndUpdateLocalDB(
        lesson: Lesson, newReminder: Reminder
    ) {
        insertLessonsUseCase.invoke(lesson.copy(idOfReminderAfterBeginningLesson = newReminder.idOfReminder))

        insertRemindersUseCase.invoke(newReminder)
        scheduleRepeatingJobUseCase.invoke(reminder = newReminder)
    }

    /**
     * Функция для удаления напоминания об отметке на паре
     * и обновления локальной базы данных.
     * @param lesson Пара, для которой удаляется напоминание.
     */
    private suspend fun removeReminderAboutCheckingOnLessonAndUpdateLocalDB(
        lesson: Lesson,
    ) {
        val newLesson = lesson.copy(idOfReminderAfterBeginningLesson = null)
        insertLessonsUseCase.invoke(newLesson)
        val reminder = getReminderByIdOfReminderUseCase.invoke(
            idOfReminder = lesson.idOfReminderAfterBeginningLesson ?: -1
        )
        reminder?.let {
            cancelJobUseCase.invoke(reminder = it)
            deleteAllReminderUseCase.invoke(it)
        }
    }
}

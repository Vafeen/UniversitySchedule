package ru.vafeen.universityschedule.presentation.components.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import ru.vafeen.universityschedule.domain.models.model_additions.Frequency
import ru.vafeen.universityschedule.domain.utils.getMainColorForThisTheme
import ru.vafeen.universityschedule.presentation.components.ui_utils.CardOfNextLesson
import ru.vafeen.universityschedule.presentation.components.ui_utils.StringForSchedule
import ru.vafeen.universityschedule.presentation.components.ui_utils.TextForThisTheme
import ru.vafeen.universityschedule.presentation.components.ui_utils.WeekDay
import ru.vafeen.universityschedule.presentation.components.viewModels.MainScreenViewModel
import ru.vafeen.universityschedule.presentation.navigation.BottomBarNavigator
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.presentation.theme.Theme
import ru.vafeen.universityschedule.presentation.utils.changeFrequencyIfDefinedInSettings
import ru.vafeen.universityschedule.presentation.utils.getDateStringWithWeekOfDay
import ru.vafeen.universityschedule.presentation.utils.getFrequencyByLocalDate
import ru.vafeen.universityschedule.presentation.utils.nowIsLesson
import ru.vafeen.universityschedule.presentation.utils.suitableColor
import ru.vafeen.universityschedule.resources.R
import java.time.LocalDate
import java.time.LocalTime

/**
 * Главный экран приложения.
 *
 * Этот экран отображает расписание уроков, позволяет пользователю
 * взаимодействовать с различными элементами интерфейса и управлять
 * настройками частоты уроков.
 *
 * @param bottomBarNavigator Навигатор для управления нижней панелью навигации.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreen(bottomBarNavigator: BottomBarNavigator) {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val viewModel: MainScreenViewModel = koinViewModel()
    val context = LocalContext.current

    // Подписка на настройки из ViewModel.
    val settings by viewModel.settingsFlow.collectAsState()
    val defaultColor = Theme.colors.mainColor

    // Определение текущей темы (темная или светлая).
    val dark = isSystemInDarkTheme()
    val mainColor by remember {
        mutableStateOf(
            settings.getMainColorForThisTheme(isDark = dark) ?: defaultColor
        )
    }

    var isFrequencyInChanging by remember { mutableStateOf(false) }
    val cor = rememberCoroutineScope()
    var localTime by remember { mutableStateOf(LocalTime.now()) }
    var localDate by remember { mutableStateOf(LocalDate.now()) }

    // Получение частоты по текущей дате.
    val weekOfYear by remember {
        derivedStateOf {
            localDate.getFrequencyByLocalDate()
                .changeFrequencyIfDefinedInSettings(settings = settings)
        }
    }

    // Получение списка уроков из ViewModel.
    val lessons by viewModel.lessonsFlow.collectAsState(listOf())
    val cardsWithDateState = rememberLazyListState()

    // Функция для выбора типа частоты.
    fun chooseTypeOfDefinitionFrequencyDependsOn(selectedFrequency: Frequency?) {
        viewModel.saveSettingsToSharedPreferences {
            it.copy(isSelectedFrequencyCorrespondsToTheWeekNumbers = selectedFrequency?.let { localDate.getFrequencyByLocalDate() == it })
        }
        isFrequencyInChanging = false // Сброс состояния изменения частоты.
    }

    // Создание состояния для горизонтального пагера.
    val pagerState = rememberPagerState(pageCount = { viewModel.pageNumber }, initialPage = 0)

    // Обработка нажатия кнопки "Назад".
    BackHandler {
        when {
            pagerState.currentPage == 0 -> {
                bottomBarNavigator.back()
                (context as Activity).finish()
            }

            else -> {
                cor.launch(Dispatchers.Main) {
                    pagerState.animateScrollToPage(0)
                }
            }
        }
    }

    // Обновление времени каждую секунду.
    LaunchedEffect(key1 = null) {
        withContext(Dispatchers.Main) {
            while (true) {
                localTime = LocalTime.now()
                delay(timeMillis = 1000L)
            }
        }
    }

    // Обновление даты при смене страницы в пагере.
    LaunchedEffect(key1 = pagerState.currentPage) {
        localDate = viewModel.todayDate.plusDays(pagerState.currentPage.toLong())
        cardsWithDateState.animateScrollToItem(if (pagerState.currentPage > 0) pagerState.currentPage - 1 else pagerState.currentPage)
    }

    // Обработка состояния жизненного цикла.
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {}
        }
    }

    // Основной контейнер для экрана.
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isFrequencyInChanging = true }) {
                    Text(
                        text = stringResource(id = weekOfYear.resourceName),
                        fontSize = FontSize.big22,
                        color = Theme.colors.oppositeTheme,
                    )
                    Icon(
                        painter = painterResource(id = if (isFrequencyInChanging) R.drawable.keyboard_arrow_up else R.drawable.keyboard_arrow_down),
                        contentDescription = stringResource(R.string.icon_fold_or_unfold_list_with_frequency),
                        tint = Theme.colors.oppositeTheme,
                    )
                }

                // Выпадающее меню для выбора частоты.
                DropdownMenu(
                    modifier = Modifier
                        .background(Theme.colors.singleTheme)
                        .border(BorderStroke(width = 2.dp, color = Theme.colors.oppositeTheme)),
                    expanded = isFrequencyInChanging,
                    onDismissRequest = { isFrequencyInChanging = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Row {
                                TextForThisTheme(
                                    text = stringResource(id = Frequency.Numerator.resourceName),
                                    fontSize = FontSize.medium19
                                )
                                if (settings.isSelectedFrequencyCorrespondsToTheWeekNumbers != null
                                    && weekOfYear == Frequency.Numerator
                                )
                                    Icon(
                                        painterResource(id = R.drawable.done),
                                        contentDescription = stringResource(R.string.this_is_selected_or_not),
                                        tint = Theme.colors.oppositeTheme
                                    )
                            }
                        },
                        onClick = { chooseTypeOfDefinitionFrequencyDependsOn(selectedFrequency = Frequency.Numerator) })

                    Spacer(
                        modifier = Modifier
                            .height(2.dp)
                            .padding(horizontal = 15.dp)
                            .background(color = Theme.colors.oppositeTheme)
                    )

                    DropdownMenuItem(
                        text = {
                            Row {
                                TextForThisTheme(
                                    text = stringResource(id = Frequency.Denominator.resourceName),
                                    fontSize = FontSize.medium19
                                )
                                if (settings.isSelectedFrequencyCorrespondsToTheWeekNumbers != null
                                    && weekOfYear == Frequency.Denominator
                                )
                                    Icon(
                                        painterResource(id = R.drawable.done),
                                        contentDescription = stringResource(R.string.this_is_selected_or_not),
                                        tint = Theme.colors.oppositeTheme
                                    )
                            }
                        },
                        onClick = { chooseTypeOfDefinitionFrequencyDependsOn(selectedFrequency = Frequency.Denominator) })

                    Spacer(
                        modifier = Modifier
                            .height(2.dp)
                            .padding(horizontal = 15.dp)
                            .background(color = Theme.colors.oppositeTheme)
                    )

                    DropdownMenuItem(
                        text = {
                            Row {
                                TextForThisTheme(
                                    text = stringResource(id = R.string.auto),
                                    fontSize = FontSize.medium19
                                )
                                if (settings.isSelectedFrequencyCorrespondsToTheWeekNumbers == null)
                                    Icon(
                                        painterResource(id = R.drawable.done),
                                        contentDescription = stringResource(R.string.this_is_selected_or_not),
                                        tint = Theme.colors.oppositeTheme
                                    )
                            }
                        },
                        onClick = { chooseTypeOfDefinitionFrequencyDependsOn(selectedFrequency = null) })
                }
            }
        }

        // Горизонтальный ряд для отображения дат.
        LazyRow(
            state = cardsWithDateState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items(count = viewModel.pageNumber) { index ->
                val day by remember { mutableStateOf(viewModel.todayDate.plusDays(index.toLong())) }
                Column {
                    Card(
                        modifier = Modifier
                            .fillParentMaxWidth(1 / 3f)
                            .padding(horizontal = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (day == viewModel.todayDate) mainColor else Theme.colors.buttonColor,
                            contentColor = (if (day == viewModel.todayDate) mainColor else Theme.colors.buttonColor).suitableColor(),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    ) {
                        Text(
                            text = day.getDateStringWithWeekOfDay(context = context),
                            fontSize = FontSize.small17,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    cor.launch(Dispatchers.Main) {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(vertical = 5.dp, horizontal = 10.dp),
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Отображение разделителя для текущей даты.
                    if (day == localDate)
                        Card(
                            modifier = Modifier
                                .fillParentMaxWidth(1 / 3f)
                                .padding(top = 2.dp)
                                .padding(horizontal = 18.dp)
                                .height(2.dp),
                            colors = CardDefaults.cardColors(containerColor = Theme.colors.oppositeTheme)
                        ) {}
                }
            }
        }

        // Горизонтальный пагер для отображения пар по дням.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(10f),
        ) { page ->
            val dateOfThisLesson = viewModel.todayDate.plusDays(page.toLong())
            val weekOfYearOfThisDay = dateOfThisLesson.getFrequencyByLocalDate()
                .changeFrequencyIfDefinedInSettings(settings = settings)

            // Фильтрация уроков по дню недели и частоте.
            val lessonsOfThisDay = lessons.filter {
                it.dayOfWeek == dateOfThisLesson.dayOfWeek &&
                        (it.frequency == null || it.frequency == weekOfYearOfThisDay) &&
                        (it.subGroup == settings.subgroup || settings.subgroup == null || it.subGroup == null)
            }.sorted()

            val lessonsInOppositeNumAndDenDay = lessons.filter {
                it.dayOfWeek == dateOfThisLesson.dayOfWeek &&
                        it.frequency == weekOfYearOfThisDay.getOpposite() &&
                        (it.subGroup == settings.subgroup || settings.subgroup == null || it.subGroup == null)
            }.sorted()

            Box {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp),
                ) {
                    if (lessonsOfThisDay.isNotEmpty()) {
                        viewModel.nowIsLesson = false

                        lessonsOfThisDay.forEach { lesson ->
                            if (lesson.nowIsLesson(localTime) && viewModel.todayDate == dateOfThisLesson) {
                                viewModel.nowIsLesson = true

                                lesson.StringForSchedule(
                                    colorBack = mainColor,
                                    dateOfThisLesson = null,
                                    viewModel = viewModel,
                                    isNoteAvailable = settings.notesAboutLesson,
                                    isNotificationsAvailable = settings.notificationsAboutLesson,
                                )
                            } else if (viewModel.todayDate == dateOfThisLesson && lessonsOfThisDay.any { it.startTime > localTime } && lesson == lessonsOfThisDay.filter { it.startTime > localTime }[0] && !viewModel.nowIsLesson) {
                                CardOfNextLesson(colorOfCard = mainColor) {
                                    lesson.StringForSchedule(
                                        paddingValues = PaddingValues(),
                                        colorBack = Theme.colors.buttonColor,
                                        dateOfThisLesson = dateOfThisLesson,
                                        viewModel = viewModel,
                                        isNoteAvailable = settings.notesAboutLesson,
                                        isNotificationsAvailable = settings.notificationsAboutLesson,
                                    )
                                }
                            } else lesson.StringForSchedule(
                                colorBack = Theme.colors.buttonColor,
                                dateOfThisLesson = dateOfThisLesson,
                                viewModel = viewModel,
                                isNoteAvailable = settings.notesAboutLesson,
                                isNotificationsAvailable = settings.notificationsAboutLesson,
                            )
                        }
                    } else WeekDay(
                        isCatShowed = settings.weekendCat,
                        viewModel = viewModel,
                        context = context,
                        modifier = Modifier.let {
                            var modifier =
                                Modifier.padding(vertical = 100.dp); if (lessonsInOppositeNumAndDenDay.isEmpty()) modifier =
                            Modifier.weight(1f); modifier
                        })

                    if (lessonsInOppositeNumAndDenDay.isNotEmpty()) {
                        TextForThisTheme(
                            text = "${stringResource(id = R.string.other_lessons_in_this_day)} ${
                                stringResource(
                                    id = weekOfYearOfThisDay.getOpposite().resourceName
                                )
                            }",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = FontSize.big22,
                        )

                        lessonsInOppositeNumAndDenDay.forEach { lesson ->
                            lesson.StringForSchedule(
                                colorBack = Theme.colors.buttonColor,
                                dateOfThisLesson = null,
                                viewModel = viewModel,
                                isNoteAvailable = settings.notesAboutLesson,
                                isNotificationsAvailable = settings.notificationsAboutLesson,
                            )
                        }
                    }
                }


            }
        }
    }
}

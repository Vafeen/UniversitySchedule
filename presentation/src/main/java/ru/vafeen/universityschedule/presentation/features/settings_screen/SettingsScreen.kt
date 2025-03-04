package ru.vafeen.universityschedule.presentation.features.settings_screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import ru.vafeen.universityschedule.domain.utils.getMainColorForThisThemeOrDefault
import ru.vafeen.universityschedule.domain.utils.getVersionName
import ru.vafeen.universityschedule.presentation.components.edit_link_dialog.EditLinkDialog
import ru.vafeen.universityschedule.presentation.components.ui_utils.CardOfSettings
import ru.vafeen.universityschedule.presentation.components.ui_utils.ColorPickerDialog
import ru.vafeen.universityschedule.presentation.components.ui_utils.FeatureOfSettings
import ru.vafeen.universityschedule.presentation.components.ui_utils.TextForThisTheme
import ru.vafeen.universityschedule.presentation.components.video.AssetsInfo
import ru.vafeen.universityschedule.presentation.components.video.GifPlayer
import ru.vafeen.universityschedule.presentation.navigation.BottomBarNavigator
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.presentation.theme.Theme
import ru.vafeen.universityschedule.presentation.utils.Link
import ru.vafeen.universityschedule.presentation.utils.getIconByRequestStatus
import ru.vafeen.universityschedule.presentation.utils.openLink
import ru.vafeen.universityschedule.presentation.utils.sendEmail
import ru.vafeen.universityschedule.presentation.utils.suitableColor
import ru.vafeen.universityschedule.resources.R

@Composable
internal fun SettingsScreen(bottomBarNavigator: BottomBarNavigator) {
    val viewModel: SettingsScreenViewModel = koinViewModel()
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val state by viewModel.state.collectAsState()

    var linkIsEditable by remember { mutableStateOf(false) }
    var colorIsEditable by remember { mutableStateOf(false) }
    var isFeaturesEditable by remember { mutableStateOf(false) }
    var isSubGroupChanging by remember { mutableStateOf(false) }
    var catsOnUIIsChanging by remember { mutableStateOf(false) }

    val subGroupLazyRowState = rememberLazyListState()

    BackHandler {
        bottomBarNavigator.back()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Заголовок экрана
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Icon(
                    painter = painterResource(
                        id = getIconByRequestStatus(
                            networkState = state.gSheetsServiceRequestStatus
                        )
                    ),
                    contentDescription = stringResource(R.string.icon_data_updating_state),
                    tint = Theme.colors.oppositeTheme
                )
                Spacer(modifier = Modifier.width(15.dp))
                TextForThisTheme(
                    text = stringResource(R.string.settings),
                    fontSize = FontSize.big22
                )
            }
        }

        // Диалоговое окно для редактирования ссылки
        if (linkIsEditable) {
            EditLinkDialog(context = context) {
                linkIsEditable = false
            }
        }

        // Диалоговое окно для изменения цвета интерфейса
        if (colorIsEditable) {
            ColorPickerDialog(
                context = context,
                firstColor = state.settings.getMainColorForThisThemeOrDefault(isDark = dark, Theme.colors.mainColor),
                onDismissRequest = { colorIsEditable = false }
            ) { color ->
                viewModel.sendEvent(SettingsScreenEvent.SaveSettingsEvent {
                    if (dark) it.copy(
                        darkThemeColor = color
                    ) else it.copy(lightThemeColor = color)
                })
            }
        }

        // Основной контент настроек
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Раздел "Общие"
            Box(modifier = Modifier.fillMaxWidth()) {
                TextForThisTheme(
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = FontSize.big22,
                    text = stringResource(R.string.general)
                )
                if (state.settings.catInSettings) {
                    GifPlayer(
                        size = 80.dp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { viewModel.sendEvent(SettingsScreenEvent.MeowEvent) },
                        imageUri = Uri.parse(AssetsInfo.FUNNY_SETTINGS_CAT)
                    )
                }
            }

            // Карточка для редактирования ссылки
            CardOfSettings(
                text = stringResource(R.string.link_to_table),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.link),
                        contentDescription = stringResource(R.string.edit_link),
                        tint = it.suitableColor()
                    )
                },
                onClick = { linkIsEditable = true }
            )

            // Карточка для просмотра таблицы
            if (state.settings.link != null) {
                CardOfSettings(
                    text = stringResource(R.string.table),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.table),
                            contentDescription = stringResource(R.string.view_table),
                            tint = it.suitableColor()
                        )
                    },
                    onClick = { state.settings.link?.let { context.openLink(link = it) } }
                )
            }

            // Подгруппа
            if (state.subGroups.isNotEmpty()) {
                CardOfSettings(
                    text = stringResource(R.string.subgroup),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.group),
                            contentDescription = stringResource(R.string.subgroup),
                            tint = it.suitableColor()
                        )
                    },
                    onClick = { isSubGroupChanging = !isSubGroupChanging },
                    additionalContentIsVisible = isSubGroupChanging,
                    additionalContent = { padding ->
                        LazyRow(
                            state = subGroupLazyRowState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = padding)
                        ) {
                            items(state.subGroups) { subgroup ->
                                AssistChip(
                                    leadingIcon = {
                                        if (subgroup == state.settings.subgroup) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.done),
                                                contentDescription = stringResource(R.string.this_is_user_subgroup),
                                                tint = Theme.colors.oppositeTheme
                                            )
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 3.dp),
                                    onClick = {
                                        viewModel.sendEvent(SettingsScreenEvent.SaveSettingsEvent {
                                            it.copy(
                                                subgroup = if (it.subgroup != subgroup) subgroup else null
                                            )
                                        })
                                    },
                                    label = { TextForThisTheme(text = subgroup) }
                                )
                            }
                        }
                    }
                )
            }

            // Карточка для настроек уведомлений
            CardOfSettings(
                text = stringResource(id = R.string.features),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.tune),
                        contentDescription = stringResource(R.string.features),
                        tint = it.suitableColor()
                    )
                },
                onClick = { isFeaturesEditable = !isFeaturesEditable },
                additionalContentIsVisible = isFeaturesEditable
            ) { padding ->
                FeatureOfSettings(
                    onClick = {
                        viewModel.sendEvent(SettingsScreenEvent.SaveSettingsEvent {
                            it.copy(notificationsAboutLesson = !it.notificationsAboutLesson)
                        })
                    },
                    padding = padding,
                    text = stringResource(R.string.notification_about_lesson_before_time),
                    checked = state.settings.notificationsAboutLesson
                )
                FeatureOfSettings(
                    onClick = {
                        viewModel.sendEvent(SettingsScreenEvent.SaveSettingsEvent {
                            it.copy(notesAboutLesson = !it.notesAboutLesson)
                        })

                    },
                    padding = padding,
                    text = stringResource(R.string.note),
                    checked = state.settings.notesAboutLesson
                )
            }

            // Раздел "Интерфейс"
            TextForThisTheme(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = FontSize.big22,
                text = stringResource(R.string.interface_str)
            )

            // Карточка для изменения цвета интерфейса
            CardOfSettings(
                text = stringResource(R.string.interface_color),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.palette),
                        contentDescription = stringResource(R.string.change_color_of_interface),
                        tint = it.suitableColor()
                    )
                },
                onClick = { colorIsEditable = true }
            )

            // Карточка для изменения отображения котиков
            CardOfSettings(
                text = stringResource(R.string.cats_on_ui),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.cat),
                        contentDescription = stringResource(R.string.cats_in_interface),
                        tint = it.suitableColor()
                    )
                },
                onClick = { catsOnUIIsChanging = !catsOnUIIsChanging },
                additionalContentIsVisible = catsOnUIIsChanging,
                additionalContent = {
                    Column {
                        FeatureOfSettings(
                            onClick = {
                                viewModel.sendEvent(SettingsScreenEvent.SaveSettingsEvent {
                                    it.copy(weekendCat = !it.weekendCat)
                                })
                            },
                            padding = it,
                            text = stringResource(R.string.weekend_cat),
                            checked = state.settings.weekendCat
                        )
                        FeatureOfSettings(
                            onClick = {
                                viewModel.sendEvent(SettingsScreenEvent.SaveSettingsEvent {
                                    it.copy(catInSettings = !it.catInSettings)
                                })
                            },
                            padding = it,
                            text = stringResource(R.string.cat_in_settings),
                            checked = state.settings.catInSettings
                        )
                    }
                }
            )

            // Раздел "Контакты"
            TextForThisTheme(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = FontSize.big22,
                text = stringResource(R.string.contacts)
            )

            // Карточка для отправки email
            CardOfSettings(
                text = stringResource(R.string.code),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.terminal),
                        contentDescription = stringResource(R.string.view_code),
                        tint = it.suitableColor()
                    )
                },
                onClick = { context.openLink(link = Link.CODE) }
            )

            // Карточка для отправки сообщения об ошибке
            CardOfSettings(
                text = stringResource(R.string.report_a_bug),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.bug_report),
                        contentDescription = stringResource(R.string.report_a_bug),
                        tint = it.suitableColor()
                    )
                }, onClick = {
                    context.sendEmail(email = Link.EMAIL)
                })
            // version
            TextForThisTheme(
                modifier = Modifier
                    .padding(10.dp)
                    .padding(bottom = 20.dp)
                    .align(Alignment.End),
                fontSize = FontSize.small17,
                text = "${stringResource(R.string.version)} ${LocalContext.current.getVersionName()}"
            )
        }
    }
}

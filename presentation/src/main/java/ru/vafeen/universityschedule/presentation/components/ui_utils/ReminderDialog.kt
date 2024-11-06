package ru.vafeen.universityschedule.presentation.components.ui_utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.vafeen.universityschedule.domain.models.Lesson
import ru.vafeen.universityschedule.domain.models.model_additions.Frequency
import ru.vafeen.universityschedule.domain.utils.generateID
import ru.vafeen.universityschedule.presentation.components.viewModels.MainScreenViewModel
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.presentation.theme.Theme
import ru.vafeen.universityschedule.presentation.utils.NotificationAboutLessonsSettings
import ru.vafeen.universityschedule.presentation.utils.createReminderAfterStartingLessonForBeCheckedAtThisLesson
import ru.vafeen.universityschedule.presentation.utils.createReminderBefore15MinutesOfLesson
import ru.vafeen.universityschedule.presentation.utils.getLessonTimeString
import java.time.LocalDate
import java.time.LocalDateTime
import ru.vafeen.universityschedule.resources.R as DR
import ru.vafeen.universityschedule.resources.R as PR

@Composable
internal fun Lesson.ReminderDialog(
    onDismissRequest: () -> Unit,
    viewModel: MainScreenViewModel,
    thisDate: LocalDate,
) {
    val context = LocalContext.current

    suspend fun generateID(): Int = viewModel.remindersFlow.first().map {
        it.idOfReminder
    }.generateID()

    val checkBoxColor = CheckboxDefaults.colors(
        checkedColor = Theme.colors.oppositeTheme,
        checkmarkColor = Theme.colors.singleTheme,
        uncheckedColor = Theme.colors.oppositeTheme
    )
    DefaultDialog(onDismissRequest = onDismissRequest) { dp ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Theme.colors.buttonColor)
                .padding(dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            this@ReminderDialog.name?.let {
                TextForThisTheme(
                    text = this@ReminderDialog.name ?: undefined,
                    fontSize = FontSize.medium19
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
            Row {
                Icon(
                    imageVector = ImageVector.vectorResource(id = DR.drawable.schedule),
                    contentDescription = "Icon schedule",
                    tint = Theme.colors.oppositeTheme
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextForThisTheme(
                    text = getLessonTimeString(),
                    fontSize = FontSize.small17,
                )
            }

            Spacer(modifier = Modifier.height(5.dp))
            TextForThisTheme(
                text = stringResource(id = if (frequency == Frequency.Every || frequency == null) DR.string.every_week else DR.string.every_2_weeks),
                fontSize = FontSize.medium19
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = DR.drawable.message),
                        contentDescription = "Message about lesson",
                        tint = Theme.colors.oppositeTheme
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    TextForThisTheme(
                        text = stringResource(id = PR.string.notification_about_lesson_before_time),
                        fontSize = FontSize.medium19
                    )
                }
                Checkbox(
                    checked = idOfReminderBeforeLesson != null, onCheckedChange = {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (idOfReminderBeforeLesson == null) {
                                val idOfNewReminder = generateID()
                                val newReminder =
                                    this@ReminderDialog.createReminderBefore15MinutesOfLesson(
                                        idOfNewReminder = idOfNewReminder,
                                        dt = LocalDateTime.of(
                                            thisDate,
                                            this@ReminderDialog.startTime.minusMinutes(
                                                NotificationAboutLessonsSettings.MINUTES_BEFORE_LESSON_FOR_NOTIFICATION
                                            )
                                        ),
                                        context = context,
                                    )
                                viewModel.addReminderAbout15MinutesBeforeLessonAndUpdateLocalDB(
                                    lesson = this@ReminderDialog,
                                    newReminder = newReminder
                                )
                            } else {
                                viewModel.removeReminderAbout15MinutesBeforeLessonAndUpdateLocalDB(
                                    lesson = this@ReminderDialog
                                )
                            }
                        }

                    }, colors = checkBoxColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        painter = painterResource(id = DR.drawable.notification_about_checking),
                        contentDescription = "Message about checking on this lesson",
                        tint = Theme.colors.oppositeTheme
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    TextForThisTheme(
                        text =
                        stringResource(id = PR.string.notification_about_lesson_after_starting),
                        fontSize = FontSize.medium19
                    )
                }
                Checkbox(
                    checked = idOfReminderAfterBeginningLesson != null, onCheckedChange = {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (idOfReminderAfterBeginningLesson == null) {
                                val idOfNewReminder = generateID()
                                val newReminder =
                                    this@ReminderDialog.createReminderAfterStartingLessonForBeCheckedAtThisLesson(
                                        idOfNewReminder = idOfNewReminder,
                                        dt = LocalDateTime.of(
                                            thisDate,
                                            this@ReminderDialog.startTime
                                        ), context = context
                                    )
                                viewModel.addReminderAboutCheckingOnLessonAndUpdateLocalDB(
                                    lesson = this@ReminderDialog,
                                    newReminder = newReminder
                                )
                            } else {
                                viewModel.removeReminderAboutCheckingOnLessonAndUpdateLocalDB(
                                    lesson = this@ReminderDialog
                                )
                            }
                        }
                    }, colors = checkBoxColor
                )
            }
        }
    }
}
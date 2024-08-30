package ru.vafeen.universityschedule.ui.components.ui_utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vafeen.universityschedule.R
import ru.vafeen.universityschedule.database.entity.Lesson
import ru.vafeen.universityschedule.ui.theme.FontSize
import ru.vafeen.universityschedule.utils.suitableColor
import java.time.LocalTime

@Composable
fun Lesson.StringForSchedule(
    colorBack: Color,
    lessonOfThisNumAndDenOrNot: Boolean = true,
    padding: Dp = 10.dp,
) {
    Card(
        modifier = Modifier
            .padding(padding)//.clickable {  }
            .fillMaxWidth()
            .alpha(
                if (lessonOfThisNumAndDenOrNot) 1.0f
                else 0.55f
            ),
        colors = CardDefaults.cardColors(
            containerColor = colorBack
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.schedule),
                        contentDescription = "Icon schedule",
                        tint = colorBack.suitableColor()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getLessonTimeString(), fontSize = FontSize.small17,
                        color = colorBack.suitableColor()
                    )
                }
                if (classroom?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Row {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Icon classroom",
                            tint = colorBack.suitableColor()
                        )

                        Text(
                            text = classroom,
                            color = colorBack.suitableColor(),
                            fontSize = FontSize.small17
                        )
                    }
                }
            }

            if (name?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = name, color = colorBack.suitableColor(),
                    fontSize = FontSize.big22
                )
            }

            if (teacher?.isNotEmpty() == true)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = "Icon teacher",
                        tint = colorBack.suitableColor()
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = teacher.replace(oldValue = " ", newValue = "\n"),
                        fontSize = FontSize.small17,
                        color = colorBack.suitableColor(),
                        maxLines = 3
                    )
                }

            if (subGroup?.isNotEmpty() == true)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.group),
                        contentDescription = "Icon subgroup",
                        tint = colorBack.suitableColor()
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = subGroup,
                        fontSize = FontSize.small17,
                        color = colorBack.suitableColor(),
                        maxLines = 3
                    )
                }
        }
    }
}


fun LocalTime.toLessonTime(): String = "${hour}:" + if (minute < 10) "0${minute}" else "$minute"


fun Lesson.getLessonTimeString(): String =
    this.let { "${it.startTime.toLessonTime()} - ${it.endTime.toLessonTime()}" }
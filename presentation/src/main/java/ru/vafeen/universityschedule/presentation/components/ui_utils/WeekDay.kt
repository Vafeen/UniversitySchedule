package ru.vafeen.universityschedule.presentation.components.ui_utils

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.vafeen.universityschedule.presentation.components.video.AssetsInfo
import ru.vafeen.universityschedule.presentation.components.video.GifPlayer
import ru.vafeen.universityschedule.presentation.components.viewModels.MainScreenViewModel
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.resources.R

@Composable
internal fun WeekDay(
    isCatShowed: Boolean,
    context: Context, modifier: Modifier = Modifier, viewModel: MainScreenViewModel
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        TextForThisTheme(
            text = context.getString(R.string.holiday),
            fontSize = FontSize.huge27,
            modifier = Modifier
        )
        // Отображение GIF-картинки, если нет пар и выходной день.
        if (isCatShowed) {
            GifPlayer(
                size = 150.dp,
                modifier = Modifier
                    .clickable { viewModel.meow() },
                imageUri = Uri.parse(AssetsInfo.DANCING_CAT)
            )
        }
    }
}
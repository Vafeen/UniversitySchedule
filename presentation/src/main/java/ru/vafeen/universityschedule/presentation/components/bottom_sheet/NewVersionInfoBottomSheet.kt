package ru.vafeen.universityschedule.presentation.components.bottom_sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.vafeen.universityschedule.presentation.components.ui_utils.TextForThisTheme
import ru.vafeen.universityschedule.presentation.main.MainActivityViewModel
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.presentation.theme.Theme

/**
 * Компонент нижнего модального окна для отображения информации о новой версии приложения.
 *
 * Этот компонент отображает информацию о новой версии, полученную из настроек,
 * в виде нижнего модального окна. Пользователь может закрыть это окно по своему усмотрению.
 *
 * @param viewModel ViewModel, содержащий данные о настройках приложения.
 * @param onDismissRequest Функция, вызываемая при закрытии модального окна.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewVersionInfoBottomSheet(
    viewModel: MainActivityViewModel,
    onDismissRequest: () -> Unit,
) {
    // Создание состояния модального нижнего окна.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.state.collectAsState()
    // Отображение модального нижнего окна.
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Theme.colors.buttonColor,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()) // Добавление прокрутки для содержимого.
                .padding(15.dp) // Установка отступов.
        ) {
            TextForThisTheme(
                modifier = Modifier.fillMaxWidth(),
                text = state.settings.releaseBody, // Текст информации о новой версии.
                fontSize = FontSize.big22 // Размер шрифта для текста.
            )
        }
    }
}
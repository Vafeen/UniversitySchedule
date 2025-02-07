package ru.vafeen.universityschedule.presentation.components.edit_link_dialog

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.androidx.compose.koinViewModel
import ru.vafeen.universityschedule.presentation.components.ui_utils.TextForThisTheme
import ru.vafeen.universityschedule.presentation.theme.FontSize
import ru.vafeen.universityschedule.presentation.theme.Theme
import ru.vafeen.universityschedule.presentation.utils.Link
import ru.vafeen.universityschedule.presentation.utils.copyTextToClipBoard
import ru.vafeen.universityschedule.presentation.utils.pasteText
import ru.vafeen.universityschedule.resources.R

@Composable
internal fun EditLinkDialog(
    context: Context,
    onDismissRequest: () -> Unit,
) {
    val viewModel = koinViewModel<EditLinkDialogViewModel>()

    val settings by viewModel.settings.collectAsState()
    val iconsSize = 30.dp
    Dialog(
        onDismissRequest = { onDismissRequest() }, properties = DialogProperties()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Theme.colors.singleTheme
            ),
            border = BorderStroke(width = 2.dp, color = Theme.colors.oppositeTheme)
        ) {
            Column(
                modifier = Modifier
                    .height(250.dp), verticalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .align(Alignment.End),
                    onClick = { onDismissRequest() },
                ) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = stringResource(R.string.close),
                        tint = Theme.colors.oppositeTheme
                    )
                }

                TextForThisTheme(
                    text = settings.link ?: stringResource(id = R.string.no_link),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .weight(1f)
                        .padding(horizontal = 5.dp),
                    fontSize = FontSize.big22
                )


                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.link != null) {
                        IconButton(
                            onClick = {
                                settings.link?.let {
                                    context.copyTextToClipBoard(
                                        label = "link",
                                        text = it
                                    )
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(iconsSize),
                                painter = painterResource(id = R.drawable.copy),
                                contentDescription = stringResource(R.string.copy),
                                tint = Theme.colors.oppositeTheme
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.saveSettingsToSharedPreferences { s ->
                                    s.copy(link = null)
                                }
                                onDismissRequest()
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(iconsSize),
                                painter = painterResource(id = R.drawable.drop),
                                contentDescription = stringResource(R.string.clear),
                                tint = Theme.colors.oppositeTheme
                            )
                        }
                    }
                    IconButton(onClick = {
                        context.pasteText()?.let {
                            val contains = it.contains("docs.google.com/spreadsheets/")
                            if (contains) {
                                viewModel.saveSettingsToSharedPreferences { s ->
                                    s.copy(
                                        link = if (!it.contains(Link.PROTOCOL))
                                            "${Link.PROTOCOL}$it" else it
                                    )
                                }
                                onDismissRequest()
                            } else
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.its_no_google_sheets_link),
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                    }) {
                        Icon(
                            modifier = Modifier.size(iconsSize),
                            painter = painterResource(id = R.drawable.paste),
                            contentDescription = stringResource(R.string.paste),
                            tint = Theme.colors.oppositeTheme
                        )
                    }
                }

            }
        }
    }
}

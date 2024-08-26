package ru.vafeen.universityschedule.ui.components.ui_utils

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ru.vafeen.universityschedule.network.parcelable.github_service.Release
import ru.vafeen.universityschedule.network.repository.NetworkRepository
import ru.vafeen.universityschedule.ui.components.bottom_sheet.UpdaterBottomSheet
import ru.vafeen.universityschedule.utils.getVersionName


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckUpdateAndOpenBottomSheetIfNeed(
    networkRepository: NetworkRepository,
    onDismissRequest: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val versionName = getVersionName(context = context)
    val bottomSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isUpdateNeeded by remember {
        mutableStateOf(false)
    }
    var release: Release? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = null) {
        release = networkRepository.getLatestRelease()?.body()
        if (release != null && versionName != null &&
            release?.tag_name?.substringAfter("v") != versionName
        )
            isUpdateNeeded = true
    }

    if (isUpdateNeeded)
        release?.let { releaseParam ->
            UpdaterBottomSheet(
                networkRepository = networkRepository,
                release = releaseParam,
                state = bottomSheetState,
            ) {
                isUpdateNeeded = false
                onDismissRequest(it)
            }
        }
}
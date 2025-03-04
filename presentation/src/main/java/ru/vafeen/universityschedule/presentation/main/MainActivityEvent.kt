package ru.vafeen.universityschedule.presentation.main

import ru.vafeen.universityschedule.domain.models.Settings

sealed class MainActivityEvent {
    data object CheckUpdates : MainActivityEvent()
    data object UpdateApp : MainActivityEvent()
    data object ClearReleaseData : MainActivityEvent()
    data class SaveSettingsEvent(val saving: (Settings) -> Settings) : MainActivityEvent()

}
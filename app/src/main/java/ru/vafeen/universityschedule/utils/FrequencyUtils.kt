package ru.vafeen.universityschedule.utils

import android.util.Log
import ru.vafeen.universityschedule.noui.lesson_additions.Frequency
import ru.vafeen.universityschedule.ui.components.Settings

fun Frequency.changeFrequencyIfDefinedInSettings(settings: Settings): Frequency {
    Log.d(
        "settings",
        "settings.isSelectedFrequencyCorrespondsToTheWeekNumbers = ${settings.isSelectedFrequencyCorrespondsToTheWeekNumbers}"
    )
    return if (settings.isSelectedFrequencyCorrespondsToTheWeekNumbers != false)
        this
    else this.getOpposite()
}
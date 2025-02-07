package ru.vafeen.universityschedule.presentation.components.edit_link_dialog

import androidx.lifecycle.ViewModel
import ru.vafeen.universityschedule.domain.models.Settings
import ru.vafeen.universityschedule.domain.network.service.SettingsManager


internal class EditLinkDialogViewModel(private val settingsManager: SettingsManager) : ViewModel() {
    val settings = settingsManager.settingsFlow

    /**
     * Сохраняет изменения настроек в SharedPreferences.
     *
     * @param saving Функция, изменяющая текущие настройки.
     */
    fun saveSettingsToSharedPreferences(saving: (Settings) -> Settings) {
        settingsManager.save(saving)
    }
}
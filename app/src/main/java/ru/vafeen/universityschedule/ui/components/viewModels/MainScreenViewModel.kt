package ru.vafeen.universityschedule.ui.components.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.vafeen.universityschedule.R
import ru.vafeen.universityschedule.database.DatabaseRepository
import ru.vafeen.universityschedule.database.entity.Lesson
import ru.vafeen.universityschedule.network.service.GSheetsService
import ru.vafeen.universityschedule.utils.GSheetsServiceRequestStatus
import ru.vafeen.universityschedule.utils.SharedPreferences
import ru.vafeen.universityschedule.utils.changeFrequencyIfDefinedInSettings
import ru.vafeen.universityschedule.utils.createGSheetsService
import ru.vafeen.universityschedule.utils.getFrequencyByLocalDate
import ru.vafeen.universityschedule.utils.getLessonsListFromGSheetsTable
import ru.vafeen.universityschedule.utils.getSettingsOrCreateIfNull
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject


class MainScreenViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    val sharedPreferences: SharedPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val ruDaysOfWeek =
        context.let {
            listOf(
                it.getString(R.string.monday),
                it.getString(R.string.tuesday),
                it.getString(R.string.wednesday),
                it.getString(R.string.thursday),
                it.getString(R.string.friday),
                it.getString(R.string.satudray),
                it.getString(R.string.sunday)
            )
        }
    var settings = sharedPreferences.getSettingsOrCreateIfNull()
    val todayDate: LocalDate = LocalDate.now()
    val daysOfWeek = DayOfWeek.entries.toList()
    val weekOfYear = todayDate.getFrequencyByLocalDate()
        .changeFrequencyIfDefinedInSettings(settings = settings)
    var nowIsLesson: Boolean = false
    private var gSheetsService: GSheetsService? =
        settings.link?.let { createGSheetsService(link = it) }


    fun updateLocalDatabase(updateUICallback: (List<Lesson>, GSheetsServiceRequestStatus) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val lastLessons = databaseRepository.getAllAsFlowLessons().first()
            withContext(Dispatchers.Main) {
                updateUICallback(
                    lastLessons,
                    if (settings.link?.isNotEmpty() == true) GSheetsServiceRequestStatus.Waiting
                    else GSheetsServiceRequestStatus.NoLink
                )
            }
            if (settings.link?.isNotEmpty() == true)
                try {
                    gSheetsService?.getLessonsListFromGSheetsTable()
                        ?.let {
                            databaseRepository.apply {
                                deleteAllLessons(*lastLessons.toTypedArray())
                                insertAllLessons(*it.toTypedArray())
                            }
                            withContext(Dispatchers.Main) {
                                updateUICallback(it, GSheetsServiceRequestStatus.Success)
                            }
                        }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        updateUICallback(lastLessons, GSheetsServiceRequestStatus.NetworkError)
                    }
                }
        }
    }
}
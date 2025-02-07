package ru.vafeen.universityschedule.data.di

import androidx.room.Room
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.vafeen.universityschedule.data.database.AppDatabase
import ru.vafeen.universityschedule.data.database.AppDatabaseMigrationManager
import ru.vafeen.universityschedule.data.impl.database.LessonRepositoryImpl
import ru.vafeen.universityschedule.data.impl.database.ReminderRepositoryImpl
import ru.vafeen.universityschedule.data.impl.network.end_points.GitHubDataServiceLink
import ru.vafeen.universityschedule.data.impl.network.repository.ReleaseRepositoryImpl
import ru.vafeen.universityschedule.data.impl.network.repository.SheetDataRepositoryImpl
import ru.vafeen.universityschedule.data.impl.network.service.RefresherImpl
import ru.vafeen.universityschedule.data.impl.network.service.SettingsManagerImpl
import ru.vafeen.universityschedule.data.impl.notifications.NotificationBuilderImpl
import ru.vafeen.universityschedule.data.impl.notifications.NotificationServiceImpl
import ru.vafeen.universityschedule.data.impl.scheduler.SchedulerAPIMigrationManagerImpl
import ru.vafeen.universityschedule.data.impl.scheduler.SchedulerImpl
import ru.vafeen.universityschedule.data.network.service.GitHubDataService
import ru.vafeen.universityschedule.data.network.service.GoogleSheetsService
import ru.vafeen.universityschedule.domain.database.LessonRepository
import ru.vafeen.universityschedule.domain.database.ReminderRepository
import ru.vafeen.universityschedule.domain.network.GoogleSheetsServiceLink
import ru.vafeen.universityschedule.domain.network.repository.SheetDataRepository
import ru.vafeen.universityschedule.domain.network.service.Refresher
import ru.vafeen.universityschedule.domain.network.service.ReleaseRepository
import ru.vafeen.universityschedule.domain.network.service.SettingsManager
import ru.vafeen.universityschedule.domain.notifications.NotificationBuilder
import ru.vafeen.universityschedule.domain.notifications.NotificationService
import ru.vafeen.universityschedule.domain.scheduler.Scheduler
import ru.vafeen.universityschedule.domain.scheduler.SchedulerAPIMigrationManager

internal val databaseModuleImpl = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            context = get(), klass = AppDatabase::class.java, name = "universityScheduleDB.db"
        ).addMigrations(*AppDatabaseMigrationManager().migrations()).build()
    }
    singleOf(::LessonRepositoryImpl) {
        bind<LessonRepository>()
    }
    singleOf(::ReminderRepositoryImpl) {
        bind<ReminderRepository>()
    }
}

internal val networkRepositoryModuleImpl = module {
    singleOf(::SheetDataRepositoryImpl) {
        bind<SheetDataRepository>()
    }
    singleOf(::ReleaseRepositoryImpl) {
        bind<ReleaseRepository>()
    }
}

internal val networkServiceModuleImpl = module {
    single<GitHubDataService> {
        Retrofit.Builder()
            .baseUrl(GitHubDataServiceLink.BASE_LINK)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GitHubDataService::class.java)
    }
    single<GoogleSheetsService> {
        Retrofit.Builder()
            .baseUrl(GoogleSheetsServiceLink.BASE_URL)
            .build().create(GoogleSheetsService::class.java)
    }
}

internal val servicesModuleImpl = module {
    singleOf(::NotificationServiceImpl) {
        bind<NotificationService>()
    }
    singleOf(::NotificationBuilderImpl) {
        bind<NotificationBuilder>()
    }
    singleOf(::SchedulerImpl) {
        bind<Scheduler>()
    }
    singleOf(::SchedulerAPIMigrationManagerImpl) {
        bind<SchedulerAPIMigrationManager>()
    }
    singleOf(::SettingsManagerImpl) {
        bind<SettingsManager>()
    }
    singleOf(::RefresherImpl) {
        bind<Refresher>()
    }
}
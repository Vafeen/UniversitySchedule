package ru.vafeen.universityschedule.data.impl.network.service

import android.content.Context
import android.vafeen.direct_refresher.DirectRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import ru.vafeen.universityschedule.data.converters.DownloadStatusConverter
import ru.vafeen.universityschedule.data.impl.network.end_points.DownloadServiceLink
import ru.vafeen.universityschedule.domain.network.service.Refresher
import android.vafeen.direct_refresher.refresher.Refresher as LibRefresher

internal class RefresherImpl(
    context: Context,
    private val downloadStatusConverter: DownloadStatusConverter
) :
    Refresher {
    private val libRefresher: LibRefresher =
        DirectRefresher.provideRefresher(
            context = context,
            downloader = DirectRefresher.provideDownloader(
                context = context,
                baseUrl = DownloadServiceLink.BASE_LINK
            ),
            installer = DirectRefresher.provideInstaller(context = context)
        )
    override val progressFlow =
        libRefresher.progressFlow.map { downloadStatusConverter.convertAB(it) }

    override suspend fun refresh(
        coroutineScope: CoroutineScope,
        url: String,
        downloadedFileName: String
    ) = libRefresher.refresh(coroutineScope, url, downloadedFileName)

}
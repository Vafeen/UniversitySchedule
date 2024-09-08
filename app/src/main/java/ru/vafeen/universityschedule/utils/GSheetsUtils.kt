package ru.vafeen.universityschedule.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemSecurityUpdateGood
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.graphics.vector.ImageVector
import retrofit2.Retrofit
import ru.vafeen.universityschedule.network.service.GSheetsService

fun createGSheetsService(link: String): GSheetsService? {
    if (link.isEmpty()) return null
    return try {
        Retrofit.Builder().baseUrl(link.substringBefore("edit?")).build()
            .create(GSheetsService::class.java)
    } catch (e: Exception) {
        null
    }
}

fun getIconByRequestStatus(networkState: GSheetsServiceRequestStatus): ImageVector =
    when (networkState) {
        GSheetsServiceRequestStatus.Waiting -> {
            Icons.Filled.Sync
        }

        GSheetsServiceRequestStatus.Success -> {
            Icons.Filled.SystemSecurityUpdateGood
        }

        GSheetsServiceRequestStatus.NetworkError -> {
            Icons.Filled.WifiOff
        }

        GSheetsServiceRequestStatus.NoLink -> {
            Icons.Filled.LinkOff
        }
    }




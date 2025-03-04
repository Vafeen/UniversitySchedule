package ru.vafeen.universityschedule.presentation.main

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.vafeen.universityschedule.presentation.components.permissions.RequestNotificationPermission
import ru.vafeen.universityschedule.presentation.navigation.NavigationRoot
import ru.vafeen.universityschedule.presentation.theme.MainTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModel()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainTheme {
                viewModel.navController = rememberNavController()
                RequestNotificationPermission()
                NavigationRoot(viewModel = viewModel)
            }
        }
    }
}

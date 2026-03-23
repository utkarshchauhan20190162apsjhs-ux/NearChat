package com.nearchat.ui.navigation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nearchat.NearChatApp
import com.nearchat.service.NearChatService
import com.nearchat.ui.screens.ChatScreen
import com.nearchat.ui.screens.HomeScreen
import com.nearchat.ui.screens.ProfileScreen
import com.nearchat.viewmodel.NearChatViewModelFactory

private object Destinations {
    const val Home = "home"
    const val Profile = "profile"
    const val Chat = "chat/{userId}/{name}"
}

@Composable
fun NearChatNavGraph(app: NearChatApp, context: Context) {
    val navController = rememberNavController()
    var coordinator by remember { mutableStateOf<com.nearchat.connectivity.ConnectivityCoordinator?>(null) }

    DisposableEffect(Unit) {
        val serviceIntent = Intent(context, NearChatService::class.java)
        context.startForegroundService(serviceIntent)

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                coordinator = (binder as NearChatService.LocalBinder).getService().coordinator
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                coordinator = null
            }
        }

        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(connection)
        }
    }

    coordinator?.let { connectionCoordinator ->
        NavHost(navController = navController, startDestination = Destinations.Home) {
            composable(
                Destinations.Home,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350)) }
            ) {
                val vm: com.nearchat.viewmodel.HomeViewModel = viewModel(
                    factory = NearChatViewModelFactory(app.chatRepository, app.profileRepository, connectionCoordinator)
                )
                HomeScreen(
                    viewModel = vm,
                    onOpenChat = { userId, name -> navController.navigate("chat/$userId/$name") },
                    onOpenProfile = { navController.navigate(Destinations.Profile) }
                )
            }

            composable(
                route = Destinations.Chat,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStack ->
                val userId = backStack.arguments?.getString("userId").orEmpty()
                val name = backStack.arguments?.getString("name").orEmpty()
                val vm: com.nearchat.viewmodel.ChatViewModel = viewModel(
                    factory = NearChatViewModelFactory(
                        app.chatRepository,
                        app.profileRepository,
                        connectionCoordinator,
                        remoteUserId = userId
                    )
                )
                ChatScreen(vm, name = name, onBack = { navController.popBackStack() })
            }

            composable(Destinations.Profile) {
                val vm: com.nearchat.viewmodel.ProfileViewModel = viewModel(
                    factory = NearChatViewModelFactory(app.chatRepository, app.profileRepository, connectionCoordinator)
                )
                ProfileScreen(vm, onBack = { navController.popBackStack() })
            }
        }
    }
}

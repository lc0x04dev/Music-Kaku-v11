package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MusicPlayerApp()
      }
    }
  }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Player : Screen("player", "Canción", Icons.Default.MusicNote)
    object Playlist : Screen("playlists", "Lista", Icons.Default.FeaturedPlayList)
    object Search : Screen("search", "Búsqueda", Icons.Default.Search)
}

@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val musicViewModel: MusicViewModel = viewModel()
    val currentSong by musicViewModel.currentSong.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        Screen.Home,
        Screen.Player,
        Screen.Playlist,
        Screen.Search
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .testTag("app_bottom_navigation"),
                tonalElevation = 12.dp
            ) {
                navigationItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.testTag("nav_icon_${screen.route}")
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                modifier = Modifier.testTag("nav_label_${screen.route}")
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = musicViewModel,
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
                    )
                }
                composable(Screen.Player.route) {
                    PlayerScreen(
                        viewModel = musicViewModel,
                        onMinimize = { navController.navigate(Screen.Home.route) }
                    )
                }
                composable(Screen.Playlist.route) {
                    PlaylistsScreen(
                        viewModel = musicViewModel,
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = musicViewModel,
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) }
                    )
                }
            }

            // FLOATING MINI PLAYER SELECTION
            // Appears if there's a selected song and the user is NOT in the fullscreen Player.
            val showMiniPlayer = currentSong != null && currentRoute != Screen.Player.route

            AnimatedVisibility(
                visible = showMiniPlayer,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                MiniPlayer(
                    viewModel = musicViewModel,
                    onClick = {
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

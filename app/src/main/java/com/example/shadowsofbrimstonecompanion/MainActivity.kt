package com.example.shadowsofbrimstonecompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shadowsofbrimstonecompanion.ui.CharacterCreateScreen
import com.example.shadowsofbrimstonecompanion.ui.CharacterDetailScreen
import com.example.shadowsofbrimstonecompanion.ui.CharacterListScreen
import com.example.shadowsofbrimstonecompanion.ui.navigation.NavigationRoutes
import com.example.shadowsofbrimstonecompanion.ui.theme.BrimstoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrimstoneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = NavigationRoutes.CHARACTER_LIST
                    ) {
                        // Character list screen
                        composable(NavigationRoutes.CHARACTER_LIST) {
                            CharacterListScreen(
                                onNavigateToCreateCharacter = {
                                    navController.navigate(NavigationRoutes.CHARACTER_CREATE)
                                },
                                onNavigateToCharacterDetail = { characterId ->
                                    navController.navigate(NavigationRoutes.characterDetailRoute(characterId))
                                }
                            )
                        }

                        // Character detail screen
                        composable(
                            route = "${NavigationRoutes.CHARACTER_DETAIL}/{characterId}",
                            arguments = listOf(
                                navArgument("characterId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val characterId = backStackEntry.arguments?.getLong("characterId") ?: -1L
                            CharacterDetailScreen(
                                characterId = characterId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Character creation screen
                        composable(NavigationRoutes.CHARACTER_CREATE) {
                            CharacterCreateScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onCharacterCreated = { characterId ->
                                    // Navigate to the detail screen and clear backstack to list
                                    navController.navigate(NavigationRoutes.characterDetailRoute(characterId)) {
                                        popUpTo(NavigationRoutes.CHARACTER_LIST)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
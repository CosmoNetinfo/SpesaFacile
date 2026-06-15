package com.example.spesafacile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.spesafacile.ui.screens.ListsScreen
import com.example.spesafacile.ui.screens.ShoppingScreen

@Composable
fun SpesaFacileNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "lists"
    ) {
        composable("lists") {
            ListsScreen(
                onListClick = { listId ->
                    navController.navigate("shopping/$listId")
                }
            )
        }

        composable(
            route = "shopping/{listId}",
            arguments = listOf(
                navArgument("listId") { type = NavType.IntType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "spesafacile://shopping/{listId}" }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getInt("listId") ?: return@composable
            ShoppingScreen(
                listId = listId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

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
import com.example.spesafacile.ui.screens.AppHomeShell
import com.example.spesafacile.ui.screens.SharedListScreen

@Composable
fun SpesaFacileNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "lists"
    ) {
        composable("lists") {
            AppHomeShell(
                onListClick = { listId ->
                    navController.navigate("shopping/$listId")
                }
            )
        }

        composable(
            route = "import?data={data}",
            arguments = listOf(navArgument("data") { type = NavType.StringType; defaultValue = "" }),
            deepLinks = listOf(navDeepLink { uriPattern = "spesafacile://import?data={data}" })
        ) { entry ->
            SharedListScreen(entry.arguments?.getString("data").orEmpty(),
                onOpen = { id -> navController.navigate("shopping/$id") { popUpTo("lists") } },
                onCancel = { navController.navigate("lists") { popUpTo("lists") { inclusive = true } } })
        }

        composable(
            route = "shopping/{listId}?add={add}",
            arguments = listOf(
                navArgument("listId") { type = NavType.IntType },
                navArgument("add") { type = NavType.BoolType; defaultValue = false }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "spesafacile://shopping/{listId}?add={add}" }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getInt("listId") ?: return@composable
            ShoppingScreen(
                listId = listId,
                focusAdd = backStackEntry.arguments?.getBoolean("add") == true,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

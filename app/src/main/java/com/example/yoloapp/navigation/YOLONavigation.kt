package com.example.yoloapp.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.yoloapp.ui.screens.GeneratorScreen
import com.example.yoloapp.ui.screens.HomeScreen
import com.example.yoloapp.ui.screens.LoginScreen
import com.example.yoloapp.ui.screens.OCRScreen
import com.example.yoloapp.ui.screens.PhotoPreviewScreen
import com.example.yoloapp.ui.screens.RegisterScreen
import com.example.yoloapp.ui.screens.ResetPasswordScreen
import com.example.yoloapp.ui.screens.WelcomeScreen
import com.example.yoloapp.utils.AuthManager
import com.example.yoloapp.utils.FireStoreManager
import com.example.yoloapp.utils.currentRoute
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun YOLONavigation(navController: NavHostController, authManager: AuthManager) {


    val actualRoute = currentRoute(navController)
    val activity = LocalContext.current as? Activity
    val isUserLoggedIn = authManager.isUserLoggedIn()

    val fireStoreManager = remember {
        FireStoreManager(
            authManager = authManager,
            firestore = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }


    BackHandler(
        enabled = actualRoute in listOf(
            "homeScreen",
            "loginScreen",
            "welcomeScreen"
        ) || actualRoute?.startsWith("generatorScreen") == true
    ) {
        if (actualRoute?.startsWith("generatorScreen") == true) {
            navController.navigate("homeScreen") {
                popUpTo("homeScreen") { inclusive = true }
            }
        } else {
            activity?.moveTaskToBack(true)
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) "homeScreen" else "welcomeScreen",
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = "welcomeScreen") {
            WelcomeScreen(navController)
        }

        composable(route = "loginScreen") {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = { navController.navigate("homeScreen") },
                onRegisterClick = { navController.navigate("registerScreen") },
                onResetPasswordClick = { navController.navigate("resetPasswordScreen") }
            )
        }

        composable(route = "registerScreen") {
            RegisterScreen(
                authManager = authManager,
                onRegisterSuccess = { navController.navigate("homeScreen") },
                onLoginClick = { navController.navigate("loginScreen") }
            )
        }

        composable(route = "resetPasswordScreen") {
            ResetPasswordScreen(
                authManager = authManager,
                onPasswordResetSent = { navController.navigate("loginScreen") },
                onLoginClick = { navController.navigate("loginScreen") }
            )
        }


        composable(route = "homeScreen") {
            HomeScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(route = "ocrScreen") {
            OCRScreen(navController)
        }

        composable(route = "generatorScreen") {
            GeneratorScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager
            )
        }

        composable(
            route = "generatorScreen/{chatId}",
            arguments = listOf(navArgument("chatId") { nullable = true })
        ) {
            if (it.arguments?.getString("chatId") == null) {
                navController.navigate("generatorScreen")
            }
            val chatId = it.arguments?.getString("chatId")
            GeneratorScreen(
                navController = navController,
                authManager = authManager,
                fireStoreManager = fireStoreManager,
                chatId = chatId
            )
        }


        composable(
            route = "photoPreviewScreen/{imagePath}/{ocrText}",
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType },
                navArgument("ocrText") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath")
            val ocrText = backStackEntry.arguments?.getString("ocrText")
            PhotoPreviewScreen(navController, imagePath, ocrText, fireStoreManager)
        }

    }
}


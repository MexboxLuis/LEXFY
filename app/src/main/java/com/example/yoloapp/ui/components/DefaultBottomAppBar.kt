package com.example.yoloapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.example.yoloapp.utils.currentRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DefaultBottomAppBar(
    navController: NavHostController,
) {

    val actualRoute = currentRoute(navController)
    val cameraPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }


    BottomAppBar {
        NavigationBar {
            NavigationBarItem(
                selected = actualRoute == "homeScreen",
                onClick = {
                    if (actualRoute != "homeScreen")
                        navController.navigate("homeScreen")
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null
                    )
                },
                label = { Text("Home") }
            )
            NavigationBarItem(
                selected = actualRoute == "ocrScreen",
                onClick = {
                    if (!cameraPermissionState.status.isGranted) {
                        cameraPermissionState.launchPermissionRequest()
                    } else {
                        if (actualRoute != "ocrScreen")
                            navController.navigate("ocrScreen")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TextSnippet,
                        contentDescription = null
                    )
                },
                label = { Text("OCR") }
            )
            if (actualRoute != null) {
                NavigationBarItem(
                    selected = actualRoute.startsWith("generatorScreen"),
                    onClick = {
                        if (!actualRoute.startsWith("generatorScreen"))
                            navController.navigate("generatorScreen")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.StarHalf,
                            contentDescription = null
                        )
                    },
                    label = { Text("Generator") }
                )
            }
        }
    }
}
package com.example.yoloapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackScaffold(
    navController: NavHostController,
    screenName: String,
    screenNavigationName: String? = null,
    actionsIcon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },

                title = {
                    Text(
                        screenName,
                        modifier = Modifier.padding(start = 10.dp),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Thin
                    )
                },
                actions = {
                    if (actionsIcon != null && screenNavigationName != null) {
                        IconButton(
                            onClick = {
                                navController.navigate(screenName)
                            }
                        ) {
                            Icon(
                                imageVector = actionsIcon,
                                contentDescription = null,
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }

    }
}
package com.example.yoloapp.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.yoloapp.data.ChatData
import com.example.yoloapp.utils.AuthManager
import com.example.yoloapp.utils.FireStoreManager
import com.example.yoloapp.utils.currentRoute
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@Composable
fun MainScaffold(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager,
    content: @Composable () -> Unit
) {
    val actualRoute = currentRoute(navController = navController)
    var logoutAdvice by rememberSaveable { mutableStateOf(false) }
    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val email = authManager.getCurrentUser().getOrNull()?.email
    val groupedChats =
        remember { mutableStateOf<List<Pair<String, List<Pair<String, ChatData>>>>>(emptyList()) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LaunchedEffect(email) {
                if (email != null) {
                    val chats = fireStoreManager.getChats(email)
                    val now = Timestamp.now().toDate().time

                    val categorizedChats = chats.groupBy { (_, chat) ->
                        val lastModifiedTime = chat.lastModifiedAt.toDate().time
                        val hoursDifference = (now - lastModifiedTime) / (1000 * 60 * 60)

                        when {
                            hoursDifference < 24 -> "Today"
                            hoursDifference in 24..48 -> "Yesterday"
                            hoursDifference in 48..168 -> "Last 7 Days"
                            else -> "Time Ago"
                        }
                    }

                    val categoryOrder = listOf("Today", "Yesterday", "Last 7 Days", "Time Ago")
                    groupedChats.value = categoryOrder.mapNotNull { category ->
                        categorizedChats[category]?.let { category to it }
                    }
                }
            }


            ChatListDrawer(
                groupedChats = groupedChats.value,
                email = email,
                currentRoute = actualRoute,
                onChatSelected = { chatId ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate("generatorScreen/$chatId")
                    }
                }
            )
        },
        content = {
            LaunchedEffect(actualRoute) {
                if (actualRoute != null) {
                    isBottomBarVisible = !actualRoute.startsWith("generatorScreen")
                }
            }

            Scaffold(
                topBar = {
                    DefaultTopBar(
                        navController = navController,
                        isBottomBarVisible = isBottomBarVisible,
                        onHiderClick = { isBottomBarVisible = !isBottomBarVisible },
                        onLogoutClick = { logoutAdvice = true },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onCreateNewChatClick = {
                            if (actualRoute != "generatorScreen")
                                navController.navigate("generatorScreen")
                        }
                    )
                },
                bottomBar = {
                    if (isBottomBarVisible) {
                        DefaultBottomAppBar(navController = navController)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    content()
                }
            }

            if (logoutAdvice) {
                LogoutDialog(
                    onDismissRequest = {
                        logoutAdvice = false
                    },
                    onConfirmRequest = {
                        logoutAdvice = false
                        authManager.logout()
                        navController.navigate("loginScreen")
                    }
                )
            }
        }
    )

}

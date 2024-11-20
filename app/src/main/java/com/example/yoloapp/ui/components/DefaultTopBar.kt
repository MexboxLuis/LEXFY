package com.example.yoloapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.yoloapp.R
import com.example.yoloapp.utils.currentRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    navController: NavHostController,
    isBottomBarVisible: Boolean,
    onHiderClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCreateNewChatClick: () -> Unit
) {

    val actualRoute = currentRoute(navController)
    TopAppBar(
        navigationIcon = {

            if (actualRoute != null) {
                if (actualRoute.startsWith("generatorScreen")) {

                    IconButton(onClick = { onMenuClick() }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                }
            }
        },

        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(start = 10.dp),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Thin
                )
            }
        },
        actions = {

            if (actualRoute != null) {
                if (actualRoute.startsWith("generatorScreen")) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(32.dp)
                            .width(84.dp)
                            .clip(shape = RoundedCornerShape(50))
                            .padding(end = 10.dp)
                            .clickable {
                                onHiderClick()
                            }
                    ) {
                        Text(
                            text = if (isBottomBarVisible) "Hide" else "Show",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isBottomBarVisible) Icons.Default.KeyboardArrowDown
                            else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )

                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(onClick = { onCreateNewChatClick() }) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                } else {
                    IconButton(
                        onClick = { onLogoutClick() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    )
}
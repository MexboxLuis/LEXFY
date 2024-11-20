package com.example.yoloapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.packInts
import androidx.navigation.NavHostController
import com.example.yoloapp.R
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WelcomeScreen(navController: NavHostController) {

    var welcomeMessageVisible by remember { mutableStateOf(false) }
    var animatedTextIndex by remember { mutableIntStateOf(0) }
    val fullText = "Get ready to explore the impact of AI in the world of images."
    val infiniteTransition = rememberInfiniteTransition(label = "")
    var buttonVisible by remember { mutableStateOf(false) }
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    LaunchedEffect(Unit) {
        delay(500)
        welcomeMessageVisible = true

        while (animatedTextIndex < fullText.length) {
            delay(50)
            animatedTextIndex++
        }
        delay(100)
        buttonVisible = true
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (welcomeMessageVisible) {
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_bot),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(y = floatingOffset.dp)
                            .size(150.dp)
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Welcome to the era of AI!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary

                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = fullText.take(animatedTextIndex),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                    )

                }

                Spacer(modifier = Modifier.height(32.dp))

                if (buttonVisible) {

                    Button(
                        onClick = { navController.navigate("loginScreen") },
                        modifier = Modifier.offset(y = floatingOffset.dp),
                    ) {
                        Text(
                            text = "Start Now!",
                            fontSize = 16.sp,

                            )
                    }

                }
            }
        }
    }
}
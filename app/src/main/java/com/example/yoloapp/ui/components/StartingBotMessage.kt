package com.example.yoloapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.yoloapp.R
import kotlinx.coroutines.delay

@Composable
fun StartingBotMessage(modifier: Modifier = Modifier) {
    val fullMessage = "How can I assist you in creating an image today? 😊"
    var displayedMessage by remember { mutableStateOf("") }
    var isTypingFinished by remember { mutableStateOf(false) }

    LaunchedEffect(fullMessage) {
        displayedMessage = ""
        for (char in fullMessage) {
            displayedMessage += char
            delay(50)
        }
        isTypingFinished = true
    }

    Box(
        modifier = modifier
    ) {


        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)


            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_bot),
                    contentDescription = null,
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = displayedMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
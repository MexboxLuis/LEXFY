package com.example.yoloapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

@Composable
fun NoDocumentsFound() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "No documents found",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            imageVector = Icons.Filled.AddAPhoto,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(150.dp)
                .alpha(0.4f),

            )

        Text(
            text = "Scan texts now",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 100.dp)
        )

        Icon(
            imageVector = Icons.Filled.ArrowDownward,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(128.dp)
                .padding(16.dp)
                .rotate(45f)
                .alpha(0.4f),
        )
    }
}
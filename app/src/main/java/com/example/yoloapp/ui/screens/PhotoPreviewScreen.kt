package com.example.yoloapp.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.yoloapp.R
import com.example.yoloapp.ui.components.BackScaffold
import com.example.yoloapp.utils.FireStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun PhotoPreviewScreen(
    navController: NavHostController,
    imagePath: String?,
    ocrText: String?,
    firestoreManager: FireStoreManager
) {
    var finalText by rememberSaveable { mutableStateOf(ocrText ?: "") }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var saveSuccess by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    var animatedTextIndex by rememberSaveable { mutableIntStateOf(0) }
    var isAnimationFinished by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        while (animatedTextIndex < finalText.length) {
            delay(50)
            animatedTextIndex++
        }
        delay(200)
        isAnimationFinished = true

    }


    BackScaffold(
        navController = navController,
        screenName = "Photo Preview",
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    navController.popBackStack()
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (saveSuccess == true) R.drawable.img_ok else R.drawable.img_bad
                            ),
                            contentDescription = null,
                            tint = if (saveSuccess == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (saveSuccess == true)
                                "Your text was saved successfully!"
                            else
                                "Oops! Something went wrong. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                },
                confirmButton = {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(
                            onClick = {
                                showDialog = false
                                navController.navigate("ocrScreen")
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraEnhance,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Capture More!")
                        }
                        OutlinedButton(
                            onClick = {
                                showDialog = false
                                if (saveSuccess == true) {
                                    navController.navigate("homeScreen")
                                } else {
                                    coroutineScope.launch {
                                        firestoreManager.saveTextAndImage(imagePath, finalText)
                                    }
                                }
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = if (saveSuccess == true) Icons.Default.Home else Icons.Default.CameraEnhance,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (saveSuccess == true) "Go Home" else "Retry")
                        }

                    }


                },
                modifier = Modifier.padding(16.dp)
            )
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Correct Text",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                if (imagePath != null) {
                    Image(
                        bitmap = BitmapFactory.decodeFile(imagePath).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "No image available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                if (isAnimationFinished)
                    OutlinedTextField(
                        value = finalText,
                        onValueChange = { updatedText ->
                            finalText = updatedText
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Editable Text")
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(8.dp),
                    )
                else
                    OutlinedTextField(
                        value = finalText.take(animatedTextIndex),
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        label = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Editable Text")
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(8.dp),
                    )

            }
            if (!isAnimationFinished) {
                item {
                    OutlinedButton(
                        onClick = {
                            isAnimationFinished = true
                            animatedTextIndex = finalText.length
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.DoubleArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Skip Animation")
                        }

                    }
                }
            }

            item {
                if (isSaving) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            isSaving = true
                            coroutineScope.launch {
                                val result = firestoreManager.saveTextAndImage(imagePath, finalText)
                                saveSuccess = result.isSuccess
                                isSaving = false
                                showDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = finalText.isNotEmpty() && !isSaving && isAnimationFinished
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else "Save text",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

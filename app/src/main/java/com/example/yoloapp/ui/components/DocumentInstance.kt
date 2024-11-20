package com.example.yoloapp.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yoloapp.R
import com.example.yoloapp.data.DocumentData
import com.example.yoloapp.utils.FireStoreManager
import com.example.yoloapp.utils.deleteDocument
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun DocumentInstance(
    document: DocumentData,
    documentList: List<DocumentData>,
    initialIndex: Int,
    firestoreManager: FireStoreManager,
    onDocumentUpdated: (DocumentData) -> Unit,
    onDocumentDeleted: (String) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showOptionsMenu by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showOptionsMenu = true
                    },
                    onTap = { showDialog = true }
                )
            },

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val dateFormatted = SimpleDateFormat("EEE MMM dd HH:mm", Locale.getDefault())
            .format(document.date)

        Text(
            text = dateFormatted,
            style = MaterialTheme.typography.titleMedium,
        )


        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = document.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.img_loading_placeholder),
                error = painterResource(R.drawable.img_error_placeholder)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = document.text,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }

        if (showDialog) {
            DocumentDialog(
                documentList = documentList,
                initialIndex = initialIndex,
                onDismiss = { showDialog = false }
            )
        }

        if (showOptionsMenu) {
            OptionsMenu(
                onEdit = {
                    showOptionsMenu = false
                    showEditDialog = true
                },
                onDelete = {
                    showOptionsMenu = false
                    showDeleteDialog = true
                },
                onDismiss = { showOptionsMenu = false }
            )
        }

        if (showEditDialog) {
            ShowEditDialog(
                document = document,
                firestoreManager = firestoreManager,
                onDocumentUpdated = onDocumentUpdated,
                onDismiss = { showEditDialog = false }
            )
        }

        if (showDeleteDialog) {
            ShowDeleteDialog(
                onConfirmDelete = {
                    coroutineScope.launch {
                        deleteDocument(document, firestoreManager, onDocumentDeleted)
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}
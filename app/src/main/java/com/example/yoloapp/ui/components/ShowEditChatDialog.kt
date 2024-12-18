package com.example.yoloapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.yoloapp.utils.normalizeText

@Composable
fun ShowEditChatDialog(
    currentTitle: String,
    onConfirmEdit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTitle by rememberSaveable { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Chat Title", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { onDismiss() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }
        },
        text = {
            TextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("New Title") }
            )
        },
        confirmButton = {
            OutlinedButton(onClick = { onConfirmEdit(normalizeText(newTitle)) }, enabled = newTitle.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.SaveAs,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Save")
            }

        },
    )
}
package com.example.yoloapp.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShowDeleteChatDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text("Are you sure you want to delete this chat? This action cannot be undone.") },
        confirmButton = {

            OutlinedButton(onClick = { onConfirmDelete() }) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Delete")
            }
        },
        dismissButton = {

            OutlinedButton(onClick = { onDismiss() }) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cancel")
            }
        }
    )
}

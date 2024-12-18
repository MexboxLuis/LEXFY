package com.example.yoloapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.yoloapp.data.DocumentData
import com.example.yoloapp.utils.FireStoreManager
import com.example.yoloapp.utils.updateDocumentText
import kotlinx.coroutines.launch

@Composable
fun ShowEditDocumentDialog(
    document: DocumentData,
    firestoreManager: FireStoreManager,
    onDocumentUpdated: (DocumentData) -> Unit,
    onDismiss: () -> Unit
) {
    var newText by remember { mutableStateOf(document.text) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit your OCR", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            updateDocumentText(
                                document,
                                newText,
                                firestoreManager,
                                onDocumentUpdated
                            )
                            onDismiss()
                        }
                    },
                    enabled = newText.isNotEmpty(),
                ) {
                    Icon(
                        imageVector = Icons.Default.SaveAs,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save")
                }
            }
        }
    }
}
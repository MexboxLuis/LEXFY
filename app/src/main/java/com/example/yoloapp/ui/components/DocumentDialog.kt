package com.example.yoloapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.yoloapp.data.DocumentData

@Composable
fun DocumentDialog(
    documentList: List<DocumentData>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    var currentDocumentIndex by remember { mutableIntStateOf(initialIndex) }
    val currentDocument = documentList[currentDocumentIndex]

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = currentDocument.text)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            if (currentDocumentIndex > 0) {
                                currentDocumentIndex--
                            }
                        },
                        enabled = currentDocumentIndex > 0
                    ) {
                        Text("Previous")
                    }

                    TextButton(
                        onClick = {
                            if (currentDocumentIndex < documentList.size - 1) {
                                currentDocumentIndex++
                            }
                        },
                        enabled = currentDocumentIndex < documentList.size - 1
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}
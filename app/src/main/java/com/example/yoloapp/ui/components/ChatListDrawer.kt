package com.example.yoloapp.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yoloapp.data.ChatData
import com.example.yoloapp.utils.FireStoreManager
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun ChatListDrawer(
    fireStoreManager: FireStoreManager,
    email: String? = null,
    currentRoute: String?,
    groupedChats: List<Pair<String, List<Pair<String, ChatData>>>>,
    onChatSelected: (String) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "${
                email?.split("@")?.get(0)
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }'s Chats",
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (groupedChats.isEmpty()) {
            Text(
                text = "Start a new chat to see it here (it's free)!",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {

            groupedChats.forEach { (category, chats) ->

                item {
                    Text(
                        text = category,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(chats.sortedByDescending { it.second.lastModifiedAt.toDate().time }) { (id, chat) ->

                    var showOptionsMenu by rememberSaveable { mutableStateOf(false) }
                    var showEditDialog by rememberSaveable { mutableStateOf(false) }
                    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {

                        NavigationDrawerItem(
                            label = { Text(text = chat.title) },
                            selected = id == currentRoute,
                            onClick = {
                                if (id != currentRoute) {
                                    onChatSelected(id)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
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
                        ShowEditChatDialog(
                            currentTitle = chat.title,
                            onConfirmEdit = { newTitle ->
                                coroutineScope.launch {
                                    val result = fireStoreManager.updateChatTitle(id, newTitle)
                                    if (result.isSuccess) {
                                        showEditDialog = false
                                        Toast.makeText(
                                            context,
                                            "Chat title updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error updating chat title",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showEditDialog = false
                                    }
                                }
                            },
                            onDismiss = { showEditDialog = false }
                        )
                    }

                    if (showDeleteDialog) {
                        ShowDeleteChatDialog(
                            onConfirmDelete = {
                                coroutineScope.launch {
                                    val result = fireStoreManager.deleteChat(id)
                                    if (result.isSuccess) {
                                        showDeleteDialog = false
                                        Toast.makeText(
                                            context,
                                            "Chat deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error deleting chat",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showDeleteDialog = false

                                    }
                                }
                            },
                            onDismiss = { showDeleteDialog = false }
                        )
                    }
                }
            }
        }

    }
}

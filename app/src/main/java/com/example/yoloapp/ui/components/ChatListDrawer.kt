package com.example.yoloapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yoloapp.data.ChatData
import java.util.Locale


@Composable
fun ChatListDrawer(
    email: String? = null,
    currentRoute: String?,
    groupedChats: List<Pair<String, List<Pair<String, ChatData>>>>,
    onChatSelected: (String) -> Unit
) {

    val currentChatId = currentRoute
        ?.substringAfter("generatorScreen/")
        ?.takeIf { it.isNotEmpty() }

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

        groupedChats.forEach { (category, chats) ->
            Text(
                text = category,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )

            val sortedChats = chats.sortedByDescending { it.second.lastModifiedAt.toDate().time }

            sortedChats.forEach { (id, chat) ->
                NavigationDrawerItem(
                    label = { Text(text = chat.title) },
                    selected = id == currentChatId,
                    onClick = {
                        if (id != currentChatId) {
                            onChatSelected(id)
                        }
                    },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

    }
}


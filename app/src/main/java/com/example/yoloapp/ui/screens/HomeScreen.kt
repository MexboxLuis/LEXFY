package com.example.yoloapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.yoloapp.data.DocumentData
import com.example.yoloapp.ui.components.DocumentInstance
import com.example.yoloapp.ui.components.LoadingIndicator
import com.example.yoloapp.ui.components.MainScaffold
import com.example.yoloapp.ui.components.NoDocumentsFound
import com.example.yoloapp.utils.AuthManager
import com.example.yoloapp.utils.FireStoreManager
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var currentUserEmail by rememberSaveable { mutableStateOf<String?>(null) }
    var documentList by rememberSaveable { mutableStateOf<List<DocumentData>>(emptyList()) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var showOldestFirst by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = authManager.getCurrentUser()
        result.onSuccess { user ->
            currentUserEmail = user?.email
            coroutineScope.launch {
                val documents = fireStoreManager.getDocumentsByEmail(currentUserEmail!!)
                documentList = documents.sortedByDescending { it.date.time }
                isLoading = false
            }
        }.onFailure {
            isLoading = false
        }
    }

    MainScaffold(navController = navController, authManager = authManager, fireStoreManager = fireStoreManager) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (documentList.isEmpty()) {
                    NoDocumentsFound()
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("Search...") },
                            modifier = Modifier.fillMaxWidth(0.85f),
                            trailingIcon = {
                                if (searchText.isEmpty())
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null
                                    )
                                else {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        )

                        IconButton(
                            onClick = {
                                showOldestFirst = !showOldestFirst
                                documentList = if (showOldestFirst) {
                                    documentList.sortedBy { it.date }
                                } else {
                                    documentList.sortedByDescending { it.date }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (showOldestFirst) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            documentList.filter {
                                it.text.contains(searchText, ignoreCase = true)
                            }
                        ) { index, document ->
                            DocumentInstance(
                                document = document,
                                documentList = documentList,
                                initialIndex = index,
                                firestoreManager = fireStoreManager,
                                onDocumentUpdated = { updatedDocument ->
                                    documentList = documentList.map {
                                        if (it.documentId == updatedDocument.documentId) updatedDocument else it
                                    }
                                },
                                onDocumentDeleted = { deletedDocumentId ->
                                    documentList =
                                        documentList.filter { it.documentId != deletedDocumentId }
                                }
                            )
                        }
                        item {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}




